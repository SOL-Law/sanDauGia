package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Base64;
import java.util.function.Consumer;
import java.awt.image.BufferedImage;

public class DesktopItemIcon extends JPanel {

    private boolean hovered = false;
    private boolean selected = false;
    private static DesktopItemIcon currentSelected;
    private String itemName;
    private JLabel timerLabel;

    // Màu nền Glassmorphism (có thêm kênh Alpha độ mờ)
    private final Color COLOR_CARD_BG = new Color(30, 30, 35, 150); // Mờ 150/255 để thấy ảnh nền JPG
    private final Color COLOR_CARD_HOVER = new Color(45, 45, 50, 200);
    private final Color COLOR_CARD_SELECT = new Color(40, 55, 90, 220); // Xanh sẫm huyền bí

    public DesktopItemIcon(String name, String price, String leader, String base64Image, String remainingTime, Consumer<String> onClick) {
        this.itemName = name;
        setLayout(new BorderLayout(20, 0));

        // Cực kì quan trọng để tự vẽ bo góc mà không bị dính viền đen cứng
        setOpaque(false);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        setPreferredSize(new Dimension(800, 125));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel icon = new JLabel(loadIcon(name, base64Image));
        icon.setPreferredSize(new Dimension(100, 100));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false); // Trong suốt để ăn theo nền bo góc tự vẽ

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        nameLabel.setForeground(Color.WHITE);

        final int[] timeLeft = {0};
        try { timeLeft[0] = Integer.parseInt(remainingTime); } catch(Exception e){}

        timerLabel = new JLabel(formatTime(timeLeft[0]));
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timerLabel.setForeground(timeLeft[0] <= 10 ? new Color(255, 75, 75) : new Color(120, 255, 160)); // Đỏ hoặc Xanh lá Neon

        Timer uiTimer = new Timer(1000, e -> {
            if (timeLeft[0] > 0) {
                timeLeft[0]--;
                timerLabel.setText(formatTime(timeLeft[0]));
                timerLabel.setForeground(timeLeft[0] <= 10 ? new Color(255, 75, 75) : new Color(120, 255, 160));
            } else {
                ((Timer)e.getSource()).stop();
                timerLabel.setText("⏰ Đã kết thúc");
                timerLabel.setForeground(Color.GRAY);
            }
        });
        uiTimer.start();

        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(timerLabel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        String formattedPrice = price;
        try { formattedPrice = String.format("%,d", Integer.parseInt(price)); } catch (Exception ignored){}

        JLabel priceLabel = new JLabel(formattedPrice + " VNĐ");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        priceLabel.setForeground(new Color(255, 110, 110)); // Đỏ sáng nổi bật

        JLabel leaderLabel = new JLabel(leader.equals("None") ? "Chưa có ai đặt giá" : "👑 Top bid: " + leader);
        leaderLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        leaderLabel.setForeground(new Color(200, 200, 210)); // Trắng xám sang trọng

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(priceLabel);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(leaderLabel);

        add(icon, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                hovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }
            public void mouseClicked(MouseEvent e) {
                if(currentSelected != null) {
                    currentSelected.selected = false;
                    currentSelected.repaint();
                }
                selected = true;
                currentSelected = DesktopItemIcon.this;
                repaint();
                onClick.accept(itemName);
            }
        });
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return " Đã kết thúc";
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (days > 0) return String.format(" ⏳ %d ngày %02d:%02d:%02d", days, hours, minutes, seconds);
        else if (hours > 0) return String.format(" ⏳ %02d:%02d:%02d", hours, minutes, seconds);
        else return String.format(" ⚡ %02d:%02d", minutes, seconds);
    }

    private ImageIcon loadIcon(String name, String base64) {
        try {
            if(base64 != null && !base64.isEmpty() && !base64.equals("none")) {
                byte[] bytes = Base64.getDecoder().decode(base64);
                Image img = new ImageIcon(bytes).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        String path = name.toLowerCase().contains("laptop") ? "laptop.png" : name.toLowerCase().contains("phone") ? "phone.png" : "watch.png";
        try {
            return new ImageIcon(new ImageIcon("src/main/java/frontend/icons/" + path).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền động thay đổi theo trạng thái
        if (selected) {
            g2.setColor(COLOR_CARD_SELECT);
        } else if (hovered) {
            g2.setColor(COLOR_CARD_HOVER);
        } else {
            g2.setColor(COLOR_CARD_BG);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        // Viền động phát sáng
        if (selected) {
            g2.setColor(new Color(90, 140, 255));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
        } else if (hovered) {
            g2.setColor(new Color(255, 255, 255, 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        } else {
            g2.setColor(new Color(255, 255, 255, 25));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        }

        g2.dispose();
        // Gọi super sau khi đã tự vẽ nền bo góc
        super.paintComponent(g);
    }
}