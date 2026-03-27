package org.example.model;

import org.example.model.item.Item;
import org.example.model.user.User;

public class Auction extends Entity {
    // ==========================================
    // THUỘC TÍNH QUẢN LÝ PHIÊN ĐẤU GIÁ
    // ==========================================
    private Item item;                // Đối tượng hàng hóa được mang ra đấu giá
    private User highestBidder;       // Tham chiếu đến người dùng đang trả giá cao nhất

    private double currentHighestBid; // Mức giá cao nhất được ghi nhận ở thời điểm hiện tại
    private boolean isActive;         // Trạng thái phiên đấu giá (true: đang diễn ra, false: đã kết thúc)

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    // Constructor mặc định (cần thiết cho việc map dữ liệu từ Database)
    public Auction() {
        super();
    }

    // Constructor khởi tạo phiên đấu giá mới dựa trên một vật phẩm
    public Auction(int id, Item item) {
        super(id);
        this.item = item;
        this.currentHighestBid = item.getStartPrice(); // Giá khởi điểm của phiên bằng giá gốc của vật phẩm
        this.highestBidder = null; // Chưa có giao dịch đặt giá nào khi mới mở phiên
        this.isActive = true;      // Thiết lập trạng thái hoạt động cho phiên đấu giá
    }

    // ==========================================
    // NGHIỆP VỤ LÕI 1: XỬ LÝ ĐẶT GIÁ (PLACE BID)
    // ==========================================
    public boolean placeBid(User bidder, double bidAmount) {
        // Kiểm tra ràng buộc 1: Phiên đấu giá phải đang trong trạng thái hoạt động
        if (!isActive) {
            System.out.println("Giao dịch thất bại: Phiên đấu giá này đã kết thúc.");
            return false;
        }

        // Kiểm tra ràng buộc 2: Mức giá đề xuất phải lớn hơn mức giá cao nhất hiện tại
        if (bidAmount <= currentHighestBid) {
            System.out.println("Giao dịch thất bại: Mức giá đề xuất phải cao hơn " + currentHighestBid + " VNĐ.");
            return false;
        }

        // Kiểm tra ràng buộc 3: Xác thực số dư khả dụng trong tài khoản của người dùng
        if (bidder.getBalance() < bidAmount) {
            System.out.println("Giao dịch thất bại: Tài khoản của người dùng [" + bidder.getUsername() + "] không đủ số dư.");
            return false;
        }

        // Nếu thỏa mãn tất cả điều kiện, tiến hành ghi nhận mức giá mới
        this.currentHighestBid = bidAmount;
        this.highestBidder = bidder;

        // Đồng bộ hóa mức giá hiện tại cho đối tượng Item
        this.item.setCurrentPrice(bidAmount);

        System.out.println("Giao dịch thành công: Người dùng [" + bidder.getUsername() + "] đã đặt giá " + bidAmount + " VNĐ.");
        return true;
    }

    // ==========================================
    // NGHIỆP VỤ LÕI 2: KẾT THÚC PHIÊN ĐẤU GIÁ
    // ==========================================
    public void closeAuction() {
        this.isActive = false; // Cập nhật trạng thái đóng phiên
        System.out.println("\n--- KẾT THÚC PHIÊN ĐẤU GIÁ SỐ " + this.getId() + " ---");

        // Kiểm tra xem có người dùng nào thắng cuộc không
        if (highestBidder != null) {
            // Thực hiện nghiệp vụ trừ tiền từ tài khoản người thắng cuộc
            highestBidder.deductBalance(currentHighestBid);
            System.out.println("Thông báo: Người dùng [" + highestBidder.getUsername() + "] đã thắng đấu giá với mức tiền: " + currentHighestBid + " VNĐ.");
        } else {
            System.out.println("Thông báo: Phiên đấu giá kết thúc mà không có giao dịch nào được thực hiện.");
        }
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public Item getItem() { return item; }
    public User getHighestBidder() { return highestBidder; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public boolean isActive() { return isActive; }
}
