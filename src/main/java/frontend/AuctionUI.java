package frontend;

import com.google.gson.Gson;
import network.Request;
import client.ui.auction.AuctionPanel;
import client.ui.auction.UploadDialog;
import client.ui.history.HistoryDialog;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

import client.ui.auction.UserProfileButton;

public class AuctionUI extends JFrame {

    private JTextField bidField;
    private JButton bidButton;
    private JTextField searchField;
    private AuctionPanel homePanel, artPanel, elecPanel, vehiclePanel, otherPanel;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;
    private String userRole;
    private String selectedItem = null;
    private UserProfileButton avatarButton;
    private client.ui.auction.PriceChartDialog chartDialog;
    private java.util.Map<String, String> itemOwners = new java.util.HashMap<>();
    private JLabel idVal;

    //  BIẾN TOÀN CỤC ĐỂ ĐIỀU HƯỚNG CHUYỂN TRANG
    private JPanel cardPanel;
    private JLabel breadcrumbLabel;
    private JButton categoryBtn;

    public AuctionUI(PrintWriter out, BufferedReader in, Gson gson, String role) {
        this.out = out;
        this.in = in;
        this.gson = gson;
        this.userRole = role;

        setTitle("PHÒNG ĐẤU GIÁ REALTIME - Quyền: " + role);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
        startListeningServer();
        this.setVisible(true);

        SwingUtilities.invokeLater(this::requestInitialData);
    }

    private void requestInitialData() {
        out.println(gson.toJson(new Request("GET_AUCTION", "")));
        out.println(gson.toJson(new Request("GET_BALANCE", userRole)));
    }

    //  HÀM PUBLIC ĐỂ TRANG KHÁC GỌI LỆNH ĐIỀU HƯỚNG CHUYỂN TRANG
    public void switchPage(String pageName) {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, pageName);
        breadcrumbLabel.setText("<html><font color='#0064d2'><b>Trang chủ</b></font> &gt; " + pageName + "</html>");
        if (!"Tất cả".equals(pageName) && !"Nghệ thuật".equals(pageName) && !"Điện tử".equals(pageName) && !"Xe cộ".equals(pageName) && !"Khác".equals(pageName)) {
            categoryBtn.setText("Danh mục khác ▾");
        }
    }

    private void startListeningServer() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    Request res = gson.fromJson(msg, Request.class);
                    switch (res.getType()) {
                        case "UPDATE_BALANCE":
                            SwingUtilities.invokeLater(() -> {
                                double balance = Double.parseDouble(res.getPayload());
                                avatarButton.updateBalance(balance);
                            });
                            break;
                        case "PROFILE_DATA":
                            SwingUtilities.invokeLater(() -> {
                                if (idVal != null) idVal.setText(res.getPayload());
                            });
                            break;
                        case "START_SESSION":
                            startNewSession("Phiên đấu giá bắt đầu!");
                            break;
                        case "END_SESSION":
                            auctionEnded("Phiên đấu giá kết thúc!");
                            break;
                        case "HISTORY_DATA":
                            SwingUtilities.invokeLater(() -> {
                                setCursor(Cursor.getDefaultCursor());
                                new HistoryDialog(AuctionUI.this, res.getPayload()).setVisible(true);
                            });
                            break;
                        case "CHART_DATA":
                            SwingUtilities.invokeLater(() -> {
                                String[] parts = res.getPayload().split("\\|", 2);
                                if (parts.length == 2) {
                                    if (chartDialog == null) {
                                        chartDialog = new client.ui.auction.PriceChartDialog(AuctionUI.this);
                                    }
                                    chartDialog.updateData(parts[0], parts[1]);
                                    if (!chartDialog.isVisible()) chartDialog.setVisible(true);
                                }
                            });
                            break;
                        case "UPDATE_AUCTION":
                            SwingUtilities.invokeLater(() -> {
                                updateAuctionInfo(res.getPayload());
                                if (chartDialog != null && chartDialog.isVisible() && chartDialog.getCurrentItem() != null) {
                                    out.println(gson.toJson(new Request("GET_CHART", chartDialog.getCurrentItem())));
                                }
                            });
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Server disconnected");
            }
        }).start();
    }

    private void initUI() {
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(Color.WHITE);

        // ========================= 1. KHUNG CHỨA TRANG (CARD LAYOUT) =========================
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(Color.WHITE);

        bidButton = new JButton("Đặt giá");
        JButton editBtn = new JButton(" Sửa tên sản phẩm");
        JButton deleteBtn = new JButton("Xóa sản phẩm");
        JButton uploadBtn = new JButton("Đăng sản phẩm");
        JButton chartBtn = new JButton(" Xem Biểu Đồ");

        java.util.function.Consumer<String> onSelectAction = item -> {
            selectedItem = item;
            if ("BIDDER".equals(userRole) || "ADMIN".equals(userRole)) {
                bidButton.setEnabled(true);
            }
            String itemOwner = itemOwners.get(selectedItem);
            String myUsername = avatarButton.getUsername();
            boolean isMyItem = myUsername.equals(itemOwner);

            if ("ADMIN".equals(userRole)) {
                editBtn.setEnabled(true);
                deleteBtn.setEnabled(true);
            } else if ("SELLER".equals(userRole) && isMyItem) {
                editBtn.setEnabled(true);
                deleteBtn.setEnabled(true);
            } else {
                editBtn.setEnabled(false);
                deleteBtn.setEnabled(false);
            }
        };

        homePanel = new AuctionPanel(onSelectAction);
        artPanel = new AuctionPanel(onSelectAction);
        elecPanel = new AuctionPanel(onSelectAction);
        vehiclePanel = new AuctionPanel(onSelectAction);
        otherPanel = new AuctionPanel(onSelectAction);

        cardPanel.add(homePanel, "Tất cả");
        cardPanel.add(artPanel, "Nghệ thuật");
        cardPanel.add(elecPanel, "Điện tử");
        cardPanel.add(vehiclePanel, "Xe cộ");
        cardPanel.add(otherPanel, "Khác");

        //  TẠO TRANG THÔNG TIN CÁ NHÂN (PROFILE CARD)
        JPanel profileCard = new JPanel(new GridBagLayout());
        profileCard.setBackground(Color.WHITE);
        setupProfilePage(profileCard);
        cardPanel.add(profileCard, "Thông tin cá nhân");

        //  TẠO TRANG CÀI ĐẶT (SETTINGS CARD)
        JPanel settingsCard = new JPanel(new GridBagLayout());
        settingsCard.setBackground(Color.WHITE);
        setupSettingsPage(settingsCard);
        cardPanel.add(settingsCard, "Cài đặt");

        // ========================= 2. BREADCRUMB =========================
        breadcrumbLabel = new JLabel("<html><font color='#0064d2'><b>Trang chủ</b></font> &gt; Tất cả</html>");
        breadcrumbLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
        breadcrumbLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        breadcrumbLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ========================= 3. THANH TÌM KIẾM + DANH MỤC TOP =========================
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        searchContainer.setBackground(Color.WHITE);

        categoryBtn = new JButton("Tất cả danh mục ▾");
        categoryBtn.setFont(new Font("Helvetica", Font.PLAIN, 14));
        categoryBtn.setPreferredSize(new Dimension(160, 38));
        categoryBtn.setBackground(new Color(245, 245, 245));
        categoryBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        breadcrumbLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchPage("Tất cả");
                categoryBtn.setText("Tất cả danh mục ▾");
            }
        });

        JPopupMenu categoryMenu = new JPopupMenu();
        String[] cats = {"Tất cả", "Nghệ thuật", "Điện tử", "Xe cộ", "Khác"};
        for (int i = 0; i < cats.length; i++) {
            final int index = i;
            JMenuItem item = new JMenuItem("  " + cats[i]);
            item.setFont(new Font("Helvetica", Font.PLAIN, 14));
            item.setPreferredSize(new Dimension(160, 30));
            item.addActionListener(e -> {
                categoryBtn.setText(cats[index] + " ▾");
                switchPage(cats[index]);
            });
            categoryMenu.add(item);
        }

        final long[] lastClosedTime = {0};
        categoryMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { lastClosedTime[0] = System.currentTimeMillis(); }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        categoryBtn.addActionListener(e -> {
            if (System.currentTimeMillis() - lastClosedTime[0] > 150) {
                categoryMenu.show(categoryBtn, 0, categoryBtn.getHeight());
            }
        });

        searchField = new JTextField(35) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Color.GRAY);
                    g2.setFont(getFont().deriveFont(Font.PLAIN));
                    int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
                    g2.drawString("Tìm kiếm", 10, y);
                    g2.dispose();
                }
            }
        };
        searchField.setFont(new Font("Helvetica", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(450, 38));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                out.println(gson.toJson(new Request("GET_AUCTION", "")));
            }
        });

        searchContainer.add(categoryBtn);
        searchContainer.add(searchField);

        //  TRUYỀN CHÍNH THỨC ĐỊA CHỈ THẰNG AUCTIONUI NÀY CHO AVATAR BUTTON ĐỂ ĐIỀU HƯỚNG
        avatarButton = new UserProfileButton(this, "admin", out, gson);
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightTop.setBackground(Color.WHITE);
        rightTop.add(avatarButton);

        topPanel.add(searchContainer, BorderLayout.CENTER);
        topPanel.add(rightTop, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(breadcrumbLabel, BorderLayout.NORTH);
        centerPanel.add(cardPanel, BorderLayout.CENTER);

        // ========================= 4. BOTTOM PANEL =========================
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JLabel label = new JLabel("Nhập giá:");
        label.setFont(new Font("Helvetica", Font.BOLD, 14));
        bidField = new JTextField(10);
        bidField.setPreferredSize(new Dimension(100, 35));

        bidButton.setEnabled(false);
        bidButton.setPreferredSize(new Dimension(90, 35));
        bidButton.setBackground(new Color(0, 100, 210));
        bidButton.setForeground(Color.WHITE);
        bidButton.addActionListener(e -> placeBid());

        editBtn.setEnabled(false);
        editBtn.setPreferredSize(new Dimension(140, 35));
        editBtn.addActionListener(e -> {
            if (selectedItem == null) return;
            String newName = JOptionPane.showInputDialog(this, "Nhập tên mới cho sản phẩm:", selectedItem);
            if (newName == null || newName.trim().isEmpty()) return;
            String priceStr = JOptionPane.showInputDialog(this, "Nhập giá khởi điểm mới:");
            if (priceStr == null || priceStr.trim().isEmpty()) return;
            try {
                int newPrice = Integer.parseInt(priceStr);
                String p = String.format("{\"oldName\":\"%s\",\"newName\":\"%s\",\"price\":%d}", selectedItem, newName, newPrice);
                out.println(gson.toJson(new Request("EDIT_ITEM", p)));
                selectedItem = null; editBtn.setEnabled(false); deleteBtn.setEnabled(false);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Giá tiền không hợp lệ!"); }
        });

        deleteBtn.setEnabled(false);
        deleteBtn.setPreferredSize(new Dimension(140, 35));
        deleteBtn.addActionListener(e -> {
            if (selectedItem == null) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sản phẩm [" + selectedItem + "] không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                out.println(gson.toJson(new Request("DELETE_ITEM", selectedItem)));
                selectedItem = null; editBtn.setEnabled(false); deleteBtn.setEnabled(false);
            }
        });

        chartBtn.setPreferredSize(new Dimension(130, 35));
        chartBtn.addActionListener(e -> {
            if (selectedItem == null) { JOptionPane.showMessageDialog(this, "Hãy chọn 1 sản phẩm trước!"); return; }
            out.println(gson.toJson(new Request("GET_CHART", selectedItem)));
        });

        uploadBtn.setPreferredSize(new Dimension(140, 35));
        uploadBtn.addActionListener(e -> new UploadDialog(this, out, gson, avatarButton.getUsername()).setVisible(true));

        if ("BIDDER".equals(userRole)) {
            bottomPanel.add(label); bottomPanel.add(bidField); bottomPanel.add(bidButton);
        } else if ("SELLER".equals(userRole)) {
            bottomPanel.add(editBtn); bottomPanel.add(deleteBtn); bottomPanel.add(uploadBtn);
        } else if ("ADMIN".equals(userRole)) {
            bottomPanel.add(label); bottomPanel.add(bidField); bottomPanel.add(bidButton);
            bottomPanel.add(editBtn); bottomPanel.add(deleteBtn); bottomPanel.add(uploadBtn);
        }

        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(chartBtn);

        background.add(topPanel, BorderLayout.NORTH);
        background.add(centerPanel, BorderLayout.CENTER);
        background.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(background);
    }

    //  TRANG THÔNG TIN
    private void setupProfilePage(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("HỒ SƠ CÁ NHÂN", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);

        gbc.gridwidth = 1;
        JLabel nameLbl = new JLabel("Tên tài khoản: "); nameLbl.setFont(new Font("Helvetica", Font.BOLD, 16));
        gbc.gridy = 1; gbc.gridx = 0; panel.add(nameLbl, gbc);
        JLabel nameVal = new JLabel("Đang tải..."); nameVal.setFont(new Font("Helvetica", Font.PLAIN, 16));
        gbc.gridx = 1; panel.add(nameVal, gbc);

        JLabel roleLbl = new JLabel("Quyền hạn: "); roleLbl.setFont(new Font("Helvetica", Font.BOLD, 16));
        gbc.gridy = 2; gbc.gridx = 0; panel.add(roleLbl, gbc);
        JLabel roleVal = new JLabel(userRole); roleVal.setFont(new Font("Helvetica", Font.PLAIN, 16));
        gbc.gridx = 1; panel.add(roleVal, gbc);

        Timer t = new Timer(1000, e -> nameVal.setText(avatarButton.getUsername())); t.start();
    }

    //  TRANG CÀI ĐẶT (ĐÃ KẾT NỐI DATABASE CHUẨN)
    private void setupSettingsPage(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);

        gbc.gridwidth = 1;
        JLabel idLbl = new JLabel("ID Tài khoản: "); idLbl.setFont(new Font("Helvetica", Font.BOLD, 15));
        gbc.gridy = 1; gbc.gridx = 0; panel.add(idLbl, gbc);

        // Gắn biến idVal ra ngoài để hàm Listener cập nhật được
        idVal = new JLabel("Đang tải..."); idVal.setFont(new Font("Helvetica", Font.PLAIN, 15));
        gbc.gridx = 1; panel.add(idVal, gbc);

        // Khi trang cài đặt load xong, tự động xin Server cái ID
        SwingUtilities.invokeLater(() -> out.println(gson.toJson(new Request("GET_PROFILE", avatarButton.getUsername()))));

        JLabel nameLbl = new JLabel("Đổi tên hiển thị: "); nameLbl.setFont(new Font("Helvetica", Font.BOLD, 15));
        gbc.gridy = 2; gbc.gridx = 0; panel.add(nameLbl, gbc);
        JTextField nameField = new JTextField(15);
        gbc.gridx = 1; panel.add(nameField, gbc);

        JLabel passLbl = new JLabel("Đổi mật khẩu mới: "); passLbl.setFont(new Font("Helvetica", Font.BOLD, 15));
        gbc.gridy = 3; gbc.gridx = 0; panel.add(passLbl, gbc);
        JPasswordField passField = new JPasswordField(15);
        gbc.gridx = 1; panel.add(passField, gbc);

        JButton saveBtn = new JButton("Áp dụng thay đổi");
        saveBtn.setBackground(new Color(0, 100, 210)); saveBtn.setForeground(Color.WHITE);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newPass = new String(passField.getPassword()).trim();
            String currentName = avatarButton.getUsername();
            String finalName = newName.isEmpty() ? currentName : newName;

            // Gói 3 thông tin lại ném xuống Server để chọc vào Database
            String payload = String.format("{\"oldUser\":\"%s\",\"newUser\":\"%s\",\"newPass\":\"%s\"}", currentName, finalName, newPass);
            out.println(gson.toJson(new Request("UPDATE_PROFILE", payload)));

            if (!newName.isEmpty()) {
                avatarButton.setUsername(newName); // Cập nhật tên trên UI cho mượt
            }

            JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu Hệ thống thành công!");
            switchPage("Tất cả"); // Xong việc thì đá về trang chủ
        });
    }

    private void placeBid() {
        if (selectedItem == null) { JOptionPane.showMessageDialog(this, "Chọn sản phẩm trước!"); return; }
        try {
            int price = Integer.parseInt(bidField.getText());
            String currentUser = avatarButton.getUsername();
            String payload = String.format("{\"item\":\"%s\",\"price\":%d, \"username\":\"%s\"}", selectedItem, price, currentUser);
            out.println(gson.toJson(new Request("PLACE_BID", payload)));
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Giá không hợp lệ!"); }
    }

    public void updateAuctionInfo(String data) {
        if (data == null || data.replace(" ", "").isEmpty()) {
            homePanel.loadItems(""); artPanel.loadItems(""); elecPanel.loadItems("");
            vehiclePanel.loadItems(""); otherPanel.loadItems("");
            return;
        }

        String keyword = searchField.getText().toLowerCase().trim();
        StringBuilder homeData = new StringBuilder(), artData = new StringBuilder(), elecData = new StringBuilder(), vehicleData = new StringBuilder(), otherData = new StringBuilder();

        for (String item : data.split(";")) {
            if (item.trim().isEmpty()) continue;
            String[] parts = item.split("\\|", -1);
            String name = parts[0];

            if (!keyword.isEmpty() && !name.toLowerCase().contains(keyword)) continue;

            if (parts.length > 5) itemOwners.put(name, parts[5]);
            else itemOwners.put(name, "unknown");

            String category = "Khác";
            if (parts.length > 6) category = parts[6];

            homeData.append(item).append(";");

            switch (category) {
                case "Nghệ thuật": artData.append(item).append(";"); break;
                case "Điện tử": elecData.append(item).append(";"); break;
                case "Xe cộ": vehicleData.append(item).append(";"); break;
                default: otherData.append(item).append(";"); break;
            }
        }

        homePanel.loadItems(homeData.toString());
        artPanel.loadItems(artData.toString());
        elecPanel.loadItems(elecData.toString());
        vehiclePanel.loadItems(vehicleData.toString());
        otherPanel.loadItems(otherData.toString());
    }

    public void startNewSession(String message) {
        bidButton.setEnabled(true); bidField.setText("");
        JOptionPane.showMessageDialog(this, message);
    }

    public void auctionEnded(String message) {
        SwingUtilities.invokeLater(() -> {
            bidButton.setEnabled(false); JOptionPane.showMessageDialog(this, message);
        });
    }
}