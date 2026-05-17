package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PriceChartDialog extends JDialog {
    private String itemName;
    private List<Integer> prices = new ArrayList<>();
    private List<String> times = new ArrayList<>();

    public PriceChartDialog(JFrame parent) {
        super(parent, "Biểu đồ giá Realtime", false); // false để không chặn thao tác đặt giá
        setSize(700, 450);
        setLocationRelativeTo(parent);
        setContentPane(new ChartPanel());
    }

    // Hàm nhận chuỗi data từ Server và bóc tách ra
    public void updateData(String itemName, String data) {
        this.itemName = itemName;
        setTitle("📈 Biểu đồ giá Realtime: " + itemName);
        prices.clear();
        times.clear();

        if (data != null && !data.isEmpty()) {
            String[] points = data.split(",");
            for (String pt : points) {
                if (pt.trim().isEmpty()) continue;
                String[] parts = pt.split("-");
                if (parts.length == 2) {
                    times.add(parts[0]);
                    prices.add(Integer.parseInt(parts[1]));
                }
            }
        }
        repaint(); // Xóa vẽ lại
    }

    public String getCurrentItem() { return itemName; }

    // ===========================================
    // THUẬT TOÁN TỰ VẼ BIỂU ĐỒ BẰNG GRAPHICS 2D
    // ===========================================
    class ChartPanel extends JPanel {
        public ChartPanel() { setBackground(Color.WHITE); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 60;
            int width = getWidth();
            int height = getHeight();

            // 1. Vẽ 2 trục toạ độ (Trục tung, Trục hoành)
            g2.setColor(Color.BLACK);
            g2.drawLine(padding, height - padding, padding, padding);
            g2.drawLine(padding, height - padding, width - padding, height - padding);

            if (prices.isEmpty()) {
                g2.drawString("Chưa có dữ liệu", width / 2 - 40, height / 2);
                return;
            }

            // 2. Tính toán tỷ lệ dựa vào Giá Min và Giá Max
            int maxPrice = prices.stream().max(Integer::compare).get();
            int minPrice = prices.stream().min(Integer::compare).get();
            maxPrice = maxPrice + (maxPrice / 10); // Đẩy nóc lên tí cho đẹp
            minPrice = Math.max(0, minPrice - (minPrice / 10));

            int yRange = Math.max(maxPrice - minPrice, 1);
            int numPoints = prices.size();
            int xStep = (width - 2 * padding) / Math.max(numPoints - 1, 1);

            // 3. Vẽ các đường lưới ngang kẻ caro
            for (int i = 0; i <= 5; i++) {
                int yLabelValue = minPrice + (yRange * i / 5);
                int yPos = height - padding - (yLabelValue - minPrice) * (height - 2 * padding) / yRange;
                g2.setColor(new Color(230, 230, 230)); // Kẻ ngang mờ
                g2.drawLine(padding, yPos, width - padding, yPos);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(yLabelValue + "đ", 5, yPos + 5); // Ghi giá tiền
            }

            // 4. Vẽ đường nối line chart (MÀU ĐỎ)
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3f));
            for (int i = 0; i < numPoints - 1; i++) {
                int x1 = padding + i * xStep;
                int y1 = height - padding - (prices.get(i) - minPrice) * (height - 2 * padding) / yRange;
                int x2 = padding + (i + 1) * xStep;
                int y2 = height - padding - (prices.get(i + 1) - minPrice) * (height - 2 * padding) / yRange;
                g2.drawLine(x1, y1, x2, y2);
            }

            // 5. Vẽ các cục chấm tròn và điền thời gian
            for (int i = 0; i < numPoints; i++) {
                int x = padding + i * xStep;
                int y = height - padding - (prices.get(i) - minPrice) * (height - 2 * padding) / yRange;

                g2.setColor(Color.BLUE);
                g2.fillOval(x - 5, y - 5, 10, 10); // Chấm xanh

                g2.setColor(Color.BLACK);
                g2.drawString(prices.get(i) + "", x - 15, y - 10); // Hiện số giá

                g2.setColor(Color.GRAY);
                g2.drawString(times.get(i), x - 20, height - padding + 20); // Hiện thời gian ở đáy
            }
        }
    }
}