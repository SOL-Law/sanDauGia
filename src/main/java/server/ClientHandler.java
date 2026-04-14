package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import network.Request;
import server.dao.UserDao;
import model.user.User;
import model.AuctionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            String jsonReceived;
            AuctionManager manager = AuctionManager.getInstance();

            while ((jsonReceived = in.readLine()) != null) {

                Request req = gson.fromJson(jsonReceived, Request.class);
                System.out.println("[Client " + socket.getPort() + "] -> " + req.getAction());

                switch (req.getAction()) {

                    // =========================
                    // LOGIN
                    // =========================
                    case "LOGIN":
                        JsonObject loginObj = gson.fromJson(req.getPayload(), JsonObject.class);
                        String usernameLogin = loginObj.get("username").getAsString();
                        String passwordLogin = loginObj.get("password").getAsString();

                        String role = UserDao.login(usernameLogin, passwordLogin);

                        if (role != null) {
                            JsonObject res = new JsonObject();
                            res.addProperty("role", role);
                            sendMessage(gson.toJson(new Request("LOGIN_SUCCESS", res.toString())));
                        } else {
                            sendMessage(gson.toJson(new Request("ERROR", "Sai tài khoản hoặc mật khẩu!")));
                        }
                        break;

                    // =========================
                    // REGISTER
                    // =========================
                    case "REGISTER":
                        User userReg = gson.fromJson(req.getPayload(), User.class);

                        if (UserDao.register(userReg.getUsername(), userReg.getPassword())) {
                            sendMessage(gson.toJson(new Request("REGISTER_SUCCESS", "")));
                        } else {
                            sendMessage(gson.toJson(new Request("ERROR", "User đã tồn tại!")));
                        }
                        break;

                    // =========================
                    // LOAD DATA
                    // =========================
                    case "GET_AUCTION":
                        String data = manager.getAllItems();
                        sendMessage(gson.toJson(new Request("AUCTION_UPDATE", data)));
                        break;

                    // =========================
                    // PLACE BID (🔥 FIX CHUẨN ĐỒNG BỘ VỚI ĐỒNG ĐỘI)
                    // =========================
                    case "PLACE_BID":
                        // 1. Kiểm tra xem phiên đấu giá còn chạy không
                        if (!manager.isRunning()) {
                            sendMessage(gson.toJson(new Request("ERROR", "Phiên đấu giá đã kết thúc!")));
                            break;
                        }

                        // 2. Đọc dữ liệu Client gửi lên
                        JsonObject obj = gson.fromJson(req.getPayload(), JsonObject.class);
                        String itemName = obj.get("item").getAsString();
                        int price = obj.get("price").getAsInt(); // Đã sửa thành getAsInt để khớp với AuctionManager

                        // 3. Tạo username tạm dựa vào cổng kết nối
                        String username = "user" + socket.getPort();

                        // 4. Gọi thẳng hàm placeBid của đồng đội (Hàm này đã có sẵn synchronized chống dẫm đạp)
                        boolean success = manager.placeBid(itemName, price, username);

                        if (success) {
                            // Nếu đặt giá thành công -> Cập nhật danh sách mới nhất
                            String newData = manager.getAllItems();
                            Request resUpdate = new Request("AUCTION_UPDATE", newData);

                            // Phát loa cho toàn bộ Server (Real-time)
                            AuctionServer.broadcast(gson.toJson(resUpdate));
                        } else {
                            // Nếu đặt giá thất bại (giá thấp hơn giá hiện tại)
                            sendMessage(gson.toJson(new Request("ERROR", "Mức giá không hợp lệ (Phải cao hơn giá hiện tại)!")));
                        }
                        break;

                    // =========================
                    // ADD ITEM
                    // =========================
                    case "ADD_ITEM":
                        JsonObject addObj = gson.fromJson(req.getPayload(), JsonObject.class);
                        String name = addObj.get("item").getAsString();
                        int startPrice = addObj.get("price").getAsInt();

                        manager.addItem(name, startPrice);

                        String newDataAfterAdd = manager.getAllItems();
                        Request resAdd = new Request("AUCTION_UPDATE", newDataAfterAdd);

                        AuctionServer.broadcast(gson.toJson(resAdd));
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Client " + socket.getPort() + " disconnect!");
        } finally {
            try {
                AuctionServer.activeClients.remove(this);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String jsonMessage) {
        if (out != null) {
            out.println(jsonMessage);
        }
    }
}