package model.item;

public class Art extends Item {

    // Thuộc tính riêng của tác phẩm nghệ thuật
    private String author;      // Tác giả
    private String material;    // Chất liệu

    // Constructor mặc định (dùng khi tạo object rỗng hoặc đọc dữ liệu từ file/db)
    public Art() {
        super();
    }

    // Constructor đầy đủ thông tin
    public Art(int id, String name, double startPrice, String description, String author, String material) {
        // Gọi constructor lớp cha để khởi tạo phần chung
        super(id, name, startPrice, description);

        // Gọi setter để kiểm tra dữ liệu đầu vào
        this.setAuthor(author);
        this.material = material; // (chưa validate ở đây)
    }

    public String getAuthor() {
        return author;
    }

    // Setter có xử lý dữ liệu đầu vào
    public void setAuthor(String author) {
        // Nếu null hoặc rỗng → gán mặc định
        if (author == null || author.trim().isEmpty()) {
            this.author = "Không rõ";
        } else {
            this.author = author; // (có thể trim thêm cho đẹp)
        }
    }

    public String getMaterial() {
        return material;
    }

    // Setter đơn giản (chưa có validate)
    public void setMaterial(String material) {
        this.material = material;
    }

    // Ghi đè phương thức hiển thị từ lớp cha
    @Override
    public void displayItemDetails() {
        // In ra thông tin object thông qua toString()
        System.out.println(this.toString());
    }

    // toString dùng để in nhanh thông tin object (debug/log)
    @Override
    public String toString() {
        return "Art{" +
                "id=" + getId() +                 // ID từ lớp cha
                ", name='" + getName() + '\'' +   // Tên tác phẩm
                ", author='" + author + '\'' +   // Tác giả
                ", material='" + material + '\'' + // Chất liệu
                ", price=" + getCurrentPrice() + // Giá hiện tại
                '}';
    }
}