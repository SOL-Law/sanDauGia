package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import network.Request;
import server.dao.UserDao;
import model.user.User;
import model.AuctionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            String jsonReceived;

            // 🔥 Singleton AuctionManager
            AuctionManager manager = AuctionManager.getInstance();

            while ((jsonReceived = in.readLine()) != null) {

                Request req = gson.fromJson(jsonReceived, Request.class);
                System.out.println("[Client " + socket.getPort() + "] -> " + req.getAction());

                switch (req.getAction()) {

                    // =========================
                    // LOGIN
                    // =========================
                    case "LOGIN":
                        User userLogin = gson.fromJson(req.getPayload(), User.class);

                        if (UserDao.login(userLogin.getUsername(), userLogin.getPassword())) {
                            sendMessage(gson.toJson(new Request("LOGIN_SUCCESS", "")));
                        } else {
                            sendMessage(gson.toJson(new Request("ERROR", "Sai tài khoản!")));
                        }
                        break;

                    // =========================
                    // REGISTER
                    // =========================
                    case "REGISTER":
                        User userReg = gson.fromJson(req.getPayload(), User.class);

                        if (UserDao.register(userReg.getUsername(), userReg.getPassword())) {
                            sendMessage(gson.toJson(new Request("REGISTER_SUCCESS", "")));
                        } else {
                            sendMessage(gson.toJson(new Request("ERROR", "User đã tồn tại!")));
                        }
                        break;

                    // =========================
                    // LOAD DATA BAN ĐẦU
                    // =========================
                    case "GET_AUCTION":
                        String data = manager.getAllItems();
                        sendMessage(gson.toJson(new Request("AUCTION_UPDATE", data)));
                        break;

                    // =========================
                    // PLACE BID
                    // =========================
                    case "PLACE_BID":

                        // ❗ nếu phiên đã kết thúc
                        if (!manager.isRunning()) {
                            sendMessage(gson.toJson(
                                    new Request("ERROR", "Phiên đấu giá đã kết thúc!")
                            ));
                            break;
                        }

                        JsonObject obj = gson.fromJson(req.getPayload(), JsonObject.class);

                        String item = obj.get("item").getAsString();
                        int price = obj.get("price").getAsInt();

                        // giả lập user
                        String username = "user" + socket.getPort();

                        boolean success = manager.placeBid(item, price, username);

                        if (success) {

                            // 🔥 realtime broadcast cho toàn bộ client
                            String newData = manager.getAllItems();
                            Request res = new Request("AUCTION_UPDATE", newData);

                            AuctionServer.broadcast(gson.toJson(res));

                        } else {
                            sendMessage(gson.toJson(
                                    new Request("ERROR", "Giá phải cao hơn giá hiện tại!")
                            ));
                        }

                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Client " + socket.getPort() + " disconnect!");
        } finally {
            try {
                AuctionServer.activeClients.remove(this);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =========================
    // SEND MESSAGE
    // =========================
    public void sendMessage(String jsonMessage) {
        if (out != null) {
            out.println(jsonMessage);
        }
    }
}