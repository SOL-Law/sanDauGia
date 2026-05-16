package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class AuctionPanel extends JPanel {
    private JPanel listContainer;
    private Consumer<String> onSelect;

    public AuctionPanel(Consumer<String> onSelect) {
        this.onSelect = onSelect;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE); // Nền trắng

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);

        // Gỡ bỏ hoàn toàn padding để thanh cuộn áp sát mép phải
        listContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder()); // Gỡ viền JScrollPane
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(20); // Lăn chuột mượt hơn

        add(scroll, BorderLayout.CENTER);
    }

    public void loadItems(String data) {
        SwingUtilities.invokeLater(() -> {
            JPanel newContainer = new JPanel();
            newContainer.setLayout(new BoxLayout(newContainer, BoxLayout.Y_AXIS));
            newContainer.setBackground(Color.WHITE);
            newContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            if (data != null && !data.trim().isEmpty()) {
                String[] arr = data.split(";");
                for (String item : arr) {
                    if (item.trim().isEmpty()) continue;
                    String[] p = item.split("\\|", -1);
                    if (p.length < 3) continue;

                    String base64 = (p.length > 3) ? p[3] : "";
                    String time = (p.length > 4) ? p[4] : "0";

                    newContainer.add(new DesktopItemIcon(p[0], p[1], p[2], base64, time, onSelect));
                }
            }

            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, listContainer);
            if (scroll != null) {
                scroll.setViewportView(newContainer);
            }
            listContainer = newContainer;
            revalidate();
            repaint();
        });
    }
}