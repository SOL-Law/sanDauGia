package model;

import model.item.*;
import model.user.User;
import java.time.LocalDateTime;
import java.util.*;

public class AuctionManager {
    private static AuctionManager instance;
    private Map<Integer, Auction> auctions = new HashMap<>();
    private boolean isRunning = true;

    private AuctionManager() {
        // Khởi tạo vài phiên mẫu với các loại Item cụ thể
        auctions.put(1, new Auction(1,
                new Electronics(1, "Laptop Gaming", 1000, "Laptop cấu hình cao", "Dell", 24),
                LocalDateTime.now().plusSeconds(30)));

        auctions.put(2, new Auction(2,
                new Vehicle(2, "Xe máy", 500, "Xe tay ga", "Xe máy", "125cc"),
                LocalDateTime.now().plusSeconds(45)));

        auctions.put(3, new Auction(3,
                new Art(3, "Tranh sơn dầu", 300, "Tác phẩm nghệ thuật", "Nguyễn Văn A", "Sơn dầu"),
                LocalDateTime.now().plusSeconds(60)));
    }
    // Trong AuctionManager.java
    public synchronized Auction getAuctionById(int id) {
        return auctions.get(id);
    }


    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // Đặt giá cho phiên cụ thể
    public synchronized boolean placeBid(int auctionId, User bidder, double price) {
        if (!isRunning) return false;
        Auction auction = auctions.get(auctionId);
        if (auction != null) {
            return auction.placeBid(bidder, price);
        }
        return false;
    }

    // Thêm phiên mới với Item bất kỳ
    public synchronized void addItem(Item item, LocalDateTime endTime) {
        int newId = auctions.size() + 1;
        auctions.put(newId, new Auction(newId, item, endTime));
        System.out.println("Thêm sản phẩm: " + item.getName() + " | Giá khởi điểm: " + item.getStartPrice());
    }

    // Trả về chuỗi để dễ gửi qua socket
    public synchronized String getAllItems() {
        StringBuilder sb = new StringBuilder();
        for (Auction a : auctions.values()) {
            BidInfo info = new BidInfo(
                    a.getItem().getName(),
                    (int) a.getCurrentHighestBid(),
                    a.getHighestBidder() != null ? a.getHighestBidder().getUsername() : "none"
            );
            sb.append(info.toString()).append("\n");
        }
        return sb.toString();
    }


    public synchronized void endAuction() { isRunning = false; }

    public synchronized void startNewSession() {
        this.isRunning = true;
        System.out.println("Quản lý đã khởi động lại phiên mới!");
    }

    public synchronized boolean isRunning() { return isRunning; }
}
