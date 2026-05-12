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

    public static boolean isAuctionRunning = false;

    private static int remainingTime = 0;

    private static ScheduledExecutorService scheduler;

    private static Gson gson = new Gson();

    public static void main(String[] args) {

        int port = 8888;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("SERVER ĐÃ MỞ. Đang chờ kết nối...");
            System.out.println("⏳ Server đang chờ lệnh bắt đầu phiên đấu giá...");

            while (true) {

                Socket clientSocket =
                        serverSocket.accept();

                System.out.println(
                        "=> Client: "
                                + clientSocket.getPort()
                );

                ClientHandler clientThread =
                        new ClientHandler(clientSocket);

                activeClients.add(clientThread);

                System.out.println(
                        "   Online: "
                                + activeClients.size()
                );

                new Thread(clientThread).start();

                // 🔥 FIX QUAN TRỌNG:
                // gửi danh sách item ngay khi client connect


                // 🔥 FIX QUAN TRỌNG:
                // sync timer nếu session đang chạy
                if (isAuctionRunning) {

                    clientThread.sendMessage(

                            gson.toJson(

                                    new Request(
                                            "TIMER",
                                            String.valueOf(remainingTime)
                                    )
                            )
                    );
                }
            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }
    }

    // =========================
    // START TIMER SESSION
    // =========================
    public static void startAuctionTimer(int seconds) {

        if (isAuctionRunning) {

            System.out.println(
                    "⚠️ Phiên đang chạy rồi!"
            );

            return;
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                // Đợi 1 giây để nó thực sự chết hẳn
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.out.println("⚠️ Scheduler cũ quá lì lợm!");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isAuctionRunning = true;

        AuctionManager manager = AuctionManager.getInstance();
        manager.startNewSession();

        // BƯỚC QUAN TRỌNG: Gửi danh sách đồ ngay khi vừa Bắt đầu
        String data = manager.getAllItems();
        broadcast(gson.toJson(new Request("UPDATE_AUCTION", data)));
        remainingTime = seconds;

        System.out.println(
                "🟢 BẮT ĐẦU PHIÊN: "
                        + seconds
                        + " giây"
        );

        broadcast(

                gson.toJson(

                        new Request(
                                "START_SESSION",
                                "Phiên đấu giá bắt đầu!"
                        )
                )
        );

        scheduler =
                Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {

            try {
                if (!isAuctionRunning) return;

                broadcast(

                        gson.toJson(

                                new Request(
                                        "TIMER",
                                        String.valueOf(remainingTime)
                                )
                        )
                );

                if (remainingTime <= 0) {

                    System.out.println("🔴 HẾT GIỜ");

                    AuctionManager.getInstance().endAuction();

                    String result =
                            manager.getAllItems();

                    broadcast(

                            gson.toJson(

                                    new Request(
                                            "END_SESSION",
                                            result
                                    )
                            )
                    );

                    scheduler.shutdown();

                    scheduler = null;

                    isAuctionRunning = false;

                    System.out.println(
                            "✅ Sẵn sàng phiên mới"
                    );
                }

                remainingTime--;

            }

            catch (Exception e) {

                e.printStackTrace();

            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    // =========================
    // CLIENT LOGIN SYNC TIMER
    // =========================
    public static int getRemainingTime() {

        return remainingTime;

    }

    // =========================
    // SEND ITEMS TO 1 CLIENT
    // =========================
    public static void sendAuctionDataTo(ClientHandler client) {

        try {

            String data =
                    AuctionManager
                            .getInstance()
                            .getAllItems();

            client.sendMessage(

                    gson.toJson(

                            new Request(
                                    "UPDATE_AUCTION",
                                    data
                            )
                    )
            );

        }

        catch (Exception e) {

            e.printStackTrace();

        }
    }

    // =========================
    // BROADCAST ALL CLIENTS
    // =========================
    public static void broadcast(String jsonMessage) {

        for (ClientHandler client : activeClients) {

            client.sendMessage(jsonMessage);

        }

    }
}   