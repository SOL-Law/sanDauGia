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

    public DesktopItemIcon(
            String name,
            String price,
            String leader,
            String base64Image,
            Consumer<String> onClick
    ) {

        this.itemName = name;

        // TO HƠN ĐỂ KHỚP KHUNG
        setPreferredSize(new Dimension(180,180));
        setMinimumSize(new Dimension(180,180));
        setMaximumSize(new Dimension(180,180));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JLabel icon =
                new JLabel(loadIcon(name, base64Image));

        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameLabel =
                new JLabel(name);

        nameLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 16)
        );

        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel priceLabel =
                new JLabel(price + " VNĐ");

        priceLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        priceLabel.setForeground(
                new Color(255,215,0)
        );

        priceLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel leaderLabel =
                new JLabel("👑 " + leader);

        leaderLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        leaderLabel.setForeground(Color.LIGHT_GRAY);
        leaderLabel.setAlignmentX(CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(18));
        add(icon);
        add(Box.createVerticalStrut(10));
        add(nameLabel);
        add(Box.createVerticalStrut(4));
        add(priceLabel);
        add(Box.createVerticalStrut(4));
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
    // ICON LOADER
    // ===========================
    private ImageIcon loadIcon(
            String name,
            String base64
    ) {

        try {

            if(base64 != null &&
                    !base64.isEmpty() &&
                    !base64.equals("none")) {

                byte[] bytes =
                        Base64.getDecoder()
                                .decode(base64);

                ImageIcon icon =
                        new ImageIcon(bytes);

                Image img =
                        icon.getImage()
                                .getScaledInstance(
                                        78,78,
                                        Image.SCALE_SMOOTH
                                );

                return new ImageIcon(img);
            }

        } catch (Exception ignored) {}

        String lower =
                name.toLowerCase();

        if(lower.contains("laptop"))
            return local("src/main/java/frontend/icons/laptop.png");

        if(lower.contains("phone"))
            return local("src/main/java/frontend/icons/phone.png");

        if(lower.contains("watch"))
            return local("src/main/java/frontend/icons/watch.png");

        return local("src/main/java/frontend/icons/watch.png");
    }

    private ImageIcon local(String path) {

        ImageIcon icon =
                new ImageIcon(path);

        Image img =
                icon.getImage()
                        .getScaledInstance(
                                78,78,
                                Image.SCALE_SMOOTH
                        );

        return new ImageIcon(img);
    }

    // ===========================
    // PAINT
    // ===========================
    protected void paintComponent(Graphics g) {

        Graphics2D g2 =
                (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        if(hovered) {

            g2.setColor(
                    new Color(255,255,255,18)
            );

            g2.fillRoundRect(
                    0,0,
                    getWidth(),
                    getHeight(),
                    18,18
            );
        }

        if(selected) {

            g2.setColor(
                    new Color(0,180,255)
            );

            g2.setStroke(
                    new BasicStroke(2f)
            );

            g2.drawRoundRect(
                    1,1,
                    getWidth()-3,
                    getHeight()-3,
                    18,18
            );
        }

        super.paintComponent(g);
    }
}