package model.user;

public class Admin extends User {

    // Constructor mặc định (tạo object rỗng / phục vụ mapping dữ liệu)
    public Admin() {
        super();
    }

    // Constructor đầy đủ thông tin
    public Admin(int id, String username, String password, String email) {
        // Gọi constructor lớp cha để khởi tạo thông tin chung
        super(id, username, password, email);
    }

    // ==========================================
    // GHI ĐÈ PHƯƠNG THỨC (Đa hình - Polymorphism)
    // ==========================================
    @Override
    public void displayRole() {
        System.out.println("--- THÔNG TIN QUẢN TRỊ VIÊN ---");

        // Thông tin tài khoản
        System.out.println("Username : " + getUsername());
        System.out.println("Email    : " + getEmail());

        // Vai trò + quyền hạn
        System.out.println("Vai trò  : Admin (Quản trị viên)");
        System.out.println("Quyền    : Quản lý toàn hệ thống, kiểm duyệt user, xóa auction");

        System.out.println("--------------------------------");
    }

    // ==========================================
    // GHI ĐÈ toString() (hỗ trợ debug/in nhanh)
    // ==========================================
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}