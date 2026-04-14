package model;

import model.item.Item;
import model.user.User;
import java.util.*;

public class Auction extends Entity {
    private static final double MIN_BID_INCREMENT = 10_000;

    private Item item;
    private User highestBidder;
    private double currentHighestBid;
    private boolean isActive;

    // 🔥 thêm nhẹ
    private final Object lock = new Object();
    private List<Object> clients = new ArrayList<>(); // tạm để Object cho đỡ ảnh hưởng code cũ

    public Auction() {
        super();
    }

    private String validateBid(User bidder, double bidAmount) {
        if (bidder == null) return "Người dùng không hợp lệ";
        if (!isActive) return "Phiên đã kết thúc";
        if (highestBidder != null && bidder.equals(highestBidder))
            return "Bạn đang là người trả giá cao nhất";
        if (bidAmount < currentHighestBid + MIN_BID_INCREMENT)
            return "Phải tăng ít nhất " + MIN_BID_INCREMENT + " VNĐ";
        if (bidder.getBalance() < bidAmount)
            return "Không đủ tiền";

        return null;
    }

    public Auction(int id, Item item) {
        super(id);

        if (item == null) {
            throw new IllegalArgumentException("Item không được null.");
        }

        this.item = item;
        this.currentHighestBid = item.getStartPrice();
        this.highestBidder = null;
        this.isActive = true;
    }

    // =========================
    // ĐẶT GIÁ (đã fix thread-safe)
    // =========================
    public boolean placeBid(User bidder, double bidAmount) {

        synchronized (lock) { // 🔥 thêm dòng này

            String error = validateBid(bidder, bidAmount);
            if (error != null) {
                System.out.println("Lỗi: " + error);
                return false;
            }

            currentHighestBid = bidAmount;
            highestBidder = bidder;
            item.setCurrentPrice(bidAmount);

            System.out.printf("Thành công: [%s] bid %,.0f VNĐ\n",
                    bidder.getUsername(), bidAmount);

            // 🔥 thêm realtime nhẹ
            broadcastUpdate();

            return true;
        }
    }

    // =========================
    // REALTIME (thêm rất nhẹ)
    // =========================
    public void addClient(Object client) {
        clients.add(client);
    }

    private void broadcastUpdate() {
        for (Object c : clients) {
            // để tránh lỗi code cũ, chỉ print demo
            System.out.println("Update -> Giá mới: " + currentHighestBid);
        }
    }

    // =========================
    // KẾT THÚC
    // =========================
    public void closeAuction() {

        if (!isActive) {
            System.out.println("Phiên đã đóng rồi.");
            return;
        }

        isActive = false;

        System.out.println("\n--- KẾT THÚC PHIÊN " + getId() + " ---");

        if (highestBidder != null) {
            highestBidder.deductBalance(currentHighestBid);

            System.out.printf("Người thắng: [%s] với %,.0f VNĐ\n",
                    highestBidder.getUsername(),
                    currentHighestBid);
        } else {
            System.out.println("Không có ai tham gia.");
        }
    }

    // =========================
    // GETTER
    // =========================
    public Item getItem() { return item; }
    public User getHighestBidder() { return highestBidder; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public boolean isActive() { return isActive; }

    // =========================
    // DEBUG
    // =========================
    @Override
    public String toString() {
        return "Auction{" +
                "id=" + getId() +
                ", item=" + (item != null ? item.getName() : "null") +
                ", highestBid=" + String.format("%,.0f VNĐ", currentHighestBid) +
                ", bidder=" + (highestBidder != null ? highestBidder.getUsername() : "null") +
                ", active=" + isActive +
                '}';
    }
}