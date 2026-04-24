package client.ui.auth;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class RegisterPanel extends JPanel {

    public RegisterPanel(AuthFrame frame,
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

        JLabel title = new JLabel("REGISTER", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);

        c.gridy = 0;
        form.add(title, c);

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

        JButton registerBtn = new RippleButton("CREATE");

        registerBtn.addActionListener(e -> {

            String username = user.getText().trim();
            String password = new String(pass.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
                return;
            }

            String payload = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    username, password
            );

            Request req = new Request("REGISTER", payload);
            out.println(gson.toJson(req));
        });

        c.gridy = 3;
        form.add(registerBtn, c);

        JButton switchBtn = new JButton("Back to login");
        switchBtn.setForeground(Color.LIGHT_GRAY);
        switchBtn.setBorderPainted(false);
        switchBtn.setContentAreaFilled(false);

        switchBtn.addActionListener(e -> frame.showLogin());

        c.gridy = 4;
        form.add(switchBtn, c);

        add(form);
    }
}