package org.example.model.item;

public class Vehicle extends Item {
    private String vehicleType;
    private String engine;

    // 1. Constructor rỗng
    public Vehicle() {
        super();
    }

    // 2. Constructor đầy đủ
    public Vehicle(int id, String name, double startPrice, String description,
                   String vehicleType, String engine) {
        super(id, name, startPrice, description); // Gọi Bố Item
        this.vehicleType = vehicleType;
        this.engine = engine;
    }

    // 3. Getters & Setters
    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    // 4. THỰC THI ĐA HÌNH: Cách in thông tin riêng của Xe cộ
    @Override
    public void displayItemDetails() {
        System.out.println("🏎️ --- SIÊU XE TỐC ĐỘ ---");
        System.out.println("Mã SP: " + this.id);
        System.out.println("Tên xe: " + getName());
        System.out.println("Mô tả: " + getDescription());
        System.out.println("Loại xe: " + this.vehicleType);
        System.out.println("Động cơ: " + this.engine);
        System.out.println("Giá hiện tại: " + getCurrentPrice() + " VNĐ");
    }
}