package Model.User;

import Model.Entity;

public abstract class User extends Entity {
    // Encapsulation: Thuộc tính phải là private/protected
    private String username;
    private String password;
    private String email;

    // Constructor
    public User(int id, String username, String password, String email) {
        super(id); // Gọi constructor của lớp cha (Entity) để gán ID
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Các Getter và Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Đa hình (Polymorphism): Một hàm abstract bắt buộc các lớp con phải tự định nghĩa [cite: 121]
    public abstract void displayRole();
}
