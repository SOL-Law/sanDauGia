package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.function.Consumer;

public class AuctionPanel extends JPanel {
    private final JPanel listContainer;
    private final Consumer<String> onSelect;
    private Image bgImage;

    public AuctionPanel(Consumer<String> onSelect) {
        this.onSelect = onSelect;
        setLayout(new BorderLayout());

        // 🔥 ÉP BUỘC TRONG SUỐT: Để hệ thống cho phép tự vẽ ảnh nền bằng paintComponent phía dưới
        setOpaque(false);

        // 🔥 NẠP ẢNH NỀN TRỰC TIẾP TẠI ĐÂY
        // Lưu ý: Đảm bảo tên file trùng khớp với ảnh trong thư mục icons của bạn (image_832bcc.jpg hoặc image_838d48.jpg)
        try {
            File imgFile = new File("src/main/java/frontend/icons/bg.jpg");
            if (imgFile.exists()) {
                bgImage = ImageIO.read(imgFile);
            } else {
                System.err.println("⚠️ Không tìm thấy file ảnh nền tại: " + imgFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi nạp ảnh nền: " + e.getMessage());
        }

        // Khởi tạo danh sách container và ép tàng hình tuyệt đối
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setBackground(new Color(0, 0, 0, 0));
        listContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(listContainer);

        // 🔥 TRIỆT TIÊU TẬN GỐC MÀU NỀN TRẮNG CỦA THANH CUỘN VÀ VIEWPORT (Đặc biệt hiệu quả dưới FlatLaf)
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBackground(new Color(0, 0, 0, 0));
        scroll.getViewport().setBackground(new Color(0, 0, 0, 0));

        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());

        // Tăng tốc độ cuộn chuột cho mượt mà
        scroll.getVerticalScrollBar().setUnitIncrement(25);

        add(scroll, BorderLayout.CENTER);
    }

    public void loadItems(String data) {
        // Thực hiện cập nhật giao diện an toàn trên luồng đồ họa Swing
        SwingUtilities.invokeLater(() -> {
            // 🔥 GIẢI PHÁP CHIẾN LƯỢC: Chỉ dọn sạch ruột (removeAll) của container cũ thay vì tráo đổi Viewport.
            // Điều này ngăn chặn FlatLaf tự động reset lại độ đục và màu trắng của JViewport.
            listContainer.removeAll();

            if (data != null && !data.trim().isEmpty()) {
                String[] arr = data.split(";");
                for (String item : arr) {
                    if (item.trim().isEmpty()) continue;
                    String[] p = item.split("\\|", -1);
                    if (p.length < 3) continue;

                    String base64 = (p.length > 3) ? p[3] : "";
                    String time = (p.length > 4) ? p[4] : "0";

                    // Thêm Card sản phẩm Dark Mode cao cấp vào danh sách
                    listContainer.add(new DesktopItemIcon(p[0], p[1], p[2], base64, time, onSelect));

                    // Tạo khoảng trống hở 12px giữa các card để nhìn xuyên ra ảnh nền nghệ thuật phía sau
                    listContainer.add(Box.createVerticalStrut(12));
                }
            }

            // Ép buộc cập nhật hình học cấu trúc và vẽ lại danh sách ngay lập tức
            listContainer.revalidate();
            listContainer.repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Kích hoạt thuật toán khử răng cưa và làm mịn hình ảnh chất lượng cao
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pW = getWidth();
        int pH = getHeight();

        if (bgImage != null) {
            // Thuật toán co giãn bao phủ Cover tự động căn giữa (tương tự background-size: cover trong CSS)
            int iW = bgImage.getWidth(this);
            int iH = bgImage.getHeight(this);
            double scale = Math.max((double) pW / iW, (double) pH / iH);
            int nW = (int) (iW * scale);
            int nH = (int) (iH * scale);

            int x = (pW - nW) / 2;
            int y = (pH - nH) / 2;

            g2.drawImage(bgImage, x, y, nW, nH, this);

            // Phủ lớp màu tối huyền bí (Alpha = 160) giúp làm chìm ảnh nền xuống, làm nổi bật các thẻ Card sản phẩm lên
            g2.setColor(new Color(15, 15, 20, 160));
            g2.fillRect(0, 0, pW, pH);
        } else {
            // LỚP BẢO HIỂM: Nếu sai đường dẫn hoặc lỗi ảnh, tự động nhuộm nền bằng màu Dark Mode sang trọng,
            // Đảm bảo tuyệt đối giao diện không bao giờ bị lộ màu trắng thô sần ra ngoài nữa.
            g2.setColor(new Color(24, 24, 28));
            g2.fillRect(0, 0, pW, pH);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}