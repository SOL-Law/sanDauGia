package Model.User;
// Kế thừa phân cấp rõ ràng
public class Bidder extends User {

    public Bidder(int id, String username, String password, String email) {
        super(id, username, password, email);
    }

    // Ghi đè phương thức để thể hiện tính đa hình (Polymorphism) [cite: 121]
    @Override
    public void displayRole() {
        System.out.println("Vai trò: Người tham gia đấu giá (Bidder)");
    }
}