package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AuctionServer {
    // Danh sách lưu trữ tất cả các ClientHandler đang kết nối (Dùng cho Realtime Observer)
    public static List<ClientHandler> activeClients = new ArrayList<>();

    public static void main(String[] args) {
        int port = 8888;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TỔNG ĐÀI SERVER ĐÃ MỞ (CHẾ ĐỘ ĐA LUỒNG). Đang chờ kết nối...");

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
    // Hàm này duyệt qua danh sách tất cả client đang kết nối và gửi tin nhắn cho họ
    public static void broadcast(String jsonMessage) {
        System.out.println(" [SERVER PHÁT SÓNG]: " + jsonMessage);
        for (ClientHandler client : activeClients) {
            client.sendMessage(jsonMessage);
        }
    }
}
