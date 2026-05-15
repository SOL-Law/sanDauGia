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

    public DesktopItemIcon(
            String name,
            String price,
            String leader,
            String base64Image,
            String remainingTime,
            Consumer<String> onClick
    ) {
        this.itemName = name;

        setPreferredSize(new Dimension(180, 200));
        setMinimumSize(new Dimension(180, 200));
        setMaximumSize(new Dimension(180, 200));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JLabel icon = new JLabel(loadIcon(name, base64Image));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel(price + " VNĐ");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        priceLabel.setForeground(new Color(255, 215, 0));
        priceLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel leaderLabel = new JLabel("Top: " + leader);
        leaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leaderLabel.setForeground(Color.LIGHT_GRAY);
        leaderLabel.setAlignmentX(CENTER_ALIGNMENT);

        // 🔥 TẠO ĐỒNG HỒ ĐẾM NGƯỢC THÔNG MINH 🔥
        final int[] timeLeft = {0};
        try { timeLeft[0] = Integer.parseInt(remainingTime); } catch(Exception e){}

        // Gọi hàm formatTime để hiện chữ đẹp ngay từ giây đầu tiên
        timerLabel = new JLabel(formatTime(timeLeft[0]));
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timerLabel.setForeground(timeLeft[0] <= 10 ? Color.RED : new Color(0, 255, 150));
        timerLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Tạo nhịp tim đập mỗi 1 giây (1000ms)
        Timer uiTimer = new Timer(1000, e -> {
            if (timeLeft[0] > 0) {
                timeLeft[0]--;
                // Gọi hàm format để cập nhật chữ mỗi giây
                timerLabel.setText(formatTime(timeLeft[0]));
                timerLabel.setForeground(timeLeft[0] <= 10 ? Color.RED : new Color(0, 255, 150));
            } else {
                ((Timer)e.getSource()).stop(); // Dừng nhịp tim
                timerLabel.setText(" KẾT THÚC");
                timerLabel.setForeground(Color.GRAY);
            }
        });
        uiTimer.start();

        add(Box.createVerticalStrut(10));
        add(icon);
        add(Box.createVerticalStrut(8));
        add(nameLabel);
        add(Box.createVerticalStrut(2));
        add(priceLabel);
        add(Box.createVerticalStrut(2));
        add(leaderLabel);
        add(Box.createVerticalStrut(5));
        add(timerLabel);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            public void mouseClicked(MouseEvent e) {
                if(currentSelected != null) {
                    currentSelected.selected = false;
                    currentSelected.repaint();
                }
                selected = true;
                currentSelected = DesktopItemIcon.this;
                onClick.accept(itemName);
                repaint();
            }
        });
    }

    // ===========================
    // HÀM DỊCH THUẬT THỜI GIAN (SIÊU XỊN)
    // ===========================
    private String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) return " KẾT THÚC";

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (days > 0) {
            // Ví dụ: 2 ngày 05:30:15
            return String.format(" %d ngày %02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            // Ví dụ: 05:30:15
            return String.format(" %02d:%02d:%02d", hours, minutes, seconds);
        } else {
            // Ví dụ: 05:15 (chỉ còn phút và giây)
            return String.format("️ %02d:%02d", minutes, seconds);
        }
    }

    // ===========================
    // ICON LOADER
    // ===========================
    private ImageIcon loadIcon(String name, String base64) {
        try {
            if(base64 != null && !base64.isEmpty() && !base64.equals("none")) {
                byte[] bytes = Base64.getDecoder().decode(base64);
                ImageIcon icon = new ImageIcon(bytes);
                Image img = icon.getImage().getScaledInstance(78,78, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}

        String lower = name.toLowerCase();
        if(lower.contains("laptop")) return local("src/main/java/frontend/icons/laptop.png");
        if(lower.contains("phone")) return local("src/main/java/frontend/icons/phone.png");
        return local("src/main/java/frontend/icons/watch.png");
    }

    private ImageIcon local(String path) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(78,78, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // ===========================
    // PAINT
    // ===========================
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(hovered) {
            g2.setColor(new Color(255,255,255,18));
            g2.fillRoundRect(0,0, getWidth(), getHeight(), 18,18);
        }
        if(selected) {
            g2.setColor(new Color(0,180,255));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1,1, getWidth()-3, getHeight()-3, 18,18);
        }
        super.paintComponent(g);
    }
}