package org.example.model.item;

public class Electronics extends Item {
    // ==========================================
    // THUỘC TÍNH MỞ RỘNG (Đặc thù của Thiết bị điện tử)
    // ==========================================
    private String brand;           // Thương hiệu sản xuất
    private int warrantyMonths;     // Thời gian bảo hành (tính theo tháng)

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    // Constructor mặc định (Hỗ trợ ánh xạ dữ liệu từ cơ sở dữ liệu)
    public Electronics() {
        super();
    }

    // Constructor khởi tạo đầy đủ thuộc tính
    public Electronics(int id, String name, double startPrice, String description,
                       String brand, int warrantyMonths) {
        // Gọi constructor của lớp cha (Item) để khởi tạo các thuộc tính cơ bản
        super(id, name, startPrice, description);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    // ==========================================
    // GHI ĐÈ PHƯƠNG THỨC (Đa hình - Polymorphism)
    // ==========================================
    @Override
    public void displayItemDetails() {
        System.out.println("--- CHI TIẾT THIẾT BỊ ĐIỆN TỬ ---");
        System.out.println("Mã sản phẩm : " + this.getId());
        System.out.println("Tên sản phẩm: " + this.getName());
        System.out.println("Mô tả       : " + this.getDescription());
        System.out.println("Thương hiệu : " + this.brand);
        System.out.println("Bảo hành    : " + this.warrantyMonths + " tháng");
        System.out.println("Giá hiện tại: " + this.getCurrentPrice() + " VNĐ");
        System.out.println("---------------------------------");
    }
}