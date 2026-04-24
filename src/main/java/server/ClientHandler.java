package server;

import com.google.gson.Gson;
import network.Request;
import server.dao.UserDao;
import model.AuctionManager;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );
            AuctionServer.sendAuctionDataTo(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {

            String line;

            while ((line = in.readLine()) != null) {

                System.out.println("Client gửi: " + line);

                Request req =
                        gson.fromJson(
                                line,
                                Request.class
                        );

                String type =
                        req.getType();

                String payload =
                        req.getPayload();

                switch (type) {

                    case "LOGIN":
                        handleLogin(payload);
                        break;

                    case "REGISTER":
                        handleRegister(payload);
                        break;

                    case "START_SESSION":

                        AuctionServer
                                .startAuctionTimer(60);

                        break;

                    case "PLACE_BID":
                        handleBid(payload);
                        break;

                    case "UPLOAD_ITEM":
                        handleUpload(payload);
                        break;
                }
            }

        } catch (Exception e) {

            System.out.println("Client disconnected");

        }
    }

    // =========================
    // LOGIN (FIX CHÍNH Ở ĐÂY)
    // =========================
    private void handleLogin(String payload) {

        try {

            var obj =
                    gson.fromJson(
                            payload,
                            Map.class
                    );

            String username =
                    (String) obj.get("username");

            String password =
                    (String) obj.get("password");

            String role =
                    UserDao.login(
                            username,
                            password
                    );

            if (role != null) {

                Map<String, String> res =
                        new HashMap<>();

                res.put("role", role);

                String jsonPayload =
                        gson.toJson(res);

                sendResponse(
                        "LOGIN_SUCCESS",
                        jsonPayload
                );

                // 🔥 FIX QUAN TRỌNG NHẤT
                sendAuctionData();

                // 🔥 gửi timer luôn
                sendTimer();

            }

            else {

                sendResponse(
                        "LOGIN_FAIL",
                        "Sai tài khoản hoặc mật khẩu"
                );
            }

        }

        catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    "ERROR",
                    "Lỗi server"
            );
        }
    }

    // =========================
    // REGISTER
    // =========================
    private void handleRegister(String payload) {

        try {

            var obj =
                    gson.fromJson(
                            payload,
                            Map.class
                    );

            String username =
                    (String) obj.get("username");

            String password =
                    (String) obj.get("password");

            boolean ok =
                    UserDao.register(
                            username,
                            password
                    );

            if (ok) {

                sendResponse(
                        "REGISTER_SUCCESS",
                        "OK"
                );
            }

            else {

                sendResponse(
                        "REGISTER_FAIL",
                        "Tài khoản đã tồn tại"
                );
            }

        }

        catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    "ERROR",
                    "Lỗi đăng ký"
            );
        }
    }

    // =========================
    // BID
    // =========================
    private void handleBid(String payload) {

        var obj =
                gson.fromJson(
                        payload,
                        Map.class
                );

        String item =
                (String) obj.get("item");

        double price =
                (double) obj.get("price");

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.placeBid(
                item,
                (int) price,
                "User"
        );

        String data =
                manager.getAllItems();

        AuctionServer.broadcast(

                gson.toJson(

                        new Request(
                                "UPDATE_AUCTION",
                                data
                        )
                )
        );
    }

    // =========================
    // UPLOAD ITEM
    // =========================
    private void handleUpload(String payload) {

        var obj =
                gson.fromJson(
                        payload,
                        Map.class
                );

        String name =
                (String) obj.get("name");

        double price =
                (double) obj.get("price");

        AuctionManager manager =
                AuctionManager.getInstance();

        manager.addItem(
                name,
                (int) price
        );

        String data =
                manager.getAllItems();

        AuctionServer.broadcast(

                gson.toJson(

                        new Request(
                                "UPDATE_AUCTION",
                                data
                        )
                )
        );
    }

    // =========================
    // GỬI ITEM KHI LOGIN XONG
    // =========================
    private void sendAuctionData() {

        String data =
                AuctionManager
                        .getInstance()
                        .getAllItems();

        sendResponse(
                "UPDATE_AUCTION",
                data
        );
    }

    // =========================
    // GỬI TIMER KHI LOGIN XONG
    // =========================
    private void sendTimer() {

        int time =
                AuctionServer
                        .getRemainingTime();

        sendResponse(
                "UPDATE_TIMER",
                String.valueOf(time)
        );
    }

    // =========================
    // HELPER SEND JSON
    // =========================
    private void sendResponse(
            String type,
            String payload
    ) {

        Request res =
                new Request(
                        type,
                        payload
                );

        String json =
                gson.toJson(res);

        out.println(json);

        System.out.println(
                "Server trả: "
                        + json
        );
    }

    public void sendMessage(String msg) {

        out.println(msg);

    }
}