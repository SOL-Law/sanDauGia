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
    private final Map<String, java.util.concurrent.ScheduledFuture<?>> auctionTimers = new ConcurrentHashMap<>();
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

    // ==========================================
    // 1. ADD ITEM (UPLOAD TỪ CLIENT GỬI LÊN)
    // ==========================================
    public synchronized void addItem(String name, int startPrice, String base64Image, int duration, String seller, String category) {
        int newId = idCounter.incrementAndGet();
        long startTime = System.currentTimeMillis(); // Lấy mốc thời gian ngay lúc đăng

        // 🔥 Truyền chuẩn xác và đầy đủ 8 tham số vào BidInfo
        items.put(newId, new BidInfo(name, startPrice, "None", base64Image, startTime, duration, seller, category));

        System.out.println(" ADD ITEM: " + name + " | Giá: " + startPrice + " | Bán bởi: " + seller);
    }

    // ==========================================
    // 2. NẠP TỪ DATABASE LÚC BẬT SERVER (Hàm dự phòng để code cũ không bị lỗi)
    // ==========================================
    public synchronized void addItem(String name, int startPrice, String base64Image) {
        int newId = idCounter.incrementAndGet();
        long startTime = System.currentTimeMillis();

        // Nếu load từ DB cũ lên thì cho mặc định 60s, người bán 'admin', mục 'Khác'
        items.put(newId, new BidInfo(name, startPrice, "None", base64Image, startTime, 60, "admin", "Khác"));
    }

    // ==========================================
    // 3. GET ALL ITEMS (Đã fix lỗi getRemainingTime)
    // ==========================================
    public synchronized String getAllItems() {
        StringBuilder sb = new StringBuilder();
        for (BidInfo bid : items.values()) {

            //  Sửa thành getDuration()
            long elapsedSeconds = (System.currentTimeMillis() - bid.getStartTime()) / 1000;
            long timeLeft = Math.max(0, bid.getDuration() - elapsedSeconds);

            // Nối thêm seller và category vào chuỗi bằng dấu |
            sb.append(bid.getItem()).append("|")
                    .append(bid.getCurrentPrice()).append("|")
                    .append(bid.getLeader()).append("|")
                    .append(bid.getBase64Image()).append("|")
                    .append(timeLeft).append("|")
                    .append(bid.getSeller()).append("|")
                    .append(bid.getCategory()).append(";");
        }
        return sb.toString();
    }


    // =========================
    // HÀM 1: Bắt đầu đếm ngược (Gọi lúc đăng sản phẩm)
    // =========================
    // =========================
    // HÀM 1: Bắt đầu đếm ngược (Đã sửa lỗi Thread-safe)
    // =========================
    public void startAuctionTimer(String itemName, int durationInSeconds) {
        // Lấy và xóa thẳng timer cũ ra một cách an toàn, tránh NullPointerException
        java.util.concurrent.ScheduledFuture<?> oldTimer = auctionTimers.remove(itemName);
        if (oldTimer != null) {
            oldTimer.cancel(false); // Hủy luồng cũ thành công
        }

        var future = scheduler.schedule(() -> {
            endAuction(itemName);
            auctionTimers.remove(itemName);
        }, durationInSeconds, TimeUnit.SECONDS);

        auctionTimers.put(itemName, future);
    }

    // =========================
    // HÀM 2: Gõ búa kết thúc
    // =========================
    public synchronized void endAuction(String itemName) {
        System.out.println("--- KẾT THÚC ĐẤU GIÁ: " + itemName + " ---");

        // 1. Tìm món đồ trong RAM
        model.BidInfo targetItem = null;
        Integer targetKey = null;

        for (Map.Entry<Integer, BidInfo> entry : items.entrySet()) {
            if (entry.getValue().getItem().equals(itemName)) {
                targetItem = entry.getValue();
                targetKey = entry.getKey();
                break;
            }
        }

        if (targetItem == null) return;

        String winner = targetItem.getLeader();

        try {

            server.dao.ItemDao.endAuctionAndSettlePayment(itemName);

        } catch (Exception e) {
            System.out.println(" Lỗi khi xử lý Database: " + e.getMessage());
        } finally {
            // LUÔN LUÔN XÓA ĐỒ KHỎI RAM (Dù Database có lỗi hay không)
            if (targetKey != null) {
                items.remove(targetKey);
            }

            // Loan báo cho toàn bộ Client biết để gỡ tranh xuống
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String data = this.getAllItems();
            String jsonMessage = gson.toJson(new network.Request("UPDATE_AUCTION", data));
            server.AuctionServer.broadcast(jsonMessage);

            // Gửi thông báo Pop-up cho cả làng biết
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
                    // Thêm đoạn này vào bên trong block "if (price > bid.getCurrentPrice())" trước khi return true:
                    long elapsedSeconds = (System.currentTimeMillis() - bid.getStartTime()) / 1000;
                    long timeLeft = bid.getDuration() - elapsedSeconds;

                    if (timeLeft <= 30) { // 1. Nếu thời gian còn lại dưới hoặc bằng 30 giây
                        // 2. Đặt tổng thời gian mới = thời gian đã trôi qua + 60 giây (đồng nghĩa với việc còn đúng 60 giây nữa mới hết giờ)
                        int newDuration = (int) (elapsedSeconds + 60);
                        bid.setDuration(newDuration);

                        // 3. Hủy timer cũ và lên lịch lại đúng 60 giây (1 phút) nữa mới đóng phiên
                        startAuctionTimer(itemName, 60);

                        System.out.println("🔥 ANTI-SNIPING TRIGGERED: Phiên đấu giá [" + itemName + "] được kéo dài thêm 1 phút!");
                    }
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
    // DEBUG PRINT
    // =========================
    public synchronized void printAllItems() {
        System.out.println("===== ITEM LIST =====");
        for (BidInfo bid : items.values()) {
            System.out.println(bid.getItem() + " | " + bid.getCurrentPrice() + " | " + bid.getLeader());
        }
    }
    // =========================================
    // XÓA SẢN PHẨM KHỎI HỆ THỐNG
    // =========================================
    // =========================================
    // XÓA SẢN PHẨM KHỎI HỆ THỐNG (Đã bổ sung hủy Timer)
    // =========================================
    public synchronized void deleteItemFromSystem(String itemName) {
        // 1. Xóa khỏi RAM Server
        items.values().removeIf(it -> it.getItem().equals(itemName));

        // 🔥 HỦY LUÔN TIMER ĐANG CHẠY NGẦM CỦA MÓN ĐỒ NÀY
        java.util.concurrent.ScheduledFuture<?> oldTimer = auctionTimers.remove(itemName);
        if (oldTimer != null) {
            oldTimer.cancel(false);
        }

        // 2. Xóa khỏi Database
        server.dao.ItemDao.deleteItem(itemName);
        // 3. Phát loa cập nhật cho tất cả các máy Client
        String data = this.getAllItems();
        server.AuctionServer.broadcast(new com.google.gson.Gson().toJson(new network.Request("UPDATE_AUCTION", data)));
    }

    // =========================================
    // SỬA TÊN SẢN PHẨM TRÊN HỆ THỐNG
    // =========================================
    public synchronized void editItemInSystem(String oldName, String newName) {
        // 1. Sửa trên RAM Server
        for (java.util.Map.Entry<Integer, BidInfo> entry : items.entrySet()) {
            BidInfo bid = entry.getValue();

            if (bid.getItem().equals(oldName)) {
                bid.setItem(newName); // Chỉ cập nhật mỗi cái tên!
                break;
            }
        }

        // 2. Sửa dưới Database
        server.dao.ItemDao.updateItemDetails(oldName, newName);

        // 3. Phát loa cập nhật cho tất cả Client
        String data = this.getAllItems();
        server.AuctionServer.broadcast(new com.google.gson.Gson().toJson(new network.Request("UPDATE_AUCTION", data)));
    }
}