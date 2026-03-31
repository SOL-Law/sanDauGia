package frontend;

import javax.swing.*;
import java.awt.*;

public class MainUI {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Auction System");

        frame.setSize(800,600);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load ảnh nền
        ImageIcon backgroundImage = new ImageIcon("src/main/java/frontend/background.jpg");

        JLabel background = new JLabel(backgroundImage);

        background.setLayout(new FlowLayout());

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);

        JTextField usernameField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);

        JPasswordField passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {

            String username = usernameField.getText();

            String password = new String(passwordField.getPassword());

            JOptionPane.showMessageDialog(frame,
                    "Username: " + username +
                            "\nPassword: " + password);

        });

        background.add(userLabel);
        background.add(usernameField);

        background.add(passLabel);
        background.add(passwordField);

        background.add(loginButton);

        frame.setContentPane(background);

        frame.setVisible(true);
    }
}