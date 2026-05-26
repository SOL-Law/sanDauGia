package client.ui.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import frontend.AuctionUI;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

public class AuthFrame extends JFrame {

    private CardLayout layout = new CardLayout();
    private FadePanel content = new FadePanel(layout);
    private AnimatedBackground bg;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    // Biến lưu trữ tên đăng nhập của phiên hiện tại
    public String currentUsername = "admin";

    public AuthFrame() {
        connectServer();
        startListenThread();

        setTitle("Auction Auth");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screen.width * 0.8), (int) (screen.height * 0.8));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Lưu ý: Đảm bảo bạn đã có các file ảnh này trong thư mục resources
        bg = new AnimatedBackground("/images/bg1.jpg", "/images/bg2.jpg");
        bg.setLayout(new BorderLayout());

        LoginPanel login = new LoginPanel(this, out, gson);
        RegisterPanel register = new RegisterPanel(this, out, gson);

        content.setOpaque(false);
        content.add(login, "login");
        content.add(register, "register");

        bg.add(content, BorderLayout.CENTER);
        add(bg);
        setVisible(true);
    }

    // Hàm bổ sung giúp LoginPanel cập nhật tên đăng nhập thực tế khi bấm nút Đăng nhập
    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    private void connectServer() {
        try {
            socket = new Socket("localhost", 8888);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            gson = new Gson();
            System.out.println("✅ Connected to Server");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không kết nối được server!");
            System.exit(0);
        }
    }

    private void startListenThread() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    Request res = gson.fromJson(msg, Request.class);
                    if (res == null || res.getType() == null) {
                        continue;
                    }
                    switch (res.getType()) {
                        case "LOGIN_SUCCESS":
                            try {
                                JsonObject obj = gson.fromJson(res.getPayload(), JsonObject.class);
                                String role = obj.get("role").getAsString();
                                loginSuccess(role);
                            } catch (JsonSyntaxException ex) {
                                loginSuccess("USER");
                            }
                            return; // Thoát luồng nghe để chuyển giao diện

                        case "LOGIN_FAIL":
                            SwingUtilities.invokeLater(() -> {
                                shakeWindow();
                                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu");
                            });
                            break;

                        case "REGISTER_SUCCESS":
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
                                showLogin();
                            });
                            break;

                        case "REGISTER_FAIL":
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Tài khoản đã tồn tại!"));
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void loginSuccess(String role) {
        SwingUtilities.invokeLater(() -> showProLoading(role));
    }

    private void showProLoading(String role) {
        JDialog loading = new JDialog(this, false);
        loading.setUndecorated(true);
        loading.setSize(520, 300);
        loading.setLocationRelativeTo(this);
        loading.setBackground(new Color(0, 0, 0, 0));

        LoadingPanel panel = new LoadingPanel();
        loading.add(panel);
        loading.setVisible(true);

        Timer textTimer = new Timer(450, null);
        final int[] dot = {0};

        textTimer.addActionListener(e -> {
            dot[0]++;
            StringBuilder s = new StringBuilder("Opening Auction Center");
            for (int i = 0; i < dot[0] % 4; i++) s.append(".");
            panel.setText(s.toString());
        });
        textTimer.start();

        Timer timer = new Timer(2600, e -> {
            textTimer.stop();
            loading.dispose();

            // Truyền thông tin đăng nhập thực tế vào màn hình chính chính xác
            AuctionUI ui = new AuctionUI(out, in, gson, role, currentUsername);

            try {
                ui.setOpacity(0f);
                ui.setVisible(true);
                slideFadeIn(ui);
            } catch (Exception ex) {
                ui.setVisible(true);
            }
            dispose();
        });

        timer.setRepeats(false);
        timer.start();
    }

    /**
     * 🔥 ĐÃ NÂNG CẤP: Hiệu ứng rung màn hình khi lỗi theo thuật toán giảm chấn mượt mà
     */
    private void shakeWindow() {
        final Point originalPos = getLocation();
        final int[] shakeSequence = {16, -14, 12, -10, 8, -6, 4, -2, 0};
        Timer timer = new Timer(20, null);
        final int[] index = {0};

        timer.addActionListener(e -> {
            if (index[0] < shakeSequence.length) {
                int offset = shakeSequence[index[0]];
                setLocation(originalPos.x + offset, originalPos.y);
                index[0]++;
            } else {
                timer.stop();
                setLocation(originalPos); // Trả lại vị trí ban đầu chuẩn xác
            }
        });
        timer.start();
    }

    /**
     * 🔥 ĐÃ NÂNG CẤP: Hiệu ứng Slide-In lướt mượt theo thuật toán Ease-Out (Chạy nhanh đầu, chậm dần về cuối)
     */
    private void slideFadeIn(JFrame frame) {
        final Point targetPos = frame.getLocation();
        final int startX = targetPos.x + 150; // Xuất phát từ phía bên phải 150px
        frame.setLocation(startX, targetPos.y);

        Timer timer = new Timer(15, null);
        final float[] opacity = {0f};

        timer.addActionListener((ActionEvent e) -> {
            // Tăng độ mờ dần đều
            opacity[0] += 0.04f;
            frame.setOpacity(Math.min(opacity[0], 1f));

            // Thuật toán gắp khoảng cách Ease-Out giúp di chuyển mượt mà không bị khựng
            int currentX = frame.getX();
            int diff = targetPos.x - currentX;

            if (Math.abs(diff) > 1 && opacity[0] < 1f) {
                // Mỗi khung hình đi 15% khoảng cách còn lại đến đích
                currentX += Math.round(diff * 0.15f);
                frame.setLocation(currentX, targetPos.y);
            } else {
                frame.setOpacity(1f);
                frame.setLocation(targetPos);
                timer.stop();
            }
        });
        timer.start();
    }

    public void showRegister() {
        layout.show(content, "register");
        if (bg != null) bg.switchBackground();
    }

    public void showLogin() {
        layout.show(content, "login");
        if (bg != null) bg.switchBackground();
    }

    public static void main(String[] args) {
        try {
            // 1. Kích hoạt cấu trúc giao diện FlatLaf cao cấp giả lập Mac Dark Mode
            FlatMacDarkLaf.setup();

            // 2. Cài đặt các thuộc tính thẩm mỹ bo góc toàn hệ thống ứng dụng (Global)
            UIManager.put("Component.arc", 12);           // Bo góc toàn bộ ô Textfield, JComboBox
            UIManager.put("Button.arc", 12);              // Bo góc toàn bộ các JButton
            UIManager.put("ScrollBar.thumbArc", 999);     // Biến thanh cuộn thành thanh thuôn bo tròn
            UIManager.put("TableHeader.background", "#252526"); // Đổi màu nền thanh tiêu đề bảng

        } catch (Exception ex) {
            System.err.println("⚠️ Không thể kích hoạt giao diện FlatLaf, hệ thống sẽ dùng giao diện mặc định.");
        }

        // 3. Khởi chạy cửa sổ đúng luồng an toàn luồng của Swing (Đã sửa lỗi cú pháp dính luồng)
        SwingUtilities.invokeLater(AuthFrame::new);
    }
}

/**
 * Panel màn hình chờ xoay tròn hiển thị hiệu ứng đồ họa chất lượng cao
 */
class LoadingPanel extends JPanel {
    private float angle = 0f;
    private String text = "Opening Auction Center";

    public LoadingPanel() {
        setOpaque(false);
        // Tốc độ quay của vòng cung tải trang
        Timer timer = new Timer(16, e -> {
            angle += 5f;
            repaint();
        });
        timer.start();
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // BẬT KHỬ RĂNG CƯA ĐỒ HỌA: Giúp chữ và vòng tròn mịn màng, không bị gai vỡ hạt
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Vẽ nền Panel bo góc trong suốt huyền ảo
        g2.setColor(new Color(18, 18, 18, 235));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        // Cấu hình nét vẽ vòng tròn loading rực rỡ
        g2.setColor(new Color(90, 140, 255));
        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(getWidth() / 2 - 35, 65, 70, 70, (int) angle, 270);

        // Vẽ chữ tiêu đề WELCOME BACK sang trọng
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        drawCenter(g2, "WELCOME BACK", 165);

        // Vẽ dòng chữ mô tả trạng thái chuyển đổi bên dưới
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        g2.setColor(Color.LIGHT_GRAY);
        drawCenter(g2, text, 220);

        g2.dispose();
    }

    private void drawCenter(Graphics2D g2, String s, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(s)) / 2;
        g2.drawString(s, x, y);
    }
}