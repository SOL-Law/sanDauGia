package org.example.model.user;

public class Seller extends User {

    // Constructor mặc định
    public Seller() {
        super();
    }

    // Constructor khởi tạo người dùng với vai trò Người bán (Seller)
    public Seller(int id, String username, String password, String email) {
        super(id, username, password, email);
    }

    // ==========================================
    // GHI ĐÈ PHƯƠNG THỨC (Đa hình - Polymorphism)
    // ==========================================
    @Override
    public void displayRole() {
        System.out.println("Vai trò hệ thống: Người bán hàng (Seller).");
        System.out.println("Tài khoản: " + this.getUsername() + " | Email xác thực: " + this.getEmail());
        System.out.println("Quyền hạn: Được phép đăng bán Item và quản lý phiên đấu giá cá nhân.");
    }
}