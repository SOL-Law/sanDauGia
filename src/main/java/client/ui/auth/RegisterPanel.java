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
        // Tăng chiều cao form lên một chút (từ 350 lên 420) để chứa thêm ô chọn quyền
        form.setPreferredSize(new Dimension(400, 420));
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

        // THÊM HỘP CHỌN VAI TRÒ (BIDDER / SELLER)
        String[] roles = {"BIDDER", "SELLER"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setPreferredSize(new Dimension(300, 40));
        roleCombo.setBackground(new Color(30, 30, 30)); // Cùng tone màu với ô Pass
        roleCombo.setForeground(Color.WHITE);
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        c.gridy = 3; // Đẩy xuống hàng thứ 3
        form.add(roleCombo, c);

        JButton registerBtn = new RippleButton("CREATE");

        registerBtn.addActionListener(e -> {

            String username = user.getText().trim();
            String password = new String(pass.getPassword());

            // Lấy chức vụ mà người dùng vừa chọn
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
                return;
            }

            //  CẬP NHẬT PAYLOAD: Nhét thêm cái role vào chuỗi JSON
            String payload = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                    username, password, role
            );

            Request req = new Request("REGISTER", payload);
            out.println(gson.toJson(req));
        });

        c.gridy = 4; // Đẩy nút CREATE xuống hàng thứ 4
        form.add(registerBtn, c);

        JButton switchBtn = new JButton("Back to login");
        switchBtn.setForeground(Color.LIGHT_GRAY);
        switchBtn.setBorderPainted(false);
        switchBtn.setContentAreaFilled(false);

        switchBtn.addActionListener(e -> frame.showLogin());

        c.gridy = 5; // Đẩy nút Back xuống hàng thứ 5
        form.add(switchBtn, c);

        add(form);
    }
}