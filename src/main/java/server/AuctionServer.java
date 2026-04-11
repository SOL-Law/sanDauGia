package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import network.Request;
import model.AuctionManager;

public class AuctionServer {

    // Danh sách client (Observer)
    public static List<ClientHandler> activeClients = new ArrayList<>();

    public static void main(String[] args) {
        int port = 8888;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("SERVER ĐÃ MỞ. Đang chờ kết nối...");

            // chạy timer
            startAuctionTimer(60);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("=> Client: " + clientSocket.getPort());

                ClientHandler clientThread = new ClientHandler(clientSocket);

                activeClients.add(clientThread);
                System.out.println("   Online: " + activeClients.size());

                new Thread(clientThread).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // TIMER KẾT THÚC ĐẤU GIÁ
    // =========================
    public static void startAuctionTimer(int seconds) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> {
            try {
                System.out.println("🔔 HẾT GIỜ! Đang chốt phiên đấu giá...");

                // 🔥 khóa phiên đấu giá
                AuctionManager manager = AuctionManager.getInstance();
                manager.endAuction();

                // 🔥 lấy dữ liệu cuối từ manager
                String result = manager.getAllItems();

                String message = "HẾT GIỜ! KẾT QUẢ CUỐI:\n" + result;

                Gson gson = new Gson();
                String alertJson = gson.toJson(new Request("AUCTION_END", message));

                broadcast(alertJson);

                scheduler.shutdown();
                System.out.println("🔒 Đã khóa sổ phiên đấu giá!");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, seconds, TimeUnit.SECONDS); // ✅ sửa đúng biến
    }

    // =========================
    // BROADCAST (REALTIME)
    // =========================
    public static void broadcast(String jsonMessage) {
        System.out.println("[Broadcast]: " + jsonMessage);

        for (ClientHandler client : activeClients) {
            client.sendMessage(jsonMessage);
        }
    }
}