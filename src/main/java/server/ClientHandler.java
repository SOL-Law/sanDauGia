package server;

import com.google.gson.Gson;
import network.Request;
import server.dao.UserDao;
import model.AuctionManager;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Client gửi: " + line);

                Request req = gson.fromJson(line, Request.class);
                String type = req.getType();
                String payload = req.getPayload();

                switch (type) {
                    case "LOGIN":
                        handleLogin(payload);
                        break;
                    case "REGISTER":
                        handleRegister(payload);
                        break;
                    case "PLACE_BID":
                        handleBid(payload);
                        break;
                    case "UPLOAD_ITEM":
                        handleUpload(payload);
                        break;
                    case "GET_AUCTION":
                        sendAuctionData();
                        break;
                    case "GET_BALANCE":
                        handleGetBalance(payload);
                        break;
                    case "DEPOSIT":
                        handleDeposit(payload);
                        break;
                    case "GET_MY_HISTORY":
                        // payload ở đây chính là cái username (ví dụ: "admin") do Client gửi lên
                        String myHistoryData = server.dao.ItemDao.getPersonalHistory(payload);
                        sendResponse("HISTORY_DATA", myHistoryData);
                        break;
                    case "GET_CHART":
                        // payload là Tên món đồ (VD: "Laptop Gaming")
                        String chartData = server.dao.ItemDao.getChartData(payload);
                        // Trả về dạng: TênMónĐồ|15:30:00-1500,15:35:00-2000
                        sendResponse("CHART_DATA", payload + "|" + chartData);
                        break;
                    case "DELETE_ITEM":
                        // payload gửi lên chính là tên món đồ cần xóa
                        model.AuctionManager.getInstance().deleteItemFromSystem(payload);
                        break;

                    case "EDIT_ITEM":
                        try {
                            // Bóc tách JSON gửi lên từ Client
                            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
                            String oldName = json.get("oldName").getAsString();
                            String newName = json.get("newName").getAsString();

                            //  truyền 2 tham số vào Manager
                            model.AuctionManager.getInstance().editItemInSystem(oldName, newName);
                        } catch (Exception ex) {
                            // Lỗi ở lệnh này thì in ra Console, quyết không để sập kết nối của Client!
                            System.out.println("Lỗi nội bộ khi sửa tên: " + ex.getMessage());
                        }
                        break;
                    case "GET_PROFILE":
                        // Client gửi tên lên, Server trả ID về
                        String userId = server.dao.UserDao.getUserId(payload);
                        sendResponse("PROFILE_DATA", userId);
                        break;

                    case "UPDATE_PROFILE":
                        // Client gửi JSON chứa tên cũ, tên mới, pass mới
                        com.google.gson.JsonObject pObj = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
                        String oldU = pObj.get("oldUser").getAsString();
                        String newU = pObj.get("newUser").getAsString();
                        String newP = pObj.get("newPass").getAsString();

                        server.dao.UserDao.updateProfile(oldU, newU, newP);
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
            AuctionServer.activeClients.remove(this); // Nhớ xóa client khi họ thoát
        }
    }

    // =========================
    // LOGIN
    // =========================
    private void handleLogin(String payload) {
        try {
            var obj = gson.fromJson(payload, Map.class);
            String username = (String) obj.get("username");
            String password = (String) obj.get("password");

            String role = UserDao.login(username, password);

            if (role != null) {
                Map<String, String> res = new HashMap<>();
                res.put("role", role);
                String jsonPayload = gson.toJson(res);

                sendResponse("LOGIN_SUCCESS", jsonPayload);

                // Vừa login xong thì gửi danh sách đồ cho người ta xem
                sendAuctionData();

                // (Đã xóa dòng sendTimer() cũ bị lỗi)
            } else {
                sendResponse("LOGIN_FAIL", "Sai tài khoản hoặc mật khẩu");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse("ERROR", "Lỗi server");
        }
    }

    // =========================
    // REGISTER
    // =========================
    private void handleRegister(String payload) {
        try {
            var obj = gson.fromJson(payload, Map.class);
            String username = (String) obj.get("username");
            String password = (String) obj.get("password");

            // LẤY THÊM ROLE TỪ CLIENT GỬI LÊN
            String role = (String) obj.get("role");

            // Lớp bảo hiểm: Nếu bên giao diện quên gửi role, mặc định cho làm Người mua
            if (role == null || role.trim().isEmpty()) {
                role = "BIDDER";
            }

            // Truyền đủ 3 tham số vào hàm register mới
            boolean ok = UserDao.register(username, password, role);

            if (ok) {
                sendResponse("REGISTER_SUCCESS", "OK");
            } else {
                sendResponse("REGISTER_FAIL", "Tài khoản đã tồn tại");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse("ERROR", "Lỗi đăng ký");
        }
    }

    // =========================
    // BID (ĐẶT GIÁ)
    // =========================
    private void handleBid(String payload) {
        var obj = gson.fromJson(payload, Map.class);
        String item = (String) obj.get("item");
        double price = (double) obj.get("price");
        String username = (String) obj.get("username");

        double currentBalance = server.dao.UserDao.getBalance(username);
        if (currentBalance < price) {
            // Tiền trong ví ít hơn giá định đặt -> Báo lỗi và đuổi về!
            sendResponse("NOTIFY", " Đấu giá thất bại! Số dư của bạn không đủ để đặt mức giá này.");
            return;
        }

        AuctionManager manager = AuctionManager.getInstance();

        // Ghi nhận đặt giá. Nếu thành công thì Loan báo cho cả phòng!
        if (manager.placeBid(item, (int) price, username)) {
            String data = manager.getAllItems();
            AuctionServer.broadcast(gson.toJson(new Request("UPDATE_AUCTION", data)));

            // Lưu lịch sử vào Database ngay lúc này luôn!
            server.dao.ItemDao.insertBidHistory(item, username, (int) price);
        } else {
            sendResponse("NOTIFY", " Đấu giá thất bại! Vui lòng đặt giá cao hơn giá hiện tại.");
        }
    }

    // =========================
    // UPLOAD ITEM (ĐĂNG SẢN PHẨM)
    // =========================
    private void handleUpload(String payload) {
        try {
            var obj = gson.fromJson(payload, java.util.Map.class);
            String name = (String) obj.get("name");
            double price = Double.parseDouble(obj.get("price").toString());
            String base64Image = (String) obj.get("image");

            //  BÓC TÁCH THÊM NGƯỜI BÁN VÀ DANH MỤC
            String seller = (String) obj.get("username");
            String category = (String) obj.get("category");

            // Đọc số giây người dùng nhập từ Client (Nếu không có thì để 60)
            int duration = 60;
            if (obj.containsKey("time")) {
                duration = (int) Double.parseDouble(obj.get("time").toString());
            }

            // 1. Lưu vào Database (Gọi chuẩn hàm insertItem)
            server.dao.ItemDao.insertItem(name, (int) price, base64Image, seller, category);

            // 2. Đưa lên sàn RAM (Truyền đủ 6 tham số)
            model.AuctionManager manager = model.AuctionManager.getInstance();
            manager.addItem(name, (int) price, base64Image, duration, seller, category);

            // 3. Bắt đầu đếm ngược thời gian đó
            manager.startAuctionTimer(name, duration);

            // 4. Loan báo cho cả phòng
            String data = manager.getAllItems();
            server.AuctionServer.broadcast(gson.toJson(new network.Request("UPDATE_AUCTION", data)));

        } catch (Exception e) {
            System.out.println("Lỗi xử lý Upload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================
    // LẤY DANH SÁCH ĐỒ
    // =========================
    private void sendAuctionData() {
        String data = AuctionManager.getInstance().getAllItems();
        sendResponse("UPDATE_AUCTION", data);
    }

    // =========================
    // XỬ LÝ NẠP TIỀN
    // =========================
    private void handleDeposit(String payload) {
        try {
            var obj = gson.fromJson(payload, Map.class);
            String username = (String) obj.get("username");
            double amount = (double) obj.get("amount");

            if (UserDao.deposit(username, amount)) {
                double newBalance = UserDao.getBalance(username);
                sendResponse("UPDATE_BALANCE", String.valueOf(newBalance));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // LẤY SỐ DƯ
    // =========================
    private void handleGetBalance(String payload) {
        try {
            String username = payload.trim();
            double balance = UserDao.getBalance(username);
            sendResponse("UPDATE_BALANCE", String.valueOf(balance));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // GỬI TIN NHẮN (Cho Broadcast gọi)
    // =========================
    public synchronized void sendMessage(String msg) {
        out.println(msg);
        out.flush();
    }

    // =========================
    // HELPER GỬI LỆNH JSON
    // =========================
    private void sendResponse(String type, String payload) {
        Request res = new Request(type, payload);
        String json = gson.toJson(res);
        out.println(json);
        System.out.println("Server trả: " + json);
    }
}