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
    private static AuctionUI auctionUI;

    private static PrintWriter out;
    private static BufferedReader in;
    private static Gson gson = new Gson();

    public static void main(String[] args) {

        JFrame frame = new JFrame("Hệ thống Đấu giá - Nhóm Trọng");
        frame.setSize(800, 600);
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
        JButton registerButton = new JButton("Register");

        // ==========================================
        // 1. KẾT NỐI SERVER
        // ==========================================
        try {
            Socket socket = new Socket("localhost", 8888);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        Request response = gson.fromJson(fromServer, Request.class);

                        // =========================
                        // XỬ LÝ RESPONSE
                        // =========================
                        if (response.getAction().equals("LOGIN_SUCCESS")) {

                            JOptionPane.showMessageDialog(frame, "Đăng nhập thành công!");

                            // mở UI đấu giá
                            auctionUI = new AuctionUI(out, gson);
                            auctionUI.setVisible(true);

                            // đóng login
                            frame.dispose();

                            // lấy dữ liệu đấu giá
                            Request req = new Request("GET_AUCTION", "");
                            out.println(gson.toJson(req));

                        } else if (response.getAction().equals("AUCTION_UPDATE")) {

                            if (auctionUI != null) {
                                auctionUI.updateAuctionInfo(response.getPayload());
                            }

                        } else if (response.getAction().equals("UPDATE_PRICE")) {

                            if (auctionUI != null) {
                                auctionUI.updateAuctionInfo(response.getPayload());
                            }

                        } else if (response.getAction().equals("AUCTION_END")) {

                            JOptionPane.showMessageDialog(frame, response.getPayload());

                        } else if (response.getAction().equals("REGISTER_SUCCESS")) {

                            JOptionPane.showMessageDialog(frame, "Tạo tài khoản thành công!");

                        } else if (response.getAction().equals("ERROR")) {

                            JOptionPane.showMessageDialog(frame, response.getPayload(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Mất kết nối với Server.");
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Không thể kết nối đến Server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // ==========================================
        // LOGIN
        // ==========================================
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (out != null) {
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Nhập đủ thông tin!");
                    return;
                }

                String payload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
                Request loginReq = new Request("LOGIN", payload);
                out.println(gson.toJson(loginReq));
            }
        });

        // ==========================================
        // REGISTER
        // ==========================================
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (out != null) {
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Nhập đủ thông tin!");
                    return;
                }

                String payload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
                Request regReq = new Request("REGISTER", payload);
                out.println(gson.toJson(regReq));
            }
        });

        // UI
        background.add(userLabel);
        background.add(usernameField);
        background.add(passLabel);
        background.add(passwordField);
        background.add(loginButton);
        background.add(registerButton);

        frame.setContentPane(background);
        frame.setVisible(true);
    }
}