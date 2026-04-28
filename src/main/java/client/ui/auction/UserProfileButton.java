package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class UserProfileButton extends JButton {

    private JPopupMenu popupMenu;
    private String username;
    private double balance = 0.0; // Số dư mặc định (Sau này lấy từ Server)

    public UserProfileButton(String username) {
        this.username = (username != null && !username.isEmpty()) ? username : "User";

        // Cố định kích thước hình tròn (Ví dụ: 45x45 pixel)
        setPreferredSize(new Dimension(45, 45));

        // Tắt các hiệu ứng mặc định của nút vuông
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Khởi tạo Menu xổ xuống
        initPopupMenu();

        // Sự kiện click chuột vào Avatar
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Hiển thị Menu ngay dưới Avatar
                popupMenu.show(UserProfileButton.this, 0, getHeight() + 5);
            }
        });
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu();

        // --- 1. Balance (Số dư) ---
        JMenuItem balanceItem = new JMenuItem("💰 Số dư: " + balance + " VNĐ");
        balanceItem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        balanceItem.setForeground(new Color(0, 150, 0)); // Màu xanh lá
        balanceItem.setEnabled(false); // Không cho bấm, chỉ để hiển thị

        // --- 2. Nạp tiền ---
        JMenuItem napTienItem = new JMenuItem("💳 Nạp tiền");
        napTienItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mở form nạp tiền..."));

        // --- 3. Thông tin cá nhân ---
        JMenuItem profileItem = new JMenuItem("👤 Thông tin cá nhân");
        profileItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Hiển thị thông tin user..."));

        // --- 4. Lịch sử đấu giá ---
        JMenuItem historyItem = new JMenuItem("📜 Lịch sử đấu giá của tôi");
        historyItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mở bảng lịch sử..."));

        // --- 5. Donate ---
        JMenuItem donateItem = new JMenuItem("☕ Donate cho Dev");
        donateItem.setForeground(Color.MAGENTA);
        donateItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Cảm ơn bạn đã donate!"));

        // --- 6. Cài đặt ---
        JMenuItem settingsItem = new JMenuItem("⚙️ Cài đặt");
        settingsItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Mở phần cài đặt..."));

        // --- 7. Đăng xuất ---
        JMenuItem logoutItem = new JMenuItem("🚪 Đăng xuất");
        logoutItem.setForeground(Color.RED);
        logoutItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // Xử lý đóng cửa sổ và quay về màn hình Login
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) window.dispose();
                System.out.println("Đã đăng xuất!");
                // Gọi lại màn hình Login ở đây: new LoginFrame().setVisible(true);
            }
        });

        // Thêm vào Popup Menu (Dùng addSeparator để kẻ gạch ngang cho đẹp)
        popupMenu.add(balanceItem);
        popupMenu.add(napTienItem);
        popupMenu.addSeparator();

        popupMenu.add(profileItem);
        popupMenu.add(historyItem);
        popupMenu.addSeparator();

        popupMenu.add(donateItem);
        popupMenu.add(settingsItem);
        popupMenu.addSeparator();

        popupMenu.add(logoutItem);
    }

    // ==========================================
    // VẼ HÌNH TRÒN VÀ CHỮ CÁI ĐẦU TIÊN CỦA TÊN
    // ==========================================
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Khử răng cưa cho nét vẽ mịn màng
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ hình tròn làm nền (Màu xanh đậm)
        g2.setColor(new Color(40, 50, 70));
        g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));

        // Viền vòng ngoài cho đẹp
        g2.setColor(new Color(100, 150, 255));
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Ellipse2D.Double(1, 1, getWidth() - 2, getHeight() - 2));

        // Lấy chữ cái đầu tiên của Username (Viết hoa)
        String initial = username.substring(0, 1).toUpperCase();

        // Vẽ chữ ra giữa vòng tròn
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(initial)) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

        g2.drawString(initial, x, y);
        g2.dispose();
    }
}