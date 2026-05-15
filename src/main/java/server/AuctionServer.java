package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import network.Request;
import model.AuctionManager;

public class AuctionServer {

    // Danh sách các Client đang online
    public static List<ClientHandler> activeClients = new ArrayList<>();
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // 1. Kéo đồ từ kho (Database) lên trưng bày (RAM)
        server.dao.ItemDao.loadAllItemsToManager();

        int port = 8888;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("✅ SERVER ĐÃ MỞ (Cổng " + port + "). Đang chờ kết nối...");

            while (true) {
                // Lắng nghe khách vào
                Socket clientSocket = serverSocket.accept();
                System.out.println("=> Có Client mới vào phòng (Port: " + clientSocket.getPort() + ")");

                // Cử một nhân viên (ClientHandler) ra tiếp khách
                ClientHandler clientThread = new ClientHandler(clientSocket);
                activeClients.add(clientThread);
                System.out.println("   Số người đang Online: " + activeClients.size());

                // Cho nhân viên bắt đầu làm việc
                new Thread(clientThread).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // GỬI DANH SÁCH CHO 1 KHÁCH HÀNG
    // =========================
    public static void sendAuctionDataTo(ClientHandler client) {
        try {
            String data = AuctionManager.getInstance().getAllItems();
            client.sendMessage(gson.toJson(new Request("UPDATE_AUCTION", data)));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi gửi dữ liệu cho Client!");
        }
    }

    // =========================
    // PHÁT LOA CHO TẤT CẢ PHÒNG (BROADCAST)
    // =========================
    public static void broadcast(String jsonMessage) {
        for (ClientHandler client : activeClients) {
            client.sendMessage(jsonMessage);
        }
    }
}