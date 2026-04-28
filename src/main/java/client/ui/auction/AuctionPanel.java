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

        // DESKTOP PANEL
        // =========================
        desktop = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 40));
        desktop.setOpaque(false);

        // 🔥 TUYỆT CHIÊU MỚI TẠI ĐÂY: Tạo một cái bọc (Wrapper)
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        // Nhét desktop lên trên cùng (NORTH) để ép nó luôn giữ đúng kích thước
        wrapper.add(desktop, BorderLayout.NORTH);

        // Nhét cái bọc vào JScrollPane thay vì nhét desktop
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
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

            // 1. TẠO MỘT CÁI DESKTOP MỚI TINH (Vứt bỏ hoàn toàn cái cũ)
            JPanel newDesktop = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 40));
            newDesktop.setOpaque(false);

            // 2. NHỒI DỮ LIỆU VÀO CÁI MỚI
            if (data != null && !data.trim().isEmpty()) {
                String[] arr = data.split(";");
                for (String item : arr) {
                    if (item.trim().isEmpty()) continue;

                    String[] p = item.split("\\|", -1);
                    if (p.length < 3) continue;

                    String base64Img = (p.length > 3) ? p[3] : "";
                    newDesktop.add(new DesktopItemIcon(p[0], p[1], p[2], base64Img, onSelect));
                }
            }

            // 3. TÌM SCROLLPANE VÀ "THAY MÁU"
            JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, desktop);
            if (scroll != null) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setOpaque(false);
                wrapper.add(newDesktop, BorderLayout.NORTH);

                // 🔥 Lệnh thần thánh: Ép ScrollPane vứt bỏ giao diện cũ, dùng giao diện mới
                scroll.setViewportView(wrapper);
            }

            // 4. Cập nhật lại con trỏ cho lần sau
            desktop = newDesktop;
        });
    }
}