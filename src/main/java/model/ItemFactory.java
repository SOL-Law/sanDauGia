package model;

import model.item.Art;
import model.item.Electronics;
import model.item.Item;
import model.item.Vehicle;

public class ItemFactory {

    //  ĐÂY CHÍNH LÀ FACTORY METHOD PARTTERN
    public static Item createItem(int id, String name, double startPrice, String category) {
        if (category == null) category = "Khác";

        switch (category) {
            case "Nghệ thuật":
                return new Art(id, name, startPrice, "Tác phẩm đấu giá", "Không rõ tác giả", "Chất liệu tùy chọn");
            case "Điện tử":
                return new Electronics(id, name, startPrice, "Thiết bị điện tử", "Chưa rõ hãng", 12);
            case "Xe cộ":
                return new Vehicle(id, name, startPrice, "Phương tiện đi lại", "Đa dụng", "Động cơ tiêu chuẩn");
            default:
                // Mặc định trả về Art nếu không khớp
                return new Art(id, name, startPrice, "Sản phẩm đấu giá chung", "Khác", "Khác");
        }
    }
}