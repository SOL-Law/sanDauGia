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

public class AuthFrame extends JFrame {

    private CardLayout layout = new CardLayout();
    private FadePanel content = new FadePanel(layout);
    private AnimatedBackground bg;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    // 🔥 THÊM BIẾN NÀY ĐỂ LƯU TRỮ TÊN ĐĂNG NHẬP
    public String currentUsername = "admin";

    public AuthFrame() {
        connectServer();
        startListenThread();

        setTitle("Auction Auth");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screen.width * 0.8), (int) (screen.height * 0.8));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

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

    private void connectServer() {
        try {
            socket = new Socket("localhost", 8888);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            gson = new Gson();
            System.out.println("✅ Connected");
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
                    switch (res.getType()) {
                        case "LOGIN_SUCCESS":
                            try {
                                JsonObject obj = gson.fromJson(res.getPayload(), JsonObject.class);
                                String role = obj.get("role").getAsString();
                                loginSuccess(role);
                            } catch (JsonSyntaxException ex) {
                                loginSuccess("USER");
                            }
                            return;

                        case "LOGIN_FAIL":
                            SwingUtilities.invokeLater(this::shakeWindow);
                            JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu");
                            break;

                        case "REGISTER_SUCCESS":
                            JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
                            break;

                        case "REGISTER_FAIL":
                            JOptionPane.showMessageDialog(this, "Tài khoản đã tồn tại!");
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
        loading.setBackground(new Color(0,0,0,0));

        LoadingPanel panel = new LoadingPanel();
        loading.add(panel);
        loading.setVisible(true);

        Timer textTimer = new Timer(450, null);
        final int[] dot = {0};

        textTimer.addActionListener(e -> {
            dot[0]++;
            String s = "Opening Auction Center";
            for(int i=0; i<dot[0]%4; i++) s += ".";
            panel.setText(s);
        });
        textTimer.start();

        Timer timer = new Timer(2600, e -> {
            textTimer.stop();
            loading.dispose();

            // 🔥 TRUYỀN BIẾN currentUsername VÀO ĐÂY (HẾT BÁO ĐỎ LÒM!)
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

    private void shakeWindow() {
        Point p = getLocation();
        Timer timer = new Timer(18, null);
        final int[] count = {0};
        timer.addActionListener(e -> {
            int x = p.x + (count[0] % 2 == 0 ? 12 : -12);
            setLocation(x, p.y);
            count[0]++;
            if (count[0] >= 10) {
                timer.stop();
                setLocation(p);
            }
        });
        timer.start();
    }

    private void slideFadeIn(JFrame frame) {
        Point end = frame.getLocation();
        frame.setLocation(end.x + 140, end.y);
        Timer timer = new Timer(15, null);
        final float[] opacity = {0f};
        final int[] dx = {140};

        timer.addActionListener((ActionEvent e) -> {
            opacity[0] += 0.05f;
            dx[0] -= 7;
            frame.setOpacity(Math.min(opacity[0], 1f));
            frame.setLocation(end.x + Math.max(dx[0], 0), end.y);
            if(opacity[0] >= 1f && dx[0] <= 0) {
                timer.stop();
            }
        });
        timer.start();
    }

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

class LoadingPanel extends JPanel {
    private float angle = 0f;
    private String text = "Opening Auction Center";

    public LoadingPanel() {
        setOpaque(false);
        Timer timer = new Timer(16, e -> { angle += 6f; repaint(); });
        timer.start();
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(18,18,18,235));
        g2.fillRoundRect(0,0,getWidth(),getHeight(), 30,30);
        g2.setColor(new Color(90,140,255));
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(getWidth()/2 - 35, 70, 70, 70, (int)angle, 260);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        drawCenter(g2, "WELCOME BACK", 160);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g2.setColor(Color.LIGHT_GRAY);
        drawCenter(g2, text, 215);
        g2.dispose();
    }

    private void drawCenter(Graphics2D g2, String s, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(s)) / 2;
        g2.drawString(s, x, y);
    }
}