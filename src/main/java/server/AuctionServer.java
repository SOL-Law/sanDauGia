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

    public static List<ClientHandler> activeClients = new ArrayList<>();

    // 🔥 THÊM: Biến cờ kiểm soát trạng thái phòng đấu giá
    public static boolean isAuctionRunning = false;

    public static void main(String[] args) {
        int port = 8888;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("SERVER ĐÃ MỞ. Đang chờ kết nối...");

            // ❌ XÓA HOẶC COMMENT DÒNG NÀY LẠI: Không cho tự động chạy nữa
            // startAuctionTimer(60);
            System.out.println(" Server đang chờ lệnh bắt đầu phiên đấu giá...");

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
        // 🔥 KIỂM TRA: Nếu đang chạy rồi thì không cho chạy đè lên nhau
        if (isAuctionRunning) {
            System.out.println("⚠️ Một phiên đấu giá đang diễn ra, từ chối lệnh bắt đầu mới!");
            return;
        }

        isAuctionRunning = true; // Khóa cửa phòng, đánh dấu đang diễn ra
        AuctionManager.getInstance().startNewSession();

        // (TÙY CHỌN) Ở đây bạn có thể gọi Database để load thông tin món hàng tiếp theo ra
        // AuctionManager.getInstance().loadNextItemFromDB();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Gson gson = new Gson();
        final int[] timeLeft = {seconds};

        System.out.println("🟢 BẮT ĐẦU PHIÊN MỚI! Countdown: " + seconds + " giây");

        // Gửi thông báo cho toàn bộ Client biết phòng đã mở
        broadcast(gson.toJson(new Request("AUCTION_START", "Phiên đấu giá bắt đầu!")));

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Gửi Timer
                String timerJson = gson.toJson(new Request("TIMER_UPDATE", String.valueOf(timeLeft[0])));
                broadcast(timerJson);

                // HẾT GIỜ
                if (timeLeft[0] <= 0) {
                    System.out.println("🔴 HẾT GIỜ! Đang chốt phiên đấu giá...");

                    AuctionManager manager = AuctionManager.getInstance();
                    manager.endAuction();

                    String result = manager.getAllItems();
                    String message = "HẾT GIỜ! KẾT QUẢ CUỐI:\n" + result;

                    broadcast(gson.toJson(new Request("AUCTION_END", message)));

                    scheduler.shutdown(); // Tắt đồng hồ này đi

                    // 🔥 QUAN TRỌNG NHẤT: Mở khóa biến cờ để sẵn sàng cho phiên tiếp theo
                    isAuctionRunning = false;
                    System.out.println("✅ Đã khóa sổ! Sẵn sàng cho phiên mới.");
                }

                timeLeft[0]--;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void broadcast(String jsonMessage) {
        for (ClientHandler client : activeClients) {
            client.sendMessage(jsonMessage);
        }
    }
}