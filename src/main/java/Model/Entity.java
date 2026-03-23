package Model;
// Sử dụng Abstract Class để định nghĩa hành vi chung cho mọi thực thể
public abstract class  Entity {
    protected int id; // Dùng protected để các lớp con (User, Item) có thể truy cập

    public Entity() {
    }

    public Entity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
