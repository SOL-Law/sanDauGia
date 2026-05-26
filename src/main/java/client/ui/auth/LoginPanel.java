package client.ui.auth;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class LoginPanel extends JPanel {

    public LoginPanel(AuthFrame frame,
                      PrintWriter out,
                      Gson gson) {

        setLayout(new GridBagLayout());
        setOpaque(false);

        GlassPanel form = new GlassPanel();
        form.setPreferredSize(new Dimension(400, 350));
        form.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);

        // ===== TITLE =====
        JLabel title = new JLabel("LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);

        c.gridy = 0;
        form.add(title, c);

        // ===== INPUT =====
        AnimatedTextField user = new AnimatedTextField("Username");
        user.setPreferredSize(new Dimension(300, 40));

        c.gridy = 1;
        form.add(user, c);

        JPasswordField pass = new JPasswordField();
        pass.setPreferredSize(new Dimension(300, 40));
        pass.setBackground(new Color(30,30,30));
        pass.setForeground(Color.WHITE);

        c.gridy = 2;
        form.add(pass, c);

        // ===== LOGIN BUTTON =====
        JButton loginBtn = new RippleButton("LOGIN");

        loginBtn.addActionListener(e -> {

            String username = user.getText().trim();
            String password = new String(pass.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
                return;
            }

            frame.setCurrentUsername(username);

            String payload = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    username, password
            );

            Request req = new Request("LOGIN", payload);
            out.println(gson.toJson(req));
        });

        c.gridy = 3;
        form.add(loginBtn, c);

        // ===== SWITCH =====
        JButton switchBtn = new JButton("Create account");
        switchBtn.setForeground(Color.LIGHT_GRAY);
        switchBtn.setBorderPainted(false);
        switchBtn.setContentAreaFilled(false);

        switchBtn.addActionListener(e -> frame.showRegister());

        c.gridy = 4;
        form.add(switchBtn, c);

        add(form);
    }
}