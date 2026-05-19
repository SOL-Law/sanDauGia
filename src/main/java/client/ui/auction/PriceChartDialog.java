package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class PriceChartDialog extends JDialog {
    private String itemName;
    private List<Integer> prices = new ArrayList<>();
    private List<String> times = new ArrayList<>();

    public PriceChartDialog(JFrame parent) {
        super(parent, "Biểu đồ giá Realtime", false); // false để không chặn thao tác đặt giá
        setSize(750, 480);
        setLocationRelativeTo(parent);
        setContentPane(new ChartPanel());
    }

    // Hàm nhận chuỗi data từ Server và bóc tách ra (GIỮ NGUYÊN PHƯƠNG THỨC GỐC)
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

    public String getCurrentItem() {
        return itemName;
    }

    // ===========================================
    // THUẬT TOÁN TỰ VẼ BIỂU ĐỒ BẰNG GRAPHICS 2D CAO CẤP
    // ===========================================
    class ChartPanel extends JPanel {
        public ChartPanel() {
            setBackground(new Color(20, 20, 24)); // Nền tối sâu chuẩn Cyberpunk
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            // 🔥 KÍCH HOẠT SIÊU KHỬ RĂNG CƯA ĐỒ HỌA VÀ FONT CHỮ
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int padding = 60;
            int width = getWidth();
            int height = getHeight();

            // 1. Vẽ 2 trục toạ độ mờ sang trọng hơn màu đen cũ
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawLine(padding, height - padding, padding, padding);
            g2.drawLine(padding, height - padding, width - padding, height - padding);

            if (prices.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                g2.drawString("Chưa có dữ liệu giao dịch...", width / 2 - 80, height / 2);
                g2.dispose();
                return;
            }

            // 2. Tính toán tỷ lệ dựa vào Giá Min và Giá Max
            int maxPrice = prices.stream().max(Integer::compare).get();
            int minPrice = prices.stream().min(Integer::compare).get();
            maxPrice = maxPrice + (maxPrice / 10 == 0 ? 100 : maxPrice / 10); // Đẩy nóc lên tí cho đẹp
            minPrice = Math.max(0, minPrice - (minPrice / 10));

            int yRange = Math.max(maxPrice - minPrice, 1);
            int numPoints = prices.size();
            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding;
            int xStep = chartWidth / Math.max(numPoints - 1, 1);

            // 3. Vẽ các đường lưới ngang kẻ caro mờ
            for (int i = 0; i <= 5; i++) {
                int yLabelValue = minPrice + (yRange * i / 5);
                int yPos = height - padding - (yLabelValue - minPrice) * chartHeight / yRange;

                g2.setColor(new Color(255, 255, 255, 12)); // Kẻ lưới mờ dạng chấm nhẹ tinh tế
                g2.drawLine(padding, yPos, width - padding, yPos);

                g2.setColor(new Color(170, 170, 180));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(String.format("%,d đ", yLabelValue), 5, yPos + 4); // Ghi giá tiền phân tách hàng nghìn
            }

            // Mảng lưu tọa độ vẽ
            int[] xPoints = new int[numPoints];
            int[] yPoints = new int[numPoints];
            for (int i = 0; i < numPoints; i++) {
                xPoints[i] = padding + i * xStep;
                yPoints[i] = height - padding - (prices.get(i) - minPrice) * chartHeight / yRange;
            }

            // 4. VẼ VÙNG ĐỔ BÓNG GRADIENT DƯỚI ĐƯỜNG GIÁ (Xịn như sàn Binance)
            if (numPoints > 1) {
                GeneralPath areaPath = new GeneralPath();
                areaPath.moveTo(xPoints[0], height - padding);
                for (int i = 0; i < numPoints; i++) {
                    areaPath.lineTo(xPoints[i], yPoints[i]);
                }
                areaPath.lineTo(xPoints[numPoints - 1], height - padding);
                areaPath.closePath();

                GradientPaint areaGradient = new GradientPaint(
                        0, padding, new Color(0, 230, 118, 60), // Xanh lục neon mờ ở đỉnh
                        0, height - padding, new Color(0, 0, 0, 0) // Tan biến dần ở đáy
                );
                g2.setPaint(areaGradient);
                g2.fill(areaPath);
            }

            // 5. VẼ ĐƯỜNG NỐI LINE CHART CHÍNH (XANH NEON PHÁT SÁNG)
            g2.setColor(new Color(0, 230, 118));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < numPoints - 1; i++) {
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }

            // 6. Vẽ các cục chấm tròn và hiển thị thông tin số liệu
            for (int i = 0; i < numPoints; i++) {
                int x = xPoints[i];
                int y = yPoints[i];

                // Chấm tròn lõi tối viền xanh phát sáng
                g2.setColor(new Color(20, 20, 24));
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(new Color(0, 230, 118));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x - 5, y - 5, 10, 10);

                // Hiển thị giá tiền tại đỉnh chấm
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(String.format("%,d", prices.get(i)), x - 15, y - 12);

                // Hiện thời gian ở trục hoành đáy
                g2.setColor(new Color(140, 140, 150));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(times.get(i), x - 18, height - padding + 22);
            }
            g2.dispose();
        }
    }
}