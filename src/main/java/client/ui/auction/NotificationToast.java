package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class NotificationToast extends JWindow {
    private float opacity = 0f;
    private int y;

    public NotificationToast(JFrame parent, String message) {
        super(parent);

        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0)); // Nền cửa sổ vô hình
        setAlwaysOnTop(true);

        // Tạo khung nền mờ (Translucent)
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền đen mờ 180
                g2.setColor(new Color(30, 30, 30, 180));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Viền sáng nhẹ
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(100, 150, 255, 200));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Chữ màu trắng tinh, in đậm rất rõ nét
        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);

        add(panel);
        pack();

        // Căn tọa độ: Góc trên bên phải của Giao diện chính
        int startX = parent.getX() + parent.getWidth();
        int finalX = parent.getX() + parent.getWidth() - getWidth() - 20;
        y = parent.getY() + 40;

        setLocation(startX, y);
        setOpacity(0f);
        setVisible(true);

        // Hiệu ứng 1: Trượt từ mép phải vào trong màn hình
        Timer slideIn = new Timer(10, null);
        slideIn.addActionListener(new ActionListener() {
            int currentX = startX;
            @Override
            public void actionPerformed(ActionEvent e) {
                currentX -= 10;
                opacity += 0.05f;
                if (opacity > 1f) opacity = 1f;
                setOpacity(opacity);

                if (currentX <= finalX) {
                    currentX = finalX;
                    setLocation(currentX, y);
                    slideIn.stop();
                    // Chờ đúng 3 giây rồi mới thụt ra
                    waitAndSlideOut(finalX, startX);
                } else {
                    setLocation(currentX, y);
                }
            }
        });
        slideIn.start();
    }

    // Hiệu ứng 2: Đợi 3 giây rồi trượt dần ra ngoài và biến mất
    private void waitAndSlideOut(int startX, int endX) {
        Timer waitTimer = new Timer(3000, e -> {
            Timer slideOut = new Timer(10, null);
            slideOut.addActionListener(new ActionListener() {
                int currentX = startX;
                @Override
                public void actionPerformed(ActionEvent e2) {
                    currentX += 10;
                    opacity -= 0.05f;
                    if (opacity < 0f) opacity = 0f;
                    setOpacity(opacity);

                    if (currentX >= endX || opacity <= 0f) {
                        slideOut.stop();
                        dispose(); // Hủy bỏ giải phóng RAM
                    } else {
                        setLocation(currentX, y);
                    }
                }
            });
            slideOut.start();
        });
        waitTimer.setRepeats(false);
        waitTimer.start();
    }

    // Hàm gọi nhanh cho gọn code
    public static void show(JFrame parent, String message) {
        new NotificationToast(parent, message);
    }
}