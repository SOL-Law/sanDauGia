package model;

import java.util.Objects;

public abstract class Entity {


    // ID định danh duy nhất của object
    protected int id;

    // Constructor mặc định (dùng khi tạo object rỗng)
    public Entity() {
    }

    // Constructor có tham số
    public Entity(int id) {
        setId(id); // Gọi setter để k
        // iểm tra dữ liệu
    }

    public int getId() {
        return id;
    }

    // Validate id (id không được âm hoặc = 0)
    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID phải lớn hơn 0.");
        }
        this.id = id;
    }

    // ==========================================
    // So sánh object dựa trên id (rất quan trọng)
    // ==========================================
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // cùng địa chỉ
        if (obj == null || getClass() != obj.getClass()) return false;

        Entity entity = (Entity) obj;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ==========================================
    // In thông tin object (debug)
    // ==========================================
    @Override
    public String toString() {
        return "Entity{id=" + id + '}';
    }
}