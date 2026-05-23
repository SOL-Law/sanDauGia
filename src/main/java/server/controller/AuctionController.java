package server.controller;

import com.google.gson.Gson;
import network.Request;
import server.AuctionServer;
import server.ClientHandler;
import model.AuctionManager;
import java.util.Map;

public class AuctionController {
    private static Gson gson = new Gson();

    public static void handleBid(ClientHandler client, String payload) {
        var obj = gson.fromJson(payload, Map.class);
        String item = (String) obj.get("item");
        double price = (double) obj.get("price");
        String username = (String) obj.get("username");

        //  1. LẤY TỔNG TIỀN VÀ TIỀN ĐANG BỊ ĐÓNG BĂNG Ở PHIÊN KHÁC
        double currentBalance = server.dao.UserDao.getBalance(username);
        double lockedBalance = server.dao.UserDao.getLockedBalance(username);
        double availableBalance = currentBalance - lockedBalance;

        //  2. KIỂM TRA SỐ DƯ KHẢ DỤNG
        if (availableBalance < price) {
            String msg = String.format(" Thất bại! Bạn đang bị tạm giữ %,.0f VNĐ ở các phiên khác. Số dư khả dụng không đủ!", lockedBalance);
            client.sendResponse("NOTIFY", msg);
            return;
        }

        AuctionManager manager = AuctionManager.getInstance();
        if (manager.placeBid(item, (int) price, username)) {
            manager.executeAutoBidding(item);
            String data = manager.getAllItems();
            AuctionServer.broadcast(gson.toJson(new Request("UPDATE_AUCTION", data)));
            server.dao.ItemDao.insertBidHistory(item, username, (int) price);
        } else {
            client.sendResponse("NOTIFY", " Đấu giá thất bại! Vui lòng đặt giá cao hơn giá hiện tại.");
        }
    }

    public static void handleUpload(ClientHandler client, String payload) {
        try {
            var obj = gson.fromJson(payload, java.util.Map.class);
            String name = (String) obj.get("name");
            double price = Double.parseDouble(obj.get("price").toString());
            String base64Image = (String) obj.get("image");
            String seller = (String) obj.get("username");
            String category = (String) obj.get("category");

            int duration = obj.containsKey("time") ? (int) Double.parseDouble(obj.get("time").toString()) : 60;

            server.dao.ItemDao.insertItem(name, (int) price, base64Image, seller, category);
            AuctionManager manager = AuctionManager.getInstance();
            manager.addItem(name, (int) price, base64Image, duration, seller, category);
            manager.startAuctionTimer(name, duration);

            String data = manager.getAllItems();
            AuctionServer.broadcast(gson.toJson(new Request("UPDATE_AUCTION", data)));
        } catch (Exception e) {
            System.out.println("Lỗi xử lý Upload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendAuctionData(ClientHandler client) {
        String data = AuctionManager.getInstance().getAllItems();
        client.sendResponse("UPDATE_AUCTION", data);
    }

    public static void handleGetMyHistory(ClientHandler client, String payload) {
        String myHistoryData = server.dao.ItemDao.getPersonalHistory(payload);
        client.sendResponse("HISTORY_DATA", myHistoryData);
    }

    public static void handleGetChart(ClientHandler client, String payload) {
        String chartData = server.dao.ItemDao.getChartData(payload);
        client.sendResponse("CHART_DATA", payload + "|" + chartData);
    }

    public static void handleDeleteItem(ClientHandler client, String payload) {
        AuctionManager.getInstance().deleteItemFromSystem(payload);
    }

    public static void handleEditItem(ClientHandler client, String payload) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
            String oldName = json.get("oldName").getAsString();
            String newName = json.get("newName").getAsString();
            AuctionManager.getInstance().editItemInSystem(oldName, newName);
        } catch (Exception ex) {
            System.out.println("Lỗi nội bộ khi sửa tên: " + ex.getMessage());
        }
    }

    public static void handleRegisterAutoBid(ClientHandler client, String payload) {
        try {
            com.google.gson.JsonObject autoPayload = gson.fromJson(payload, com.google.gson.JsonObject.class);
            String autoItem = autoPayload.get("item").getAsString();
            int maxBid = autoPayload.get("maxBid").getAsInt();
            int increment = autoPayload.get("increment").getAsInt();
            String autoUser = autoPayload.get("username").getAsString();

            model.AutoBid newAuto = new model.AutoBid(autoUser, autoItem, maxBid, increment);
            AuctionManager.getInstance().registerAutoBid(newAuto);
            client.sendResponse("NOTIFY", " Đã kích hoạt Auto-Bid thành công cho mặt hàng: " + autoItem);

            AuctionManager.getInstance().executeAutoBidding(autoItem);
            String freshData = AuctionManager.getInstance().getAllItems();
            AuctionServer.broadcast(gson.toJson(new Request("UPDATE_AUCTION", freshData)));
        } catch (Exception ex) {
            System.out.println("Lỗi xử lý REGISTER_AUTO_BID: " + ex.getMessage());
        }
    }
    public static void handleRestoreItem(ClientHandler client, String payload) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
            String itemName = json.get("itemName").getAsString();
            int duration = json.get("duration").getAsInt();

            String result = AuctionManager.getInstance().restoreItemFromDB(itemName, duration);
            if (result.equals("SUCCESS")) {
                client.sendResponse("NOTIFY", " Khôi phục thành công! Đồ vật đã quay lại sàn đấu giá.");

                // Phát loa cập nhật giao diện cho tất cả Client đang online
                String data = AuctionManager.getInstance().getAllItems();
                server.AuctionServer.broadcast(gson.toJson(new Request("UPDATE_AUCTION", data)));
            } else {
                client.sendResponse("NOTIFY", " Lỗi: " + result);
            }
        } catch (Exception ex) {
            System.out.println("Lỗi xử lý RESTORE_ITEM: " + ex.getMessage());
        }
    }
}