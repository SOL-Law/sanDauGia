package org.example.model.item;

public class Art extends Item {
    private String artistName;
    private String material;

    // 1. Constructor RỖNG
    public Art() {
        super(); // Gọi lên thằng Bố Item rỗng
    }

    // 2. Constructor ĐẦY ĐỦ
    public Art(int id, String name, double startPrice, String description,
               String artistName, String material) {
        // Ném id, tên, giá lên cho Bố
        super(id, name, startPrice, description);
        this.artistName = artistName;
        this.material = material;
    }
    // --- (Tự Generate Get/Set cho artistName và material) ---

    // THỰC THI ĐA HÌNH
    @Override
    public void displayItemDetails() {
        System.out.println("🎨 --- TÁC PHẨM NGHỆ THUẬT ---");
        // Vì "id" trên Entity dùng "protected", thằng Art có thể gọi thẳng "this.id" thay vì "getId()"
        System.out.println("Mã SP: " + this.id);
        System.out.println("Tên tác phẩm: " + getName());
        System.out.println("Tác giả: " + this.artistName);
        System.out.println("Chất liệu: " + this.material);
        System.out.println("Giá hiện tại: " + getCurrentPrice() + " VNĐ");
    }
}