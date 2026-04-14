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

            // chạy timer realtime
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
    // TIMER REALTIME + KẾT THÚC
    // =========================
    public static void startAuctionTimer(int seconds) {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Gson gson = new Gson();

        final int[] timeLeft = {seconds};

        System.out.println("⏰ Bắt đầu countdown: " + seconds + " giây");

        // 🔥 chạy mỗi giây
        scheduler.scheduleAtFixedRate(() -> {
            try {

                // ===== GỬI TIMER CHO CLIENT =====
                String timerJson = gson.toJson(
                        new Request("TIMER_UPDATE", String.valueOf(timeLeft[0]))
                );

                broadcast(timerJson);

                // ===== HẾT GIỜ =====
                if (timeLeft[0] <= 0) {

                    System.out.println("🔔 HẾT GIỜ! Đang chốt phiên đấu giá...");

                    AuctionManager manager = AuctionManager.getInstance();
                    manager.endAuction();

                    String result = manager.getAllItems();

                    String message = "HẾT GIỜ! KẾT QUẢ CUỐI:\n" + result;

                    String alertJson = gson.toJson(
                            new Request("AUCTION_END", message)
                    );

                    broadcast(alertJson);

                    scheduler.shutdown();
                    System.out.println("🔒 Đã khóa sổ phiên đấu giá!");
                }

                timeLeft[0]--;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 0, 1, TimeUnit.SECONDS);
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