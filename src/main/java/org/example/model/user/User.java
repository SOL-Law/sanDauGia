package org.example.model.user;

import org.example.model.Entity;

public abstract class User extends Entity {

    private String username;
    private String password;
    private String email;
    private double balance;

    public User() {
        super();
    }

    public User(int id, String username, String password, String email) {
        super(id);
        this.setUsername(username);
        this.setPassword(password);
        this.setEmail(email);
        this.balance = 0.0;
    }

    public String getUsername() {
        return username;
    }

    // Validate username
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username không hợp lệ.");
        }
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    // Validate password (cơ bản)
    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password phải >= 6 ký tự.");
        }
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    // Validate email đơn giản
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
    // ABSTRACT
    // =========================
    public abstract void displayRole();

    // =========================
    // DEBUG (KHÔNG in password)
    // =========================
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", balance=" + String.format("%,.0f VNĐ", balance) +
                '}';
    }
}