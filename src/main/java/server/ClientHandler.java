package server;

import com.google.gson.Gson;
import network.Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// implements Runnable giúp class này chạy song song được với các class khác
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    // Constructor: Nhận socket từ Server truyền vào khi có kết nối
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
        try {
            // Mở luồng nhận và gửi dữ liệu cho riêng client này
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm run() sẽ chứa những việc luồng này làm (chạy 24/7 chờ tin nhắn từ Client)
    // Hàm run() sẽ chứa những việc luồng này làm (chạy 24/7 chờ tin nhắn từ Client)
    @Override
    public void run() {
        try {
            String jsonReceived;
            // Chỉ dùng 1 vòng lặp while duy nhất để đọc tin nhắn
            while ((jsonReceived = in.readLine()) != null) {
                Request req = gson.fromJson(jsonReceived, Request.class);
                System.out.println("[Client " + socket.getPort() + "] Yêu cầu: " + req.getAction());

                // PHÂN LOẠI YÊU CẦU TỪ CLIENT
                switch (req.getAction()) {
                    case "PLACE_BID":
                        // Khi có người đặt giá, đóng gói mức giá đó thành một thông báo mới
                        // Gắn mác action là "UPDATE_PRICE" và phát sóng cho toàn bộ mạng lưới
                        String alertJson = gson.toJson(new Request("UPDATE_PRICE", req.getPayload()));
                        AuctionServer.broadcast(alertJson);
                        break;

                    case "LOGIN":
                        // Logic đăng nhập sẽ làm sau...
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Client " + socket.getPort() + " đã ngắt kết nối đột ngột.");
        } finally {
            // Xử lý dọn dẹp khi Client chủ động thoát hoặc rớt mạng
            try {
                System.out.println("Đóng kết nối với Client " + socket.getPort());
                AuctionServer.activeClients.remove(this); // Xóa khỏi danh sách online
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Hàm này cực kỳ quan trọng cho tính năng Realtime (Observer Pattern)
    // Dùng để Server chủ động "bắn" tin nhắn ngược về cho Client
    public void sendMessage(String jsonMessage) {
        if (out != null) {
            out.println(jsonMessage);
        }
    }
}