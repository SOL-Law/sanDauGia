package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class NotificationToast extends JWindow {
    private float opacity = 0f;
    private int y;

    public NotificationToast(JFrame parent, String message) {
        super(parent);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0)); // Nền cửa sổ vô hình hoàn toàn
        setAlwaysOnTop(true);

        // Tạo khung nền mờ bo góc nghệ thuật
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Nền đen đặc cao cấp trong suốt nhẹ (Alpha = 230)
                g2.setColor(new Color(25, 25, 28, 230));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                // Thanh màu nhấn trang trí dọc bên trái (Xanh dương công nghệ)
                g2.setColor(new Color(90, 140, 255));
                g2.fillRoundRect(0, 0, 6, getHeight(), 14, 14);
                g2.fillRect(3, 0, 3, getHeight()); // Ghép vuông cạnh trong

                // Đường viền tổng thể mờ ảo
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(255, 255, 255, 30));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 22, 15, 22)); // Đệm chữ tránh đè thanh nhấn

        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);

        add(panel);
        pack();

        // Định vị tọa độ mục tiêu
        int startX = parent.getX() + parent.getWidth();
        int finalX = parent.getX() + parent.getWidth() - getWidth() - 20;
        y = parent.getY() + 50;

        setLocation(startX, y);
        setOpacity(0f);
        setVisible(true);

        // 🔥 THUẬT TOÁN TRƯỢT EASE-OUT MƯỢT MÀ (CHẠY NHANH ĐẦU, CHẬM DẦN VỀ CUỐI)
        Timer slideIn = new Timer(15, null);
        final int[] currentX = {startX};

        slideIn.addActionListener(e -> {
            opacity += 0.06f;
            if (opacity > 1f) opacity = 1f;
            setOpacity(opacity);

            int diffX = finalX - currentX[0];
            if (Math.abs(diffX) > 1) {
                // Di chuyển 15% khoảng cách còn lại ở mỗi khung hình để tạo hiệu ứng phanh xe mượt
                currentX[0] += Math.round(diffX * 0.15f);
                setLocation(currentX[0], y);
            } else {
                setLocation(finalX, y);
                slideIn.stop();
                // Dừng lại xem thông báo trong 3 giây rồi tự thụt lùi biến mất
                waitAndSlideOut(finalX, startX);
            }
        });
        slideIn.start();
    }

    private void waitAndSlideOut(int startX, int endX) {
        Timer waitTimer = new Timer(3000, e -> {
            Timer slideOut = new Timer(15, null);
            final int[] currentX = {startX};

            slideOut.addActionListener(e2 -> {
                opacity -= 0.06f;
                if (opacity < 0f) opacity = 0f;
                setOpacity(opacity);

                int diffX = endX - currentX[0];
                if (Math.abs(diffX) > 1 && opacity > 0f) {
                    currentX[0] += Math.round(diffX * 0.15f);
                    setLocation(currentX[0], y);
                } else {
                    slideOut.stop();
                    dispose(); // Giải phóng tài nguyên RAM hệ thống an toàn
                }
            });
            slideOut.start();
        });
        waitTimer.setRepeats(false);
        waitTimer.start();
    }

    public static void show(JFrame parent, String message) {
        SwingUtilities.invokeLater(() -> new NotificationToast(parent, message));
    }
}