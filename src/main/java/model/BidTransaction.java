package model;

import model.user.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidTransaction extends Entity {

    private Auction auction;
    private User bidder;
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidTransaction() {
        super();
    }

    public BidTransaction(int id, Auction auction, User bidder, double bidAmount) {
        super(id);

        // Validate object
        if (auction == null || bidder == null) {
            throw new IllegalArgumentException("Auction và Bidder không được null.");
        }

        // Validate giá
        if (bidAmount <= 0) {
            throw new IllegalArgumentException("Giá giao dịch phải lớn hơn 0.");
        }

        if (bidAmount <= auction.getCurrentHighestBid()) {
            throw new IllegalArgumentException("Giá phải lớn hơn giá hiện tại.");
        }

        this.auction = auction;
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Auction không được null.");
        }
        this.auction = auction;
    }

    public User getBidder() {
        return bidder;
    }

    public void setBidder(User bidder) {
        if (bidder == null) {
            throw new IllegalArgumentException("Bidder không được null.");
        }
        this.bidder = bidder;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        if (bidAmount <= 0) {
            throw new IllegalArgumentException("Giá giao dịch phải lớn hơn 0.");
        }

        if (this.auction != null && bidAmount <= this.auction.getCurrentHighestBid()) {
            throw new IllegalArgumentException("Giá phải lớn hơn giá hiện tại.");
        }

        this.bidAmount = bidAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return "BidTransaction{" +
                "id=" + getId() +
                ", bidder=" + bidder.getUsername() +
                ", bidAmount=" + String.format("%,.0f VNĐ", bidAmount) +
                ", time=" + timestamp.format(formatter) +
                '}';
    }
}