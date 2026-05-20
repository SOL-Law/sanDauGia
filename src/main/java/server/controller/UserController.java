package server.controller;

import com.google.gson.Gson;
import server.ClientHandler;
import server.dao.UserDao;
import java.util.HashMap;
import java.util.Map;

public class UserController {
    private static Gson gson = new Gson();

    public static void handleLogin(ClientHandler client, String payload) {
        try {
            var obj = gson.fromJson(payload, Map.class);
            String username = (String) obj.get("username");
            String password = (String) obj.get("password");

            String role = UserDao.login(username, password);

            if (role != null) {
                Map<String, String> res = new HashMap<>();
                res.put("role", role);
                client.sendResponse("LOGIN_SUCCESS", gson.toJson(res));
                // Vừa login xong thì gửi danh sách đồ cho người ta xem
                AuctionController.sendAuctionData(client);
            } else {
                client.sendResponse("LOGIN_FAIL", "Sai tài khoản hoặc mật khẩu");
            }
        } catch (Exception e) {
            e.printStackTrace();
            client.sendResponse("ERROR", "Lỗi server");
        }
    }

    public static void handleRegister(ClientHandler client, String payload) {
        try {
            var obj = gson.fromJson(payload, Map.class);
            String username = (String) obj.get("username");
            String password = (String) obj.get("password");
            String role = (String) obj.get("role");

            if (role == null || role.trim().isEmpty()) {
                role = "BIDDER";
            }

            if (UserDao.register(username, password, role)) {
                client.sendResponse("REGISTER_SUCCESS", "OK");
            } else {
                client.sendResponse("REGISTER_FAIL", "Tài khoản đã tồn tại");
            }
        } catch (Exception e) {
            e.printStackTrace();
            client.sendResponse("ERROR", "Lỗi đăng ký");
        }
    }

    public static void handleGetBalance(ClientHandler client, String payload) {
        try {
            double balance = UserDao.getBalance(payload.trim());
            client.sendResponse("UPDATE_BALANCE", String.valueOf(balance));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleDeposit(ClientHandler client, String payload) {
        try {
            var obj = gson.fromJson(payload, Map.class);
            String username = (String) obj.get("username");
            double amount = (double) obj.get("amount");

            if (UserDao.deposit(username, amount)) {
                double newBalance = UserDao.getBalance(username);
                client.sendResponse("UPDATE_BALANCE", String.valueOf(newBalance));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleGetProfile(ClientHandler client, String payload) {
        String userId = server.dao.UserDao.getUserId(payload);
        client.sendResponse("PROFILE_DATA", userId);
    }

    public static void handleUpdateProfile(ClientHandler client, String payload) {
        com.google.gson.JsonObject pObj = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
        String oldU = pObj.get("oldUser").getAsString();
        String newU = pObj.get("newUser").getAsString();
        String newP = pObj.get("newPass").getAsString();
        server.dao.UserDao.updateProfile(oldU, newU, newP);
    }
}