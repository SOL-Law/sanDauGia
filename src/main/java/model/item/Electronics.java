package model.item;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics() {
        super();
    }

    public Electronics(int id, String name, double startPrice, String description,
                       String brand, int warrantyMonths) {
        super(id, name, startPrice, description);
        this.setBrand(brand);
        this.setWarrantyMonths(warrantyMonths);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            this.brand = "Không rõ";
        } else {
            this.brand = brand.trim();
        }
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        if (warrantyMonths < 0) {
            throw new IllegalArgumentException("Thời gian bảo hành không được nhỏ hơn 0.");
        }
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void displayItemDetails() {
        System.out.println("--- CHI TIẾT THIẾT BỊ ĐIỆN TỬ ---");
        System.out.println("Mã sản phẩm : " + getId());
        System.out.println("Tên sản phẩm: " + getName());
        System.out.println("Mô tả       : " + getDescription());
        System.out.println("Thương hiệu : " + brand);
        System.out.println("Bảo hành    : " + warrantyMonths + " tháng");
        System.out.printf("Giá hiện tại: %,.0f VNĐ\n", getCurrentPrice());
        System.out.println("---------------------------------");
    }

    @Override
    public String toString() {
        return "Electronics{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", brand='" + brand + '\'' +
                ", warrantyMonths=" + warrantyMonths +
                ", price=" + String.format("%,.0f VNĐ", getCurrentPrice()) +
                '}';
    }
}