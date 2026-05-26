package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import model.AutoBid;
import java.util.PriorityQueue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class AuctionManager {
    // Công cụ hẹn giờ siêu chuẩn của Java
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, java.util.concurrent.ScheduledFuture<?>> auctionTimers = new ConcurrentHashMap<>();
    private static AuctionManager instance;

    // thread-safe storage: Lưu trữ đồ vật đang đấu giá
    private final Map<Integer, BidInfo> items = new ConcurrentHashMap<>();

    // auto-increment ID
    private final AtomicInteger idCounter = new AtomicInteger(0);

    
// 1. Thêm cấu trúc dữ liệu lưu danh sách AutoBid toàn hệ thống công khai
    private final List<AutoBid> autoBidsList = new CopyOnWriteArrayList<>();

    // 2. Hàm đăng ký AutoBid (Thread-safe)
    public synchronized void registerAutoBid(AutoBid newAutoBid) {
        // Nếu user đã đăng ký AutoBid cho sản phẩm này trước đó, xóa cấu hình cũ đi để ghi đè cấu hình mới
        autoBidsList.removeIf(ab -> ab.getUsername().equals(newAutoBid.getUsername())
                && ab.getItemName().equals(newAutoBid.getItemName()));
        autoBidsList.add(newAutoBid);
        System.out.println("🤖 ĐĂNG KÝ AUTO-BID: User [" + newAutoBid.getUsername() + "] sản phẩm [" + newAutoBid.getItemName() + "]");
    }

    // 3. THUẬT TOÁN ĐẤU GIÁ TỰ ĐỘNG CHÍNH (Sử dụng PriorityQueue theo yêu cầu đề bài)
    public void executeAutoBidding(String itemName) {
        boolean insideWar;
        do {
            insideWar = false;

            // A. Lấy thông tin giá hiện tại của món đồ trong RAM
            BidInfo info = null;
            for (BidInfo b : items.values()) {
                if (b.getItem().equals(itemName)) {
                    info = b;
                    break;
                }
            }
            if (info == null) return;

            int currentPrice = info.getCurrentPrice();
            String currentLeader = info.getLeader();

            // B. Khởi tạo PriorityQueue xếp theo thời gian đăng ký tăng dần (Ai đăng ký trước ưu tiên trước)
            PriorityQueue<AutoBid> queue = new PriorityQueue<>(
                    (a, b) -> Long.compare(a.getRegisteredTime(), b.getRegisteredTime())
            );

            // C. Lọc ra các AutoBid hợp lệ: Đúng sản phẩm, không phải người đang dẫn đầu, và maxBid > giá hiện tại
            for (AutoBid ab : autoBidsList) {
                if (ab.getItemName().equals(itemName)
                        && !ab.getUsername().equals(currentLeader)
                        && ab.getMaxBid() > currentPrice) {
                    queue.add(ab);
                }
            }

            // D. Thử kích hoạt lệnh đặt giá của ứng viên ưu tiên cao nhất
            while (!queue.isEmpty()) {
                AutoBid candidate = queue.poll();
                int nextPrice = currentPrice + candidate.getIncrement();

                // Kiểm tra xem bước giá mới có vượt ngưỡng chịu đựng (maxBid) của người dùng không
                if (nextPrice <= candidate.getMaxBid()) {
                    // Tiến hành tự động gọi hàm đặt giá gốc
                    if (this.placeBid(itemName, nextPrice, candidate.getUsername())) {
                        // Lưu luôn lịch sử vào database từng bước để vẽ đồ thị Price Curve mượt mà
                        server.dao.ItemDao.insertBidHistory(itemName, candidate.getUsername(), nextPrice);
                        insideWar = true; // Đặt giá thành công, kích hoạt tiếp vòng kiểm tra sau
                        break;
                    }
                }
            }
        } while (insideWar); // Lặp lại vòng đấu cho đến khi không còn bot nào nâng giá được nữa
    }

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

                        System.out.println(" ANTI-SNIPING TRIGGERED: Phiên đấu giá [" + itemName + "] được kéo dài thêm 1 phút!");
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

        //  HỦY LUÔN TIMER ĐANG CHẠY NGẦM CỦA MÓN ĐỒ NÀY
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
    // SỬA TÊN SẢN PHẨM TRÊN HỆ THỐNG (ĐÃ FIX LỖI TIMER)
    // =========================================
    public synchronized void editItemInSystem(String oldName, String newName) {
        // 1. Sửa trên RAM Server và cập nhật lại luồng Timer đếm ngược
        for (java.util.Map.Entry<Integer, BidInfo> entry : items.entrySet()) {
            BidInfo bid = entry.getValue();
            if (bid.getItem().equals(oldName)) {
                bid.setItem(newName);

                //  Tính toán số giây còn lại chính xác của phiên này
                long elapsedSeconds = (System.currentTimeMillis() - bid.getStartTime()) / 1000;
                int timeLeft = (int) Math.max(0, bid.getDuration() - elapsedSeconds);

                // Hủy bộ đếm giờ cũ đang chạy ngầm của cái tên cũ (oldName) để giải phóng luồng
                java.util.concurrent.ScheduledFuture<?> oldTimer = auctionTimers.remove(oldName);
                if (oldTimer != null) {
                    oldTimer.cancel(false);
                }

                // Kích hoạt bộ đếm giờ mới gắn liền với tên mới (newName), chạy nốt thời gian còn lại
                this.startAuctionTimer(newName, timeLeft);
                break;
            }
            for (AutoBid ab : autoBidsList) {
                if (ab.getItemName().equals(oldName)) {
                    ab.setItemName(newName);
                }
            }
        }

        // 2. Sửa dưới Database
        server.dao.ItemDao.updateItemDetails(oldName, newName);

        // 3. Phát loa cập nhật cho tất cả Client đang online thấy tên mới ngay lập tức
        String data = this.getAllItems();
        server.AuctionServer.broadcast(new com.google.gson.Gson().toJson(new network.Request("UPDATE_AUCTION", data)));
    }
    // =========================================
    // KHÔI PHỤC ĐỒ VẬT TỪ DATABASE LÊN RAM (ADMIN)
    // =========================================
    public synchronized String restoreItemFromDB(String itemName, int duration) {
        for (BidInfo bid : items.values()) {
            if (bid.getItem().equals(itemName)) return "Đồ vật này hiện vẫn đang trên sàn đấu giá rồi!";
        }

        java.util.Map<String, String> info = server.dao.ItemDao.getItemForRestore(itemName);
        if (info.isEmpty()) return "Không tìm thấy đồ vật nào tên này, hoặc lỗi DB!";

        int price = Integer.parseInt(info.get("price"));
        String image = info.get("image");
        String seller = info.get("seller");
        String category = info.get("category");
        String leader = info.get("leader"); //  Hứng leader

        //  Gọi hàm loadItemFromDB (có leader) thay vì hàm addItem cũ
        this.loadItemFromDB(itemName, price, image, duration, seller, category, leader);
        this.startAuctionTimer(itemName, duration);

        return "SUCCESS";
    }
    // ==========================================
    // HÀM MỚI: NẠP ĐỒ TỪ DB CÓ KÈM THEO NGƯỜI DẪN ĐẦU (LEADER)
    // ==========================================
    public synchronized void loadItemFromDB(String name, int startPrice, String base64Image, int duration, String seller, String category, String leader) {
        int newId = idCounter.incrementAndGet();
        long startTime = System.currentTimeMillis();
        // Truyền chuẩn xác leader vào BidInfo thay vì chữ "None"
        items.put(newId, new BidInfo(name, startPrice, leader, base64Image, startTime, duration, seller, category));
    }
}