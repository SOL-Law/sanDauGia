package org.example.model.item;

import org.example.model.Entity;

public abstract class Item extends Entity {

    private String name;
    private double startPrice;
    private double currentPrice;
    private String description;

    public Item() {
        super();
    }

    public Item(int id, String name, double startPrice, String description) {
        super(id);

        // Dùng setter để validate
        this.setName(name);
        this.setStartPrice(startPrice);
        this.setDescription(description);

        this.currentPrice = startPrice;
    }

    public String getName() {
        return name;
    }

    // Validate tên
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "Không rõ";
        } else {
            this.name = name.trim();
        }
    }

    public String getDescription() {
        return description;
    }

    // Validate mô tả
    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            this.description = "Không có mô tả";
        } else {
            this.description = description.trim();
        }
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setStartPrice(double startPrice) {
        if (startPrice <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải > 0.");
        }
        this.startPrice = startPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        // Không cho giảm giá
        if (currentPrice < this.currentPrice) {
            throw new IllegalArgumentException("Giá mới không được thấp hơn giá hiện tại.");
        }

        // Không thấp hơn giá khởi điểm
        if (currentPrice < this.startPrice) {
            throw new IllegalArgumentException("Giá không được thấp hơn giá khởi điểm.");
        }

        this.currentPrice = currentPrice;
    }

    // =========================
    // ABSTRACT
    // =========================
    public abstract void displayItemDetails();

    // =========================
    // DEBUG
    // =========================
    @Override
    public String toString() {
        return "Item{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", startPrice=" + String.format("%,.0f VNĐ", startPrice) +
                ", currentPrice=" + String.format("%,.0f VNĐ", currentPrice) +
                '}';
    }
}