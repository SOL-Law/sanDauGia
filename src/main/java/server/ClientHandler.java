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
                    case "GET_HISTORY":
                        // Gọi DB lấy lịch sử và gửi về cho Client
                        String historyData = server.dao.ItemDao.getAuctionHistory();
                        sendResponse("HISTORY_DATA", historyData);
                        break;
                    case "GET_MY_HISTORY":
                        // payload ở đây chính là cái username (ví dụ: "admin") do Client gửi lên
                        String myHistoryData = server.dao.ItemDao.getPersonalHistory(payload);
                        sendResponse("HISTORY_DATA", myHistoryData);
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

            boolean ok = UserDao.register(username, password);

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

        // 🔥 FIX QUAN TRỌNG: Client phải gửi kèm username lên
        String username = (String) obj.get("username");

        AuctionManager manager = AuctionManager.getInstance();

        // Ghi nhận đặt giá. Nếu thành công thì Loan báo cho cả phòng!
        if (manager.placeBid(item, (int) price, username)) {
            String data = manager.getAllItems();
            AuctionServer.broadcast(gson.toJson(new Request("UPDATE_AUCTION", data)));

            // 💡 Tính năng xịn: Lưu lịch sử vào Database ngay lúc này luôn!
            server.dao.ItemDao.insertBidHistory(item, username, (int) price);
        }
    }

    // =========================
    // UPLOAD ITEM (ĐĂNG SẢN PHẨM)
    // =========================
    private void handleUpload(String payload) {
        var obj = gson.fromJson(payload, java.util.Map.class);
        String name = (String) obj.get("name");
        double price = Double.parseDouble(obj.get("price").toString());
        String base64Image = (String) obj.get("image");

        //  Đọc số giây người dùng nhập từ Client (Nếu không có thì để 60)
        int duration = 60;
        if (obj.containsKey("time")) {
            duration = (int) Double.parseDouble(obj.get("time").toString());
        }

        // Lưu vào kho
        server.dao.ItemDao.insertItem(name, (int) price, base64Image);

        // Đưa lên sàn RAM (Gửi kèm thời gian)
        model.AuctionManager manager = model.AuctionManager.getInstance();
        manager.addItem(name, (int) price, base64Image, duration);

        // Bắt đầu đếm ngược thời gian đó
        manager.startAuctionTimer(name, duration);

        // Loan báo cho cả phòng
        String data = manager.getAllItems();
        server.AuctionServer.broadcast(gson.toJson(new network.Request("UPDATE_AUCTION", data)));
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