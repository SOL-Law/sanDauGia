package frontend;

import javax.swing.*;
import java.awt.*;

public class MainUI {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Auction System");

        frame.setSize(800,600);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new FlowLayout());

        JLabel userLabel = new JLabel("Username:");

        JTextField usernameField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");

        JPasswordField passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");

        frame.add(userLabel);
        frame.add(usernameField);

        frame.add(passLabel);
        frame.add(passwordField);

        frame.add(loginButton);

        frame.setVisible(true);
    }
}