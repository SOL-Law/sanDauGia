package org.example.model.item;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    // 1. Constructor rỗng (Bắt buộc cho Database)
    public Electronics() {
        super();
    }

    // 2. Constructor đầy đủ
    public Electronics(int id, String name, double startPrice, String description,
                       String brand, int warrantyMonths) {
        super(id, name, startPrice, description); // Gọi Bố Item
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    // 3. Getters & Setters
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    // 4. THỰC THI ĐA HÌNH: Cách in thông tin riêng của Đồ Điện Tử
    @Override
    public void displayItemDetails() {
        System.out.println("📱 --- THIẾT BỊ ĐIỆN TỬ ---");
        System.out.println("Mã SP: " + this.id);
        System.out.println("Tên sản phẩm: " + getName());
        System.out.println("Mô tả: " + getDescription());
        System.out.println("Thương hiệu: " + this.brand);
        System.out.println("Bảo hành: " + this.warrantyMonths + " tháng");
        System.out.println("Giá hiện tại: " + getCurrentPrice() + " VNĐ");
    }
}