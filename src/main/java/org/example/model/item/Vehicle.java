package org.example.model.item;

public class Vehicle extends Item {
    // ==========================================
    // THUỘC TÍNH MỞ RỘNG (Đặc thù của Phương tiện)
    // ==========================================
    private String vehicleType;     // Phân loại phương tiện (VD: Ô tô, Xe máy)
    private String engine;          // Thông số động cơ

    // Constructor mặc định
    public Vehicle() {
        super();
    }

    // Constructor khởi tạo đầy đủ thuộc tính
    public Vehicle(int id, String name, double startPrice, String description,
                   String vehicleType, String engine) {
        super(id, name, startPrice, description);
        this.vehicleType = vehicleType;
        this.engine = engine;
    }

    // Getters & Setters
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    // Ghi đè phương thức hiển thị chi tiết
    @Override
    public void displayItemDetails() {
        System.out.println("--- CHI TIẾT PHƯƠNG TIỆN ---");
        System.out.println("Mã phương tiện: " + this.getId());
        System.out.println("Tên phương tiện: " + this.getName());
        System.out.println("Mô tả          : " + this.getDescription());
        System.out.println("Loại xe        : " + this.vehicleType);
        System.out.println("Động cơ        : " + this.engine);
        System.out.println("Giá hiện tại   : " + this.getCurrentPrice() + " VNĐ");
        System.out.println("----------------------------");
    }
}