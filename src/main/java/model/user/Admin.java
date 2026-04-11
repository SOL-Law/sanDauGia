package model.user;

public class Admin extends User {

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String username, String password, String email) {
        super(id, username, password, email, "ADMIN");
    }

    public void displayRole() {
        System.out.println("--- ADMIN ---");
        System.out.println("Username: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("Role: ADMIN");
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}