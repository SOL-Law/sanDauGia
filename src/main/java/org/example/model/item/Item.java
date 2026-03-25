package org.example.model.item;

import org.example.model.Entity;

public abstract class Item extends Entity {
    // Thuộc tính riêng của Item để private bảo mật
    private String name;
    private double startPrice;
    private double currentPrice;
    private String description;

    // ==========================================
    // 1. Constructor RỖNG (Bắt cặp với Entity rỗng)
    // Cực kỳ quan trọng để sau này lôi dữ liệu từ MySQL lên!
    // ==========================================
    public Item() {
        super(); // Gọi lên cụ Entity rỗng
    }

    // ==========================================
    // 2. Constructor ĐẦY ĐỦ (Dùng khi tạo mới món hàng bằng tay)
    // ==========================================
    public Item(int id, String name, double startPrice, String description) {
        super(id); // Ném ID lên cho cụ Entity
        this.name = name;
        this.startPrice = startPrice;
        this.currentPrice = startPrice; // Lúc mới đăng, giá hiện tại = giá khởi điểm
        this.description = description;
    }


    // get và set các hàm


    public double getStartPrice() {
        return startPrice;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartPrice(double startPrice) {
        if (startPrice < 0) {
            // TUNG LỖI: Cấm đặt giá khởi điểm âm
            throw new IllegalArgumentException("❌ Bị điên à? Giá khởi điểm (" + startPrice + ") không được nhỏ hơn 0!");
        }
        this.startPrice = startPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        if (currentPrice < this.startPrice) {
            // TUNG LỖI: Giá đấu hiện tại không được thấp hơn giá khởi điểm
            throw new IllegalArgumentException("❌ Giá hiện tại không được thấp hơn giá khởi điểm!");
        }
        this.currentPrice = currentPrice;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getCurrentPrice() { return currentPrice; }

    // ĐA HÌNH: Ép tụi con phải tự định nghĩa cách in thông tin
    public abstract void displayItemDetails();
}