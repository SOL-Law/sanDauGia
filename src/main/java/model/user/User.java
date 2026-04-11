package model.user;

import model.Entity;

public class User extends Entity {

    private String username;
    private String password;
    private String email;
    private double balance;

    // 🔥 ROLE
    private String role;

    public User() {
        super();
        this.role = "BIDDER";
    }

    // 🔥 Constructor đầy đủ (có role)
    public User(int id, String username, String password, String email, String role) {
        super(id);
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.balance = 0.0;
        this.setRole(role);
    }

    // 🔥 FIX: thêm constructor 4 tham số (QUAN TRỌNG)
    public User(int id, String username, String password, String email) {
        super(id);
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.balance = 0.0;
        this.role = "BIDDER"; // mặc định
    }

    // =========================
    // GETTER / SETTER
    // =========================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username không hợp lệ.");
        }
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password phải >= 6 ký tự.");
        }
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        this.email = email.trim();
    }

    public double getBalance() {
        return balance;
    }

    // =========================
    // ROLE
    // =========================
    public String getRole() {
        return role;
    }

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

    // =========================
    // NẠP TIỀN
    // =========================
    public void addBalance(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền phải > 0.");
        }

        balance += amount;

        System.out.printf("Nạp thành công. [%s]: %,.0f VNĐ\n",
                username, balance);
    }

    // =========================
    // TRỪ TIỀN
    // =========================
    public void deductBalance(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền không hợp lệ.");
        }

        if (balance < amount) {
            throw new IllegalStateException("Không đủ tiền.");
        }

        balance -= amount;

        System.out.printf("Trừ tiền thành công [%s]: -%,.0f VNĐ\n",
                username, amount);
    }

    // =========================
    // DEBUG
    // =========================
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