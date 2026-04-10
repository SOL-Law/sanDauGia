package model;

import model.item.Item;
import model.user.User;

public class Auction extends Entity {
    private static final double MIN_BID_INCREMENT = 10_000;

    private Item item;
    private User highestBidder;
    private double currentHighestBid;
    private boolean isActive;

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

        // Validate
        if (item == null) {
            throw new IllegalArgumentException("Item không được null.");
        }

        this.item = item;
        this.currentHighestBid = item.getStartPrice();
        this.highestBidder = null;
        this.isActive = true;
    }

    // =========================
    // ĐẶT GIÁ
    // =========================
    public boolean placeBid(User bidder, double bidAmount) {

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

        return true;
    }

    // =========================
    // KẾT THÚC
    // =========================
    public void closeAuction() {

        // Chặn gọi nhiều lần
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