package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Base64;
import java.util.function.Consumer;

public class DesktopItemIcon extends JPanel {

    private boolean hovered = false;
    private boolean selected = false;
    private static DesktopItemIcon currentSelected;
    private String itemName;
    private JLabel timerLabel;

    public DesktopItemIcon(String name, String price, String leader, String base64Image, String remainingTime, Consumer<String> onClick) {
        this.itemName = name;

        setLayout(new BorderLayout(20, 0));
        setBackground(Color.WHITE); // Phông nền trắng
        setOpaque(true);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        setPreferredSize(new Dimension(800, 120));

        // Đường phân cách mờ mờ ở dưới
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel icon = new JLabel(loadIcon(name, base64Image));
        icon.setPreferredSize(new Dimension(100, 100));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(Color.BLACK); // Tên màu đen

        final int[] timeLeft = {0};
        try { timeLeft[0] = Integer.parseInt(remainingTime); } catch(Exception e){}

        timerLabel = new JLabel(formatTime(timeLeft[0]));
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        timerLabel.setForeground(timeLeft[0] <= 10 ? Color.RED : new Color(0, 150, 50)); // Xanh lá cây đậm

        Timer uiTimer = new Timer(1000, e -> {
            if (timeLeft[0] > 0) {
                timeLeft[0]--;
                timerLabel.setText(formatTime(timeLeft[0]));
                timerLabel.setForeground(timeLeft[0] <= 10 ? Color.RED : new Color(0, 150, 50));
            } else {
                ((Timer)e.getSource()).stop();
                timerLabel.setText("⏱️ Đã kết thúc");
                timerLabel.setForeground(Color.GRAY);
            }
        });
        uiTimer.start();

        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(timerLabel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        JLabel priceLabel = new JLabel(price + " VNĐ");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        priceLabel.setForeground(new Color(210, 0, 0)); // Giá tiền đỏ chót giống eBay

        JLabel leaderLabel = new JLabel(leader.equals("None") ? "Chưa có ai đặt giá" : "Top bid: " + leader);
        leaderLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        leaderLabel.setForeground(Color.DARK_GRAY);

        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(priceLabel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(leaderLabel);

        add(icon, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Hiệu ứng di chuột đổi màu xám/xanh nhạt siêu mượt
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                if (!selected) setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(MouseEvent e) {
                hovered = false;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                setBackground(selected ? new Color(230, 245, 255) : Color.WHITE);
            }
            public void mouseClicked(MouseEvent e) {
                if(currentSelected != null) {
                    currentSelected.selected = false;
                    currentSelected.setBackground(Color.WHITE);
                }
                selected = true;
                currentSelected = DesktopItemIcon.this;
                setBackground(new Color(230, 245, 255));
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
        if (days > 0) return String.format(" %d ngày %02d:%02d:%02d", days, hours, minutes, seconds);
        else if (hours > 0) return String.format(" %02d:%02d:%02d", hours, minutes, seconds);
        else return String.format(" %02d:%02d", minutes, seconds);
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
        return new ImageIcon(new ImageIcon("src/main/java/frontend/icons/" + path).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
    }
}