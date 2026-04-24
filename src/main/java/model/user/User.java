package model.user;

import model.Entity;

public class User extends Entity {

    private String username;
    private String password;
    private String email;
    private double balance;
    private String role; // BIDDER, SELLER, ADMIN

    // Constructor mặc định
    public User() {
        super();
        this.role = "BIDDER";
        this.balance = 0.0;
    }

    // Constructor đầy đủ (có role)
    public User(int id, String username, String password, String email, String role) {
        super(id);
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.setRole(role);
        this.balance = 0.0;
    }

    // Constructor không có role (mặc định BIDDER)
    public User(int id, String username, String password, String email) {
        super(id);
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.role = "BIDDER";
        this.balance = 0.0;
    }

    // Constructor tiện dụng: id, username, balance (dùng cho test nhanh)
    public User(int id, String username, double balance) {
        super(id);
        this.setUsername(username);
        this.password = "default123"; // gán mặc định
        this.email = username.toLowerCase() + "@example.com";
        this.role = "BIDDER";
        this.balance = balance;
    }

    // Getter / Setter
    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username không hợp lệ.");
        }
        this.username = username.trim();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password phải >= 6 ký tự.");
        }
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        this.email = email.trim();
    }

    public double getBalance() { return balance; }

    public String getRole() { return role; }
    public void setRole(String role) {
        if (role == null) {
            this.role = "BIDDER";
            return;
        }
        role = role.toUpperCase();
        if (!role.equals("BIDDER") && !role.equals("SELLER") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Role không hợp lệ.");
        }
        this.role = role;
    }

    // Nạp tiền
    public void addBalance(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền phải > 0.");
        balance += amount;
        System.out.printf("Nạp thành công. [%s]: %,.0f VNĐ\n", username, balance);
    }

    // Trừ tiền
    public void deductBalance(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền không hợp lệ.");
        if (balance < amount) throw new IllegalStateException("Không đủ tiền.");
        balance -= amount;
        System.out.printf("Trừ tiền thành công [%s]: -%,.0f VNĐ\n", username, amount);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", balance=" + String.format("%,.0f VNĐ", balance) +
                '}';
    }
}
