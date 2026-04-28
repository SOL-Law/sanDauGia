package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.Base64;

public class DesktopItemIcon extends JPanel {

    private boolean hovered = false;
    private boolean selected = false;
    private String itemName;

    // Hàm tạo ĐÃ ĐƯỢC THÊM BIẾN base64Image
    public DesktopItemIcon(
            String name,
            String price,
            String leader,
            String base64Image, // 🔥 MỚI THÊM Ở ĐÂY
            Consumer<String> onClick
    ) {

        this.itemName = name;

        setPreferredSize(new Dimension(130,160));
        setMinimumSize(new Dimension(130, 160));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // 🔥 GỌI HÀM GIẢI MÃ ẢNH TỪ MẠNG
        JLabel icon = new JLabel(loadIconFromNetwork(base64Image));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel(price + " VNĐ");
        priceLabel.setForeground(new Color(0,255,150));
        priceLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel leaderLabel = new JLabel("👑 " + leader);
        leaderLabel.setForeground(Color.LIGHT_GRAY);
        leaderLabel.setAlignmentX(CENTER_ALIGNMENT);

        add(icon);
        add(Box.createVerticalStrut(6));
        add(nameLabel);
        add(priceLabel);
        add(leaderLabel);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
            public void mouseClicked(MouseEvent e) {
                selected = true;
                onClick.accept(itemName);
                repaint();
            }
        });
    }

    // HÀM GIẢI MÃ BASE64 THÀNH ẢNH
    private ImageIcon loadIconFromNetwork(String base64Image) {
        try {
            if (base64Image == null || base64Image.isEmpty() || base64Image.equals("none")) {
                return new ImageIcon("src/main/java/frontend/icons/default.png");
            }

            // 1. Dịch ngược chuỗi chữ thành mảng Byte
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // 2. Tạo thẳng ImageIcon từ mảng Byte (Không cần file trong ổ cứng)
            ImageIcon icon = new ImageIcon(imageBytes);

            // 3. Resize cho đẹp
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            return new ImageIcon(img);

        } catch (Exception e) {
            System.out.println("Lỗi giải mã ảnh mạng!");
            return new ImageIcon("src/main/java/frontend/icons/default.png");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (hovered) {
            g2.setColor(new Color(0,150,255,80));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        }

        if (selected) {
            g2.setColor(new Color(0,200,255));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 20, 20);
        }
    }
}