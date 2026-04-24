package model;

import model.item.Item;
import model.user.User;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Auction extends Entity {
    private static final double MIN_BID_INCREMENT = 10_000;

    private Item item;
    private User highestBidder;
    private double currentHighestBid;
    private boolean isActive;
    private LocalDateTime endTime;

    private final Object lock = new Object();
    private List<Object> clients = new ArrayList<>();
    private List<BidTransaction> transactions = new ArrayList<>();

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Auction(int id, Item item, LocalDateTime endTime) {
        super(id);
        if (item == null) throw new IllegalArgumentException("Item không được null.");
        this.item = item;
        this.currentHighestBid = item.getStartPrice();
        this.highestBidder = null;
        this.isActive = true;
        this.endTime = endTime;

        scheduleClose();
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

    public boolean placeBid(User bidder, double bidAmount) {
        synchronized (lock) {
            String error = validateBid(bidder, bidAmount);
            if (error != null) {
                System.out.println("Lỗi: " + error);
                return false;
            }

            currentHighestBid = bidAmount;
            highestBidder = bidder;
            item.setCurrentPrice(bidAmount);

            BidTransaction tx = new BidTransaction(transactions.size() + 1, this, bidder, bidAmount);
            transactions.add(tx);

            System.out.printf("Thành công: [%s] bid %,.0f VNĐ\n", bidder.getUsername(), bidAmount);

            long secondsLeft = Duration.between(LocalDateTime.now(), endTime).getSeconds();
            if (secondsLeft <= 10) {
                endTime = endTime.plusSeconds(10);
                System.out.println("Gia hạn thêm 10 giây! Thời gian mới: " + endTime);
                scheduler.shutdownNow();
                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduleClose();
            }

            broadcastUpdate();
            return true;
        }
    }

    public void addClient(Object client) { clients.add(client); }

    private void broadcastUpdate() {
        for (Object c : clients) {
            System.out.println("Update -> Giá mới: " + currentHighestBid);
        }
    }

    private void scheduleClose() {
        long delay = Duration.between(LocalDateTime.now(), endTime).toMillis();
        scheduler.schedule(this::closeAuction, delay, TimeUnit.MILLISECONDS);
    }

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
                    highestBidder.getUsername(), currentHighestBid);
        } else {
            System.out.println("Không có ai tham gia.");
        }
    }

    public Item getItem() { return item; }
    public User getHighestBidder() { return highestBidder; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<BidTransaction> getTransactions() { return transactions; }

    @Override
    public String toString() {
        return "Auction{" +
                "id=" + getId() +
                ", item=" + (item != null ? item.getName() : "null") +
                ", highestBid=" + String.format("%,.0f VNĐ", currentHighestBid) +
                ", bidder=" + (highestBidder != null ? highestBidder.getUsername() : "null") +
                ", active=" + isActive +
                ", endTime=" + endTime +
                '}';
    }
}
