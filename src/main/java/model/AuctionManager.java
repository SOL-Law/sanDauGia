package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionManager {
    // Công cụ hẹn giờ siêu chuẩn của Java
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private static AuctionManager instance;

    // thread-safe storage: Lưu trữ đồ vật đang đấu giá
    private final Map<Integer, BidInfo> items = new ConcurrentHashMap<>();

    // auto-increment ID
    private final AtomicInteger idCounter = new AtomicInteger(0);

    private AuctionManager() {
        System.out.println(" AuctionManager initialized");
    }

    // =========================
    // SINGLETON
    // =========================
    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // =========================
    // ADD ITEM (UPLOAD)
    // =========================
    public synchronized void addItem(String name, int startPrice, String base64Image, int duration) {
        int newId = idCounter.incrementAndGet();
        items.put(newId, new BidInfo(name, startPrice, "None", base64Image, duration));
        System.out.println(" ADD ITEM: " + name + " | " + startPrice + " | Timer: " + duration + "s");
    }

    // Hàm nạp từ DB lúc bật Server (Mặc định 60s để tương thích với code cũ)
    public synchronized void addItem(String name, int startPrice, String base64Image) {
        addItem(name, startPrice, base64Image, 60);
    }

    // =========================
    // HÀM 1: Bắt đầu đếm ngược (Gọi lúc đăng sản phẩm)
    // =========================
    public void startAuctionTimer(String itemName, int durationInSeconds) {
        System.out.println(" Đã lên lịch kết thúc [" + itemName + "] sau " + durationInSeconds + " giây.");

        // Hẹn giờ: Đúng X giây sau, chạy hàm endAuction()
        scheduler.schedule(() -> {
            endAuction(itemName);
        }, durationInSeconds, TimeUnit.SECONDS);
    }

    // =========================
    // HÀM 2: Gõ búa kết thúc (ĐÃ FIX LỖI GỘP CHUNG)
    // =========================
    public synchronized void endAuction(String itemName) {
        System.out.println("--- KẾT THÚC ĐẤU GIÁ: " + itemName + " ---");

        // 1. Tìm món đồ trong RAM
        model.BidInfo targetItem = null;
        Integer targetKey = null; // Cần giữ lại chìa khóa (Key) để lát nữa xóa đồ

        for (Map.Entry<Integer, BidInfo> entry : items.entrySet()) {
            if (entry.getValue().getItem().equals(itemName)) {
                targetItem = entry.getValue();
                targetKey = entry.getKey();
                break;
            }
        }

        if (targetItem == null) return; // Không thấy thì thôi

        String winner = targetItem.getLeader();
        int finalPrice = targetItem.getCurrentPrice();

        try {
            // 2. Chốt sổ trên Database: Cập nhật thành FINISHED
            server.dao.ItemDao.updateItemStatus(itemName, "FINISHED");

            // 3. Trừ tiền người thắng (Nếu có người mua)
            if (!winner.equals("None")) {
                server.dao.UserDao.payForItem(winner, finalPrice);
                System.out.println(" Đã bán [" + itemName + "] cho " + winner + " giá " + finalPrice);
            } else {
                System.out.println(" [" + itemName + "] không ai mua!");
            }

        } catch (Exception e) {
            System.out.println(" Lỗi khi xử lý Database (nhưng vẫn sẽ xóa đồ): " + e.getMessage());
        } finally {
            // 4. LUÔN LUÔN XÓA ĐỒ KHỎI RAM (Dù Database có lỗi hay không)
            if (targetKey != null) {
                items.remove(targetKey);
            }

            // 5. Loan báo cho toàn bộ Client biết để gỡ tranh xuống
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String data = this.getAllItems();
            String jsonMessage = gson.toJson(new network.Request("UPDATE_AUCTION", data));
            server.AuctionServer.broadcast(jsonMessage);

            // 6. (Tùy chọn) Gửi thông báo Pop-up cho cả làng biết ai thắng
            String msg = "Phiên đấu giá [" + itemName + "] đã kết thúc! Người thắng: " + winner;
            server.AuctionServer.broadcast(gson.toJson(new network.Request("NOTIFY", msg)));
        }
    }

    // =========================
    // PLACE BID
    // =========================
    public synchronized boolean placeBid(String itemName, int price, String user) {
        for (BidInfo bid : items.values()) {
            if (bid.getItem().equals(itemName)) {
                if (price > bid.getCurrentPrice()) {
                    bid.setCurrentPrice(price);
                    bid.setLeader(user);

                    System.out.println(" BID SUCCESS: " + itemName + " -> " + price + " (" + user + ")");
                    return true;
                }
                System.out.println(" BID FAIL (price too low)");
                return false;
            }
        }
        System.out.println(" BID FAIL (item not found)");
        return false;
    }

    // =========================
    // GET ALL ITEMS (CLIENT UI)
    // =========================
    public synchronized String getAllItems() {
        StringBuilder sb = new StringBuilder();
        for (BidInfo b : items.values()) {
            sb.append(b.getItem()).append("|")
                    .append(b.getCurrentPrice()).append("|")
                    .append(b.getLeader()).append("|")
                    .append(b.getBase64Image()).append("|")
                    .append(b.getRemainingTime()).append(";"); // Nối thêm thời gian vào đuôi
        }
        return sb.toString();
    }

    // =========================
    // DEBUG PRINT
    // =========================
    public synchronized void printAllItems() {
        System.out.println("===== ITEM LIST =====");
        for (BidInfo bid : items.values()) {
            System.out.println(bid.getItem() + " | " + bid.getCurrentPrice() + " | " + bid.getLeader());
        }
    }
}