package org.example.model.user;

public class Bidder extends User {

    // Constructor mặc định
    public Bidder() {
        super();
    }

    // Constructor khởi tạo người dùng với vai trò Bidder
    public Bidder(int id, String username, String password, String email) {
        // Gọi constructor của lớp cha (User)
        super(id, username, password, email);
    }

    // ==========================================
    // GHI ĐÈ PHƯƠNG THỨC (Đa hình - Polymorphism)
    // ==========================================
    @Override
    public void displayRole() {
        System.out.println("Vai trò hệ thống: Người tham gia đấu giá (Bidder).");
        System.out.println("Tài khoản: " + this.getUsername() + " | Email: " + this.getEmail());
    }
}