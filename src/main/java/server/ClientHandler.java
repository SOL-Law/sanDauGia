package server;

import com.google.gson.Gson;
import network.Request;
import server.controller.AuctionController;
import server.controller.UserController;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
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

                Request req = gson.fromJson(line, Request.class);
                String type = req.getType();
                String payload = req.getPayload();

                // KHÔNG CODE LOGIC Ở ĐÂY NỮA, CHỈ ĐỊNH TUYẾN (ROUTING) ĐẾN CÁC CONTROLLER
                switch (type) {
                    // --- NHÓM USER CONTROLLER ---
                    case "LOGIN":             UserController.handleLogin(this, payload); break;
                    case "REGISTER":          UserController.handleRegister(this, payload); break;
                    case "GET_BALANCE":       UserController.handleGetBalance(this, payload); break;
                    case "DEPOSIT":           UserController.handleDeposit(this, payload); break;
                    case "GET_PROFILE":       UserController.handleGetProfile(this, payload); break;
                    case "UPDATE_PROFILE":    UserController.handleUpdateProfile(this, payload); break;

                    // --- NHÓM AUCTION CONTROLLER ---
                    case "PLACE_BID":         AuctionController.handleBid(this, payload); break;
                    case "UPLOAD_ITEM":       AuctionController.handleUpload(this, payload); break;
                    case "GET_AUCTION":       AuctionController.sendAuctionData(this); break;
                    case "GET_MY_HISTORY":    AuctionController.handleGetMyHistory(this, payload); break;
                    case "GET_CHART":         AuctionController.handleGetChart(this, payload); break;
                    case "DELETE_ITEM":       AuctionController.handleDeleteItem(this, payload); break;
                    case "EDIT_ITEM":         AuctionController.handleEditItem(this, payload); break;
                    case "REGISTER_AUTO_BID": AuctionController.handleRegisterAutoBid(this, payload); break;
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
            AuctionServer.activeClients.remove(this);
        }
    }

    // =========================
    // HÀM HELPER ĐỂ GỬI DATA VỀ CLIENT (Public để Controller dùng)
    // =========================
    public synchronized void sendResponse(String type, String payload) {
        Request res = new Request(type, payload);
        String json = gson.toJson(res);
        out.println(json);
        System.out.println("Server trả: " + json);
    }

    public synchronized void sendMessage(String msg) {
        out.println(msg);
        out.flush();
    }
}