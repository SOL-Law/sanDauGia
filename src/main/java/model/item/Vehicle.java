package model.item;

public class Vehicle extends Item {

    // Thuộc tính riêng của phương tiện
    private String vehicleType; // Loại xe (Ô tô, Xe máy...)
    private String engine;      // Thông tin động cơ

    // Constructor mặc định (tạo object rỗng)
    public Vehicle() {
        super();
    }

    // Constructor đầy đủ thông tin
    public Vehicle(int id, String name, double startPrice, String description,
                   String vehicleType, String engine) {
        super(id, name, startPrice, description);

        // Gọi setter để validate dữ liệu
        this.setVehicleType(vehicleType);
        this.setEngine(engine);
    }



    public String getVehicleType() {
        return vehicleType;
    }

    // Validate: tránh null / rỗng
    public void setVehicleType(String vehicleType) {
        if (vehicleType == null || vehicleType.trim().isEmpty()) {
            this.vehicleType = "Không rõ";
        } else {
            this.vehicleType = vehicleType.trim();
        }
    }

    public String getEngine() {
        return engine;
    }

    // Validate: tránh null / rỗng
    public void setEngine(String engine) {
        if (engine == null || engine.trim().isEmpty()) {
            this.engine = "Không rõ";
        } else {
            this.engine = engine.trim();
        }
    }

    // Hiển thị thông tin chi tiết (format đẹp để demo)
    @Override
    public void displayItemDetails() {
        System.out.println("--- CHI TIẾT PHƯƠNG TIỆN ---");
        System.out.println("Mã phương tiện : " + getId());
        System.out.println("Tên phương tiện: " + getName());
        System.out.println("Mô tả          : " + getDescription());
        System.out.println("Loại xe        : " + vehicleType);
        System.out.println("Động cơ        : " + engine);
        System.out.printf("Giá hiện tại   : %,.0f VNĐ\n", getCurrentPrice());
        System.out.println("----------------------------");
    }

    // toString phục vụ debug/in nhanh
    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", engine='" + engine + '\'' +
                ", price=" + String.format("%,.0f VNĐ", getCurrentPrice()) +
                '}';
    }
}