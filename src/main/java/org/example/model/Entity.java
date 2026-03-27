package org.example.model;

public abstract class Entity {
    // ==========================================
    // THUỘC TÍNH ĐỊNH DANH (Identifier)
    // ==========================================
    // Protected để các lớp con (User, Item, Auction) có thể truy cập trực tiếp nếu cần thiết
    protected int id;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    // Constructor mặc định
    public Entity() {
    }

    // Constructor khởi tạo định danh
    public Entity(int id) {
        this.id = id;
    }

    // ==========================================
    // GETTER & SETTER
    // ==========================================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}