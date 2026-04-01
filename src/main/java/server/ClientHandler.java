package server;

import com.google.gson.Gson;
import network.Request;
import server.dao.UserDao;
import model.user.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    // Đã thêm chữ 'final' để triệt tiêu warning màu vàng của IntelliJ
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    // Ổ khóa bảo vệ luồng đấu giá đồng thời (Concurrency)
    private static final Object BID_LOCK = new Object();

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
                        // Gọi hàm xử lý đặt giá
                        processBid(req.getPayload());
                        break;
                }
            }
        } catch (Exception e) {
            // 🚨 In ra lỗi màu đỏ để biết code sai ở đâu!
            System.out.println("❌ Client " + socket.getPort() + " đã ngắt kết nối do văng lỗi (Crash)!");
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
    // KHÚC NÀY LÀ KHÚC VỪA ĐƯỢC NÂNG CẤP ĐÂY!
    // ==========================================
    private void processBid(String payloadJson) {
        synchronized (BID_LOCK) {
            try {
                // 1. Dùng Gson dịch cục JSON (ví dụ: {"price": 98}) thành object BidInfo
                model.BidInfo bid = gson.fromJson(payloadJson, model.BidInfo.class);

                System.out.println("Nhận được mức giá mới từ Client: " + bid.getPrice());

                // 2. Gom thông báo chứa mức giá thật sự để chuẩn bị phát loa
                String message = "Cập nhật! Giá mới nhất là: " + bid.getPrice() + " VNĐ";
                String alertJson = gson.toJson(new Request("UPDATE_PRICE", message));

                // 3. Phát loa cho tất cả mọi người trong phòng biết
                AuctionServer.broadcast(alertJson);

            } catch (Exception e) {
                System.out.println("Lỗi lúc đọc giá: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}