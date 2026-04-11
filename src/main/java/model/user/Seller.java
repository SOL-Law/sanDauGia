package model.user;

public class Seller extends User {

    public Seller() {
        super();
        setRole("SELLER");
    }

    public Seller(int id, String username, String password, String email) {
        super(id, username, password, email, "SELLER");
    }

    public void displayRole() {
        System.out.println("--- SELLER ---");
        System.out.println("Username: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("Role: SELLER");
    }

    @Override
    public String toString() {
        return "Seller{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}