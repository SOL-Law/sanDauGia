package client.ui.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import frontend.AuctionUI;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class AuthFrame extends JFrame {

    private CardLayout layout = new CardLayout();
    private FadePanel content = new FadePanel(layout);
    private AnimatedBackground bg;

    // NETWORK
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    public AuthFrame() {

        // ===== CONNECT SERVER =====
        try {

            socket = new Socket("localhost", 8888);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            gson = new Gson();

            System.out.println("✅ Connected to server");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Không kết nối được server!");
            System.exit(0);
        }

        // ===== THREAD NGHE SERVER =====
        new Thread(() -> {

            try {

                String msg;

                while ((msg = in.readLine()) != null) {

                    System.out.println("Server response: " + msg);

                    Request res = gson.fromJson(msg, Request.class);

                    switch (res.getType()) {   // 🔥 FIX Ở ĐÂY

                        case "LOGIN_SUCCESS":

                            try {

                                JsonObject obj =
                                        gson.fromJson(
                                                res.getPayload(),
                                                JsonObject.class
                                        );

                                String role =
                                        obj.get("role").getAsString();

                                loginSuccess(role);

                            } catch (JsonSyntaxException e) {

                                System.out.println(
                                        "Payload không phải JSON -> fallback USER"
                                );

                                loginSuccess("USER");
                            }

                            break;

                        case "LOGIN_FAIL":

                            JOptionPane.showMessageDialog(
                                    this,
                                    "Sai tài khoản hoặc mật khẩu"
                            );

                            break;

                        case "REGISTER_SUCCESS":

                            JOptionPane.showMessageDialog(
                                    this,
                                    "Đăng ký thành công!"
                            );

                            break;

                        case "ERROR":

                            JOptionPane.showMessageDialog(
                                    this,
                                    res.getPayload()
                            );

                            break;
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }).start();

        // ===== UI =====

        setTitle("Auction Auth");

        Dimension screen =
                Toolkit.getDefaultToolkit().getScreenSize();

        setSize(
                (int) (screen.width * 0.8),
                (int) (screen.height * 0.8)
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        bg = new AnimatedBackground(
                "/images/bg1.jpg",
                "/images/bg2.jpg"
        );

        bg.setLayout(new BorderLayout());

        LoginPanel login =
                new LoginPanel(this, out, gson);

        RegisterPanel register =
                new RegisterPanel(this, out, gson);

        content.setOpaque(false);

        content.add(login, "login");
        content.add(register, "register");

        bg.add(content, BorderLayout.CENTER);

        add(bg);

        setVisible(true);
    }

    // =========================
    // LOGIN SUCCESS
    // =========================

    public void loginSuccess(String role) {

        SwingUtilities.invokeLater(() -> {

            new AuctionUI(out, in, gson, role).setVisible(true);

            dispose();

        });
    }

    // =========================
    // SWITCH UI
    // =========================

    public void showRegister() {

        layout.show(content, "register");

        bg.switchBackground();
    }

    public void showLogin() {

        layout.show(content, "login");

        bg.switchBackground();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(AuthFrame::new);
    }
}