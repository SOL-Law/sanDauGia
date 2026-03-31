package model.user;

public class Seller extends User {

    // Constructor mặc định (tạo object rỗng / phục vụ mapping dữ liệu)
    public Seller() {
        super();
    }

    // Constructor đầy đủ thông tin
    public Seller(int id, String username, String password, String email) {
        // Gọi constructor lớp cha để khởi tạo thông tin chung
        super(id, username, password, email);
    }

    // ==========================================
    // GHI ĐÈ PHƯƠNG THỨC (Đa hình - Polymorphism)
    // ==========================================
    public void displayRole() {
        System.out.println("--- THÔNG TIN NGƯỜI BÁN ---");

        // Thông tin tài khoản
        System.out.println("Username : " + getUsername());
        System.out.println("Email    : " + getEmail());

        // Vai trò + quyền hạn
        System.out.println("Vai trò  : Seller (Người bán)");
        System.out.println("Quyền    : Đăng bán sản phẩm, quản lý phiên đấu giá");

        System.out.println("---------------------------");
    }

    // ==========================================
    // GHI ĐÈ toString() (hỗ trợ debug/in nhanh)
    // ==========================================
    @Override
    public String toString() {
        return "Seller{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}