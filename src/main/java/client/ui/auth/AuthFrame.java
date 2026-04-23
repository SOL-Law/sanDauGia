package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {

    CardLayout layout = new CardLayout();
    JPanel container = new JPanel(layout);

    public AuthFrame() {
        setTitle("Auction Auth ProMax");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        LoginPanel login = new LoginPanel(this);
        RegisterPanel register = new RegisterPanel(this);

        container.add(login, "login");
        container.add(register, "register");

        add(container);
        setVisible(true);
    }

    public void showRegister() {
        layout.show(container, "register");
    }

    public void showLogin() {
        layout.show(container, "login");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthFrame::new);
    }
}