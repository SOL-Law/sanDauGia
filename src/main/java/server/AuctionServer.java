package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// THÊM 5 THƯ VIỆN NÀY ĐỂ CHẠY ĐỒNG HỒ VÀ PHÁT LOA BẰNG JSON
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;
import network.Request;

public class AuctionServer {
    // Danh sách lưu trữ tất cả các ClientHandler đang kết nối (Dùng cho Realtime Observer)
    public static List<ClientHandler> activeClients = new ArrayList<>();

    public static void main(String[] args) {
        int port = 8888;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TỔNG ĐÀI SERVER ĐÃ MỞ (CHẾ ĐỘ ĐA LUỒNG). Đang chờ kết nối...");

            // ==========================================
            // GỌI ĐỒNG HỒ ĐẾM NGƯỢC CHẠY (60 Giây)
            // ==========================================
            startAuctionTimer(60);

            // Vòng lặp vô hạn để Server luôn mở cửa
            while (true) {
                // Chờ Client kết nối
                Socket clientSocket = serverSocket.accept();
                System.out.println("=> Bắt được kết nối từ Client cổng: " + clientSocket.getPort());

                // 1. Tạo một "Nhân viên" (ClientHandler) để phục vụ riêng client này
                ClientHandler clientThread = new ClientHandler(clientSocket);

                // 2. Thêm vào danh sách quản lý để sau này broadcast (phát sóng) tin nhắn
                activeClients.add(clientThread);
                System.out.println("   [Số người đang online: " + activeClients.size() + "]");

                // 3. Kích hoạt luồng chạy độc lập (Không làm nghẽn vòng lặp của Server)
                new Thread(clientThread).start();
            }
        } catch (Exception e) {
            System.out.println("Lỗi Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================================================
    // TÍNH NĂNG ĐẾM NGƯỢC CHỐT PHIÊN ĐẤU GIÁ
    // ==================================================
    public static void startAuctionTimer(int durationInSeconds) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        System.out.println("⏰ Đồng hồ đếm ngược bắt đầu: " + durationInSeconds + " giây!");

        // Lệnh này sẽ delay đúng số giây ông cài đặt rồi mới chạy khối code bên trong
        scheduler.schedule(() -> {
            try {
                System.out.println("🔔 HẾT GIỜ! Đang chốt phiên đấu giá...");

                // Lấy giá chốt cuối cùng (gọi thẳng vào Database/Mock DAO của ông)
                double finalPrice = server.dao.AuctionDao.getCurrentPrice(1);

                // Gom thông báo và phát loa cho tất cả Client đang online
                String message = "HẾT GIỜ! Phiên đấu giá kết thúc. Món hàng đã được chốt với giá: " + finalPrice + " VNĐ";
                Gson gson = new Gson();
                String alertJson = gson.toJson(new Request("AUCTION_END", message));

                // Gọi hàm broadcast có sẵn để phát loa
                broadcast(alertJson);

                // Tắt bộ đếm để giải phóng RAM
                scheduler.shutdown();
                System.out.println("🔒 Đã khóa sổ phiên đấu giá!");

            } catch (Exception e) {
                System.out.println("Lỗi lúc chốt phiên: " + e.getMessage());
                e.printStackTrace();
            }
        }, durationInSeconds, TimeUnit.SECONDS);
    }

    // Hàm này duyệt qua danh sách tất cả client đang kết nối và gửi tin nhắn cho họ
    public static void broadcast(String jsonMessage) {
        System.out.println(" [SERVER PHÁT SÓNG]: " + jsonMessage);
        for (ClientHandler client : activeClients) {
            client.sendMessage(jsonMessage);
        }
    }
}