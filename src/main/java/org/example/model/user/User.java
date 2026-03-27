package org.example.model.user;

import org.example.model.Entity;

public abstract class User extends Entity {
    // ==========================================
    // THUỘC TÍNH NGƯỜI DÙNG (Bảo mật thông tin)
    // ==========================================
    private String username;        // Tên đăng nhập
    private String password;        // Mật khẩu xác thực
    private String email;           // Địa chỉ thư điện tử
    private double balance;         // Số dư tài khoản khả dụng trong hệ thống

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    // Constructor mặc định
    public User() {
        super();
    }

    // Constructor khởi tạo người dùng mới
    public User(int id, String username, String password, String email) {
        super(id);
        this.username = username;
        this.password = password;
        this.email = email;
        this.balance = 0.0; // Mặc định tài khoản mới tạo sẽ có số dư là 0
    }

    // ==========================================
    // GETTERS & SETTERS CƠ BẢN
    // ==========================================
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getBalance() { return balance; }

    // ==========================================
    // NGHIỆP VỤ TÀI CHÍNH (Nạp/Rút tiền)
    // ==========================================
    public void addBalance(double amount) {
        // Kiểm tra ràng buộc: Số tiền nạp vào phải lớn hơn 0
        if (amount <= 0) {
            throw new IllegalArgumentException("Giao dịch từ chối: Số tiền nạp phải là số dương.");
        }
        this.balance += amount;
        System.out.println("Giao dịch thành công. Số dư hiện tại của [" + this.username + "] là: " + this.balance + " VNĐ.");
    }

    public void deductBalance(double amount) {
        // Kiểm tra ràng buộc 1: Số tiền trừ phải hợp lệ (>0)
        if (amount <= 0) {
            throw new IllegalArgumentException("Giao dịch từ chối: Số tiền giao dịch không hợp lệ.");
        }
        // Kiểm tra ràng buộc 2: Số dư thực tế phải đáp ứng đủ
        if (this.balance < amount) {
            throw new IllegalStateException("Giao dịch từ chối: Tài khoản không đủ số dư để thực hiện.");
        }
        this.balance -= amount;
        System.out.println("Giao dịch thành công. Đã khấu trừ " + amount + " VNĐ từ tài khoản [" + this.username + "].");
    }

    // ==========================================
    // PHƯƠNG THỨC TRỪU TƯỢNG (Đa hình - Polymorphism)
    // ==========================================
    // Yêu cầu các lớp con (Admin, Seller, Bidder) khai báo quyền hạn riêng
    public abstract void displayRole();
}