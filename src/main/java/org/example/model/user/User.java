package org.example.model.user;

import org.example.model.Entity;

public abstract class User extends Entity {
    // Encapsulation: Thuộc tính phải là private/protected
    private String username;
    private String password;
    private String email;
    private double balance;

    // Constructor
    // Constructor rỗng cho Database
    public User() {
        super();
    }
    public User(int id, String username, String password, String email) {
        super(id); // Gọi constructor của lớp cha (Entity) để gán ID
        this.username = username;
        this.password = password;
        this.email = email;
        this.balance=0.0;
    }

    // Các Getter và Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    // ==========================================
    // CÁC HÀM XỬ LÝ VÍ TIỀN BẤT TỬ (KÈM NGOẠI LỆ)
    // ==========================================

    public double getBalance() {
        return balance;
    }

    public void addBalance(double amount) {
        if (amount <= 0) {
            // TUNG LỖI: Cấm nạp tiền âm hoặc nạp 0 đồng
            throw new IllegalArgumentException("Tiền nạp vào (" + amount + ") không hợp lệ. Phải lớn hơn 0!");
        }
        this.balance += amount;
        System.out.println("💰 Đã nạp " + amount + " vào ví. Số dư: " + this.balance);
    }

    public void deductBalance(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(" Số tiền trừ không hợp lệ!");
        }
        if (this.balance < amount) {
            // TUNG LỖI: Nghèo mà đòi đú
            throw new IllegalStateException(" Ví không đủ tiền! Cần " + amount + " nhưng chỉ có " + this.balance);
        }
        this.balance -= amount;
        System.out.println(" Đã trừ " + amount + ". Số dư còn lại: " + this.balance);
    }

    // Đa hình (Polymorphism): Một hàm abstract bắt buộc các lớp con phải tự định nghĩa [cite: 121]
    public abstract void displayRole();
}
