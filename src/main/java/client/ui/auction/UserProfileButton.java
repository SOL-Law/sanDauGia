package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import com.google.gson.Gson;
import java.io.PrintWriter;

public class UserProfileButton extends JButton {

    private JPopupMenu popupMenu;
    private String username;
    private double balance = 0.0;
    private JMenuItem balanceItem;
    private PrintWriter out;
    private Gson gson;

    //  LƯU BIẾN CON CỦA MÀN HÌNH CHÍNH ĐỂ ĐIỀU HƯỚNG
    private frontend.AuctionUI mainUI;

    public UserProfileButton(frontend.AuctionUI mainUI, String username, PrintWriter out, Gson gson) {
        this.mainUI = mainUI;
        this.username = username;
        this.out = out;
        this.gson = gson;

        setPreferredSize(new Dimension(45, 45));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        initPopupMenu();

        final boolean[] isMenuOpen = {false};
        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) { isMenuOpen[0] = true; }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> isMenuOpen[0] = false);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (!isMenuOpen[0]) {
                    int xToaDo = getWidth() - 280;
                    int yToaDo = getHeight();
                    popupMenu.show(UserProfileButton.this, xToaDo, yToaDo);
                }
            }
        });
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu();

        // --- 1. Balance ---
        String formattedMoney = String.format("%,.0f", this.balance);
        balanceItem = new JMenuItem(" Số dư: " + formattedMoney + " VNĐ");
        balanceItem.setFont(new Font("Times New Roman", Font.BOLD, 16));
        balanceItem.setForeground(new Color(0, 150, 0));
        balanceItem.setEnabled(false);

        // --- 2. Nạp tiền (CHỈ ĐIỀU HƯỚNG TRANG) ---
        JMenuItem depositItem = new JMenuItem(" Nạp tiền");
        depositItem.setPreferredSize(new Dimension(280, 40));
        depositItem.setFont(new Font("Helvetica", Font.PLAIN, 16));
        depositItem.addActionListener(e -> mainUI.switchPage("Nạp tiền"));

        // --- 3. Thông tin cá nhân ---
        JMenuItem profileItem = new JMenuItem(" Thông tin cá nhân");
        profileItem.setPreferredSize(new Dimension(280, 40));
        profileItem.setFont(new Font("Helvetica", Font.PLAIN, 16));
        profileItem.addActionListener(e -> mainUI.switchPage("Thông tin cá nhân"));

        // --- 4. Lịch sử đấu giá ---
        JMenuItem historyItem = new JMenuItem(" Lịch sử đấu giá của tôi");
        historyItem.setPreferredSize(new Dimension(280, 40));
        historyItem.setFont(new Font("Helvetica", Font.PLAIN, 16));
        historyItem.addActionListener(e -> out.println(gson.toJson(new network.Request("GET_MY_HISTORY", this.username))));

        // --- 5. Donate (CHỈ ĐIỀU HƯỚNG TRANG) ---
        JMenuItem donateItem = new JMenuItem(" Donate cho Dev");
        donateItem.setPreferredSize(new Dimension(280, 40));
        donateItem.setFont(new Font("Helvetica", Font.PLAIN, 16));
        donateItem.setForeground(Color.MAGENTA);
        donateItem.addActionListener(e -> mainUI.switchPage("Donate"));

        // --- 6. Cài đặt ---
        JMenuItem settingsItem = new JMenuItem(" Cài đặt tài khoản");
        settingsItem.setPreferredSize(new Dimension(280, 40));
        settingsItem.setFont(new Font("Helvetica", Font.PLAIN, 16));
        settingsItem.addActionListener(e -> mainUI.switchPage("Cài đặt"));

        // --- 7. Đăng xuất ---
        JMenuItem logoutItem = new JMenuItem(" Đăng xuất");
        logoutItem.setPreferredSize(new Dimension(280, 40));
        logoutItem.setFont(new Font("Helvetica", Font.PLAIN, 16));
        logoutItem.setForeground(Color.RED);
        logoutItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) window.dispose();
                SwingUtilities.invokeLater(() -> new client.ui.auth.AuthFrame());
            }
        });

        popupMenu.add(balanceItem);
        popupMenu.add(depositItem);
        popupMenu.addSeparator();
        popupMenu.add(profileItem);
        popupMenu.add(historyItem);
        popupMenu.addSeparator();
        popupMenu.add(donateItem);
        popupMenu.add(settingsItem);
        popupMenu.addSeparator();
        popupMenu.add(logoutItem);
    }

    public void updateBalance(double newBalance) {
        this.balance = newBalance;
        String formattedMoney = String.format("%,.0f", this.balance);
        balanceItem.setText(" Số dư: " + formattedMoney + " VNĐ");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(40, 50, 70));
        g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
        g2.setColor(new Color(100, 150, 255));
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Ellipse2D.Double(1, 1, getWidth() - 2, getHeight() - 2));

        String initial = username.substring(0, 1).toUpperCase();
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(initial)) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(initial, x, y);
        g2.dispose();
    }

    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; repaint(); }
}