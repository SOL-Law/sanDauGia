package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class AuctionPanel extends JPanel {

    private JPanel desktop;
    private Consumer<String> onSelect;

    public AuctionPanel(Consumer<String> onSelect) {
        this.onSelect = onSelect;
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel background = new JPanel() {
            Image bg = new ImageIcon("src/main/java/frontend/background2.jpg").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 0, 140));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        background.setLayout(new BorderLayout());
        desktop = createGridPanel();

        JScrollPane scroll = new JScrollPane(desktop);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        background.add(scroll, BorderLayout.CENTER);
        add(background, BorderLayout.CENTER);
    }

    private JPanel createGridPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        return panel;
    }

    public void loadItems(String data) {
        SwingUtilities.invokeLater(() -> {
            JPanel newPanel = createGridPanel();

            if (data != null && !data.trim().isEmpty()) {
                String[] arr = data.split(";");
                for (String item : arr) {
                    if (item.trim().isEmpty()) continue;
                    String[] p = item.split("\\|", -1);
                    if (p.length < 3) continue;

                    String base64 = (p.length > 3) ? p[3] : "";

                    // BẮT LẤY THỜI GIAN TỪ SERVER (tham số thứ 5)
                    String time = (p.length > 4) ? p[4] : "0";

                    // GẮN THỜI GIAN VÀO KHUNG TRANH
                    JPanel wrapper = createAnimatedCard(
                            new DesktopItemIcon(
                                    p[0],
                                    p[1],
                                    p[2],
                                    base64,
                                    time, // Dữ liệu thời gian
                                    onSelect
                            )
                    );
                    newPanel.add(wrapper);
                }
            }

            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, desktop);
            if (scroll != null) {
                scroll.setViewportView(newPanel);
            }
            desktop = newPanel;
            revalidate();
            repaint();
        });
    }

    private JPanel createAnimatedCard(JComponent child) {
        JPanel card = new JPanel(new BorderLayout()) {
            Image frame = new ImageIcon("src/main/java/frontend/icons/frame.png").getImage();
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(frame, 0, 0, getWidth(), getHeight(), this);
            }
        };

        card.setOpaque(false);
        // Nới rộng khung xíu để đủ chỗ chứa đồng hồ
        card.setPreferredSize(new Dimension(220, 265));
        card.setMinimumSize(new Dimension(220, 265));
        card.setMaximumSize(new Dimension(220, 265));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        card.add(child, BorderLayout.CENTER);

        return card;
    }
}