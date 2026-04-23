package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends GradientPanel {

    public LoginPanel(AuthFrame frame) {

        setLayout(new BorderLayout());

        // ===== FORM PANEL =====
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(350, 300));

        // ===== TITLE =====
        JLabel title = new JLabel("LOGIN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== INPUT =====
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();

        styleField(user);
        styleField(pass);

        // ===== BUTTON LOGIN =====
        JButton loginBtn = new JButton("LOGIN");
        styleButton(loginBtn);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== SWITCH BUTTON =====
        JButton switchBtn = new JButton("Create account");
        switchBtn.setBorderPainted(false);
        switchBtn.setContentAreaFilled(false);
        switchBtn.setForeground(Color.LIGHT_GRAY);
        switchBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        switchBtn.addActionListener(e -> frame.showRegister());

        // ===== ADD COMPONENT =====
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 30)));
        form.add(user);
        form.add(Box.createRigidArea(new Dimension(0, 15)));
        form.add(pass);
        form.add(Box.createRigidArea(new Dimension(0, 20)));
        form.add(loginBtn);
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(switchBtn);

        // ===== WRAPPER CENTER =====
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(form);

        add(wrapper, BorderLayout.CENTER);
    }

    // =========================
    // STYLE INPUT
    // =========================
    private void styleField(JTextField field) {
        field.setMaximumSize(new Dimension(300, 40));
        field.setBackground(new Color(255, 255, 255, 220));
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    // =========================
    // STYLE BUTTON
    // =========================
    private void styleButton(JButton btn) {
        btn.setBackground(new Color(0, 200, 255));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(300, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
}