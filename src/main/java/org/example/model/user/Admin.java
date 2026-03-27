package org.example.model.user;

public class Admin extends User {

    // Constructor mặc định
    public Admin() {
        super();
    }

    // Constructor khởi tạo người dùng với vai trò Quản trị viên (Admin)
    public Admin(int id, String username, String password, String email) {
        super(id, username, password, email);
    }

    // ==========================================
    // GHI ĐÈ PHƯƠNG THỨC (Đa hình - Polymorphism)
    // ==========================================
    @Override
    public void displayRole() {
        System.out.println("Vai trò hệ thống: Quản trị viên (Administrator).");
        System.out.println("Tài khoản: " + this.getUsername() + " | Email bảo mật: " + this.getEmail());
        System.out.println("Quyền hạn tối cao: Quản lý toàn bộ hệ thống, kiểm duyệt User, xóa Auction.");
    }
}