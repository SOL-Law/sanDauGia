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

    // --- (Đại ca bảo đệ tự Generate Get/Set cho 4 biến name, price, description nhé) ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    // ĐA HÌNH: Ép tụi con phải tự định nghĩa cách in thông tin
    public abstract void displayItemDetails();
}