package org.example.model;

import org.example.model.user.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidTransaction extends Entity {
    // ==========================================
    // THUỘC TÍNH LỊCH SỬ GIAO DỊCH (Transaction Records)
    // ==========================================
    private Auction auction;            // Tham chiếu đến phiên đấu giá diễn ra giao dịch
    private User bidder;                // Tham chiếu đến người dùng thực hiện trả giá
    private double bidAmount;           // Mức giá được ghi nhận trong lần giao dịch này
    private LocalDateTime timestamp;    // Dấu thời gian (Timestamp) ghi nhận lúc đặt giá

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    // Constructor mặc định (Dành cho việc map dữ liệu từ Database/File)
    public BidTransaction() {
        super();
    }

    // Constructor khởi tạo một giao dịch trả giá mới
    public BidTransaction(int id, Auction auction, User bidder, double bidAmount) {
        super(id);
        this.auction = auction;
        this.bidder = bidder;

        // Ràng buộc nghiệp vụ: Giá trị giao dịch phải là số dương
        if (bidAmount <= 0) {
            throw new IllegalArgumentException("Lỗi dữ liệu: Mức giá giao dịch phải lớn hơn 0.");
        }
        this.bidAmount = bidAmount;

        // Tự động lấy thời gian hệ thống ngay tại thời điểm khởi tạo Object
        this.timestamp = LocalDateTime.now();
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public Auction getAuction() { return auction; }
    public void setAuction(Auction auction) { this.auction = auction; }

    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) {
        if (bidAmount <= 0) {
            throw new IllegalArgumentException("Lỗi dữ liệu: Mức giá giao dịch phải lớn hơn 0.");
        }
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getTimestamp() { return timestamp; }

    // ==========================================
    // NGHIỆP VỤ HIỂN THỊ (In biên lai/Lịch sử)
    // ==========================================
    public void printTransactionReceipt() {
        // Định dạng lại thời gian cho chuẩn style Việt Nam (Ngày/Tháng/Năm Giờ:Phút:Giây)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedTime = this.timestamp.format(formatter);

        System.out.println("🧾 --- BIÊN LAI GHI NHẬN TRẢ GIÁ ---");
        System.out.println("Mã giao dịch  : " + this.getId());
        System.out.println("Mã phiên đấu  : " + this.auction.getId() + " (Vật phẩm: " + this.auction.getItem().getName() + ")");
        System.out.println("Người trả giá : " + this.bidder.getUsername());
        System.out.println("Mức giá đặt   : " + this.bidAmount + " VNĐ");
        System.out.println("Thời gian     : " + formattedTime);
        System.out.println("-----------------------------------");
    }
}