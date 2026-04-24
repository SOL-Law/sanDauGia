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

        // =========================
        // BACKGROUND (GIỮ NGUYÊN BẢN CỦA BẠN)
        // =========================
        JPanel background = new JPanel() {

            Image bg = new ImageIcon(
                    "src/main/java/frontend/background2.jpg"
            ).getImage();

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                g.drawImage(
                        bg,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        this
                );

                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        background.setLayout(new BorderLayout());

        // =========================
        // DESKTOP PANEL (GIỮ FLOWLAYOUT NHƯ CŨ)
        // =========================
        desktop = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        40,
                        40
                )
        );

        desktop.setOpaque(false);

        // =========================
        // SCROLL (FIX NHẸ, KHÔNG PHÁ BACKGROUND)
        // =========================
        JScrollPane scroll = new JScrollPane(desktop);

        scroll.setBorder(null);

        // 🔥 CHỈ FIX NHẸ: KHÔNG làm mất background
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        background.add(scroll, BorderLayout.CENTER);

        add(background, BorderLayout.CENTER);
    }

    // =========================
    // LOAD ITEMS (FIX HIỂN THỊ)
    // =========================
    public void loadItems(String data) {

        SwingUtilities.invokeLater(() -> {

            if (data == null || data.trim().isEmpty()) {
                desktop.removeAll();
                desktop.revalidate();
                desktop.repaint();
                return;
            }

            desktop.removeAll();

            String[] arr = data.split(";");

            for (String item : arr) {

                if (item.isEmpty()) continue;

                String[] p = item.split("\\|");

                if (p.length < 3) continue;

                desktop.add(new DesktopItemIcon(
                        p[0],
                        p[1],
                        p[2],
                        onSelect
                ));
            }

            desktop.revalidate();
            desktop.repaint();
        });
    }
}