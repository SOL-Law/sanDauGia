package org.example.model.item;

import org.example.model.Entity;

public abstract class Item extends Entity {
    // ==========================================
    // THUỘC TÍNH HÀNG HÓA (Đóng gói dữ liệu)
    // ==========================================
    private String name;            // Tên vật phẩm
    private double startPrice;      // Mức giá khởi điểm khi đưa lên sàn
    private double currentPrice;    // Mức giá cao nhất hiện tại đang được đấu
    private String description;     // Thông tin mô tả chi tiết

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    // Constructor mặc định
    public Item() {
        super();
    }

    // Constructor khởi tạo vật phẩm mới
    public Item(int id, String name, double startPrice, String description) {
        super(id); // Gọi constructor lớp cha Entity để thiết lập ID
        this.name = name;
        this.setStartPrice(startPrice); // Gọi setter để áp dụng logic kiểm tra dữ liệu
        this.currentPrice = startPrice; // Khi mới lên sàn, giá hiện tại bằng giá khởi điểm
        this.description = description;
    }

    // ==========================================
    // GETTERS & SETTERS (Kèm xử lý ngoại lệ - Exception)
    // ==========================================
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getStartPrice() { return startPrice; }
    public double getCurrentPrice() { return currentPrice; }

    public void setStartPrice(double startPrice) {
        // Kiểm tra tính hợp lệ: Giá khởi điểm không được phép âm
        if (startPrice < 0) {
            throw new IllegalArgumentException("Lỗi dữ liệu: Giá khởi điểm không được mang giá trị âm.");
        }
        this.startPrice = startPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        // Kiểm tra nghiệp vụ: Giá đấu mới không được thấp hơn giá khởi điểm
        if (currentPrice < this.startPrice) {
            throw new IllegalArgumentException("Lỗi nghiệp vụ: Giá hiện tại không được thấp hơn mức giá khởi điểm.");
        }
        this.currentPrice = currentPrice;
    }

    // ==========================================
    // PHƯƠNG THỨC TRỪU TƯỢNG (Đa hình - Polymorphism)
    // ==========================================
    // Yêu cầu các lớp con (Art, Electronics, Vehicle) tự định nghĩa cách hiển thị
    public abstract void displayItemDetails();
}