package org.example.model.item;

public class Art extends Item {
    // ==========================================
    // THUỘC TÍNH MỞ RỘNG (Đặc thù của Tác phẩm nghệ thuật)
    // ==========================================
    private String author;      // Tác giả/Nghệ nhân sáng tác
    private String material;    // Chất liệu (VD: Sơn dầu, Gỗ, Gốm sứ)

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    // Constructor mặc định (Hỗ trợ Data Mapping)
    public Art() {
        super();
    }

    // Constructor khởi tạo đầy đủ thuộc tính
    public Art(int id, String name, double startPrice, String description,
               String author, String material) {
        // Kế thừa và khởi tạo thuộc tính từ lớp cha (Item)
        super(id, name, startPrice, description);
        this.author = author;
        this.material = material;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    // ==========================================
    // GHI ĐÈ PHƯƠNG THỨC (Đa hình - Polymorphism)
    // ==========================================
    @Override
    public void displayItemDetails() {
        System.out.println("--- CHI TIẾT TÁC PHẨM NGHỆ THUẬT ---");
        System.out.println("Mã tác phẩm  : " + this.getId());
        System.out.println("Tên tác phẩm : " + this.getName());
        System.out.println("Mô tả        : " + this.getDescription());
        System.out.println("Tác giả      : " + this.author);
        System.out.println("Chất liệu    : " + this.material);
        System.out.println("Giá hiện tại : " + this.getCurrentPrice() + " VNĐ");
        System.out.println("------------------------------------");
    }
}