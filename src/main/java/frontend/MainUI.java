package frontend;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainUI {

    // Khai báo các "ống hút" mạng ở ngoài để nút bấm có thể dùng được
    private static PrintWriter out;
    private static BufferedReader in;
    private static Gson gson = new Gson();

    public static void main(String[] args) {

        JFrame frame = new JFrame("Hệ thống Đấu giá - Nhóm Trọng");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load ảnh nền (Nhớ đảm bảo file ảnh phải nằm đúng đường dẫn này)
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

        // ==========================================
        // 1. KẾT NỐI SERVER NGAY KHI VỪA MỞ APP
        // ==========================================
        try {
            Socket socket = new Socket("localhost", 8888);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Tạo luồng chạy ngầm để nghe Tổng đài báo về (y hệt TestClient)
            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        // Dịch JSON Server gửi về
                        Request response = gson.fromJson(fromServer, Request.class);

                        // Nếu Server báo Đăng nhập thành công
                        if (response.getAction().equals("LOGIN_SUCCESS")) {
                            JOptionPane.showMessageDialog(frame, "Đăng nhập thành công! Chuẩn bị vào phòng...");
                            // Sau này nhóm bạn sẽ code lệnh tắt cửa sổ này và mở cửa sổ Đấu Giá ở đây
                        }
                        // Nếu Server báo Lỗi (Sai pass, v.v...)
                        else if (response.getAction().equals("ERROR")) {
                            JOptionPane.showMessageDialog(frame, response.getPayload(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Mất kết nối với Server.");
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Không thể kết nối đến Server. Vui lòng bật AuctionServer trước!", "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);
        }

        // ==========================================
        // 2. SỰ KIỆN KHI BẤM NÚT LOGIN
        // ==========================================
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (out != null) {
                // Đóng gói Username & Password thành chuỗi JSON
                String payload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
                Request loginReq = new Request("LOGIN", payload);

                // Bắn gói tin lên Server
                out.println(gson.toJson(loginReq));
            } else {
                JOptionPane.showMessageDialog(frame, "Chưa kết nối được Server, không thể gửi lệnh!");
            }
        });

        // Gắn đồ lên màn hình
        background.add(userLabel);
        background.add(usernameField);
        background.add(passLabel);
        background.add(passwordField);
        background.add(loginButton);

        frame.setContentPane(background);
        frame.setVisible(true);
    }
}