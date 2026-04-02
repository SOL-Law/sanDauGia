package server;

import com.google.gson.Gson;
import network.Request;
import server.dao.AuctionDao; // <-- Thêm dòng import này
import server.dao.UserDao;
import model.user.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    // Đã XÓA biến currentHighestBid và BID_LOCK đi vì mình sẽ lấy dữ liệu chuẩn từ DAO và Lock thẳng vào class DAO

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("Lỗi khởi tạo luồng cho Client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String jsonReceived;
            while ((jsonReceived = in.readLine()) != null) {
                Request req = gson.fromJson(jsonReceived, Request.class);
                System.out.println("[Client " + socket.getPort() + "] Yêu cầu: " + req.getAction());

                switch (req.getAction()) {
                    case "LOGIN":
                        User userLogin = gson.fromJson(req.getPayload(), User.class);
                        if (UserDao.login(userLogin.getUsername(), userLogin.getPassword())) {
                            System.out.println("Client " + socket.getPort() + " đăng nhập THÀNH CÔNG!");
                            sendMessage(gson.toJson(new Request("LOGIN_SUCCESS", "Đăng nhập thành công!")));
                        } else {
                            sendMessage(gson.toJson(new Request("ERROR", "Sai thông tin đăng nhập!")));
                        }
                        break;

                    case "REGISTER":
                        User userReg = gson.fromJson(req.getPayload(), User.class);
                        if (UserDao.register(userReg.getUsername(), userReg.getPassword())) {
                            sendMessage(gson.toJson(new Request("REGISTER_SUCCESS", "Tạo tài khoản thành công!")));
                        } else {
                            sendMessage(gson.toJson(new Request("ERROR", "Tên đăng nhập đã tồn tại!")));
                        }
                        break;

                    case "PLACE_BID":
                        processBid(req.getPayload());
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(" Client " + socket.getPort() + " đã ngắt kết nối do văng lỗi (Crash)!");
            System.out.println("==== CHI TIẾT LỖI BÊN DƯỚI ====");
            e.printStackTrace();
            System.out.println("===============================");
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

    // ==========================================
    // LOGIC ĐẤU GIÁ (ĐÃ ĐỒNG BỘ CONCURRENCY & DAO)
    // ==========================================
    private void processBid(String payloadJson) {

        // Dùng AuctionDao.class làm ổ khóa. Đảm bảo 2 người gọi DB cùng lúc sẽ phải xếp hàng
        synchronized (AuctionDao.class) {
            try {
                model.BidInfo bid = gson.fromJson(payloadJson, model.BidInfo.class);
                System.out.println("Client gửi lên mức giá: " + bid.getPrice());

                // Mặc định món hàng số 1 và user số 1 (Hoặc ông có thể lấy từ bid.getAuctionId() nếu class BidInfo của ông có)
                int auctionId = 1;
                int userId = 1;

                // 1. Kiểm tra trạng thái: Phiên đấu giá còn mở không?
                if (AuctionDao.isAuctionRunning(auctionId)) {

                    // 2. Lấy giá hiện tại từ DB (Hoặc từ hàm Mock)
                    double currentPrice = AuctionDao.getCurrentPrice(auctionId);

                    // 3. Kiểm tra giá: Mức giá đưa ra phải lớn hơn giá hiện tại
                    if (bid.getPrice() <= currentPrice) {
                        sendMessage(gson.toJson(new Request("ERROR", "Giá không hợp lệ! Phải cao hơn " + currentPrice + " VNĐ")));
                    } else {
                        // 4. Lưu lịch sử: Cập nhật giá mới xuống DB
                        AuctionDao.placeBid(auctionId, userId, bid.getPrice());

                        // 5. Cập nhật theo thời gian thực (Observer Pattern)
                        String message = "Cập nhật! Giá mới nhất là: " + bid.getPrice() + " VNĐ";
                        String alertJson = gson.toJson(new Request("UPDATE_PRICE", message));
                        AuctionServer.broadcast(alertJson);
                    }
                } else {
                    // Báo lỗi nếu phiên đã đóng
                    sendMessage(gson.toJson(new Request("ERROR", "Phiên đấu giá này đã kết thúc!")));
                }

            } catch (Exception e) {
                System.out.println("Lỗi lúc đọc giá: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}