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
    private String myUsername;

    //  BIẾN TOÀN CỤC ĐỂ ĐIỀU HƯỚNG CHUYỂN TRANG
    private JPanel cardPanel;
    private JLabel breadcrumbLabel;
    private JButton categoryBtn;

    public AuctionUI(PrintWriter out, BufferedReader in, Gson gson, String role , String username) {
        this.out = out;
        this.in = in;
        this.gson = gson;
        this.userRole = role;
        this.myUsername = username;

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
    private void openAutoBidDialog() {
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm trong danh sách trước!");
            return;
        }

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField maxBidField = new JTextField();
        JTextField incrementField = new JTextField();

        inputPanel.add(new JLabel("Mức giá tối đa bạn chi trả (Max Bid):"));
        inputPanel.add(maxBidField);
        inputPanel.add(new JLabel("Bước giá tự động tăng (Increment):"));
        inputPanel.add(incrementField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "🤖 Cài đặt Đấu giá tự động cho: " + selectedItem, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int maxBid = Integer.parseInt(maxBidField.getText().trim());
                int increment = Integer.parseInt(incrementField.getText().trim());

                if (maxBid <= 0 || increment <= 0) {
                    JOptionPane.showMessageDialog(this, "Dữ liệu nhập vào phải lớn hơn 0!");
                    return;
                }

                // Gửi kèm thêm "username" lấy từ biến this.myUsername của AuctionUI để server xử lý
                String payload = String.format("{\"item\":\"%s\",\"maxBid\":%d,\"increment\":%d,\"username\":\"%s\"}",
                        selectedItem, maxBid, increment, this.myUsername);

                out.println(gson.toJson(new Request("REGISTER_AUTO_BID", payload)));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng chỉ nhập ký tự số nguyên hợp lệ!");
            }
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
                        // CẬP NHẬT SỐ DƯ LIÊN TỤC
                        case "UPDATE_AUCTION":
                            SwingUtilities.invokeLater(() -> {
                                updateAuctionInfo(res.getPayload());
                                if (chartDialog != null && chartDialog.isVisible() && chartDialog.getCurrentItem() != null) {
                                    out.println(gson.toJson(new Request("GET_CHART", chartDialog.getCurrentItem())));
                                }

                                // THÊM DÒNG NÀY CỰC QUAN TRỌNG: Cứ sàn có biến là tự động gọi Database xin lại số dư
                                out.println(gson.toJson(new Request("GET_BALANCE", avatarButton.getUsername())));
                            });
                            break;

                        //  THÊM MỚI CASE NÀY ĐỂ BẮT THÔNG BÁO TỪ SERVER (Báo lỗi thiếu tiền / Báo thắng giải)
                        case "NOTIFY":
                            SwingUtilities.invokeLater(() -> {
                                //  Bỏ cái hộp thoại chặn ngang màn hình này đi
                                // JOptionPane.showMessageDialog(AuctionUI.this, res.getPayload());

                                //  Thay bằng cái bảng mờ mờ tự trượt ra vào:
                                client.ui.auction.NotificationToast.show(AuctionUI.this, res.getPayload());
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
        //  TẠO TRANG NẠP TIỀN
        JPanel depositCard = new JPanel(new GridBagLayout());
        setupDepositPage(depositCard);
        cardPanel.add(depositCard, "Nạp tiền");

        //  TẠO TRANG DONATE
        JPanel donateCard = new JPanel(new GridBagLayout());
        setupDonatePage(donateCard);
        cardPanel.add(donateCard, "Donate");

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
        avatarButton = new UserProfileButton(this, this.myUsername, out, gson);

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

            // 1. Chỉ hỏi mỗi cái Tên mới
            String newName = JOptionPane.showInputDialog(this, "Nhập tên mới cho sản phẩm:", selectedItem);

            // Nếu người dùng ấn Cancel, bỏ trống, hoặc nhập y hệt tên cũ thì thoát luôn
            if (newName == null || newName.trim().isEmpty() || newName.equals(selectedItem)) return;

            // 2. Gói Tên cũ và Tên mới gửi lên Server (KHÔNG có giá tiền nữa)
            String p = String.format("{\"oldName\":\"%s\",\"newName\":\"%s\"}", selectedItem, newName);
            out.println(gson.toJson(new Request("EDIT_ITEM", p)));

            selectedItem = null; editBtn.setEnabled(false); deleteBtn.setEnabled(false);
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

        // ĐOẠN CODE MỚI THAY THẾ (Đã thêm nút Auto-Bid chuẩn giao diện):
        JButton autoBidBtn = new JButton("🤖 Auto-Bid");
        autoBidBtn.setBackground(new Color(255, 140, 0)); // Màu cam nổi bật
        autoBidBtn.setForeground(Color.WHITE);
        autoBidBtn.setFont(new Font("Helvetica", Font.BOLD, 13));
        autoBidBtn.setPreferredSize(new Dimension(110, 35));
        autoBidBtn.addActionListener(e -> openAutoBidDialog());

        if ("BIDDER".equals(userRole)) {
            bottomPanel.add(label);
            bottomPanel.add(bidField);
            bottomPanel.add(bidButton);
            bottomPanel.add(autoBidBtn); // Thêm nút Auto-Bid cho người mua
        } else if ("SELLER".equals(userRole)) {
            bottomPanel.add(editBtn); bottomPanel.add(deleteBtn); bottomPanel.add(uploadBtn);
        } else if ("ADMIN".equals(userRole)) {
            bottomPanel.add(label);
            bottomPanel.add(bidField);
            bottomPanel.add(bidButton);
            bottomPanel.add(autoBidBtn); // Thêm nút Auto-Bid cho Admin
            bottomPanel.add(editBtn); bottomPanel.add(deleteBtn); bottomPanel.add(uploadBtn);
        }

        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(chartBtn);

        background.add(topPanel, BorderLayout.NORTH);
        background.add(centerPanel, BorderLayout.CENTER);
        background.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(background);
    }

    //  TRANG THÔNG TIN CÁ NHÂN (ĐÃ FIX GIAO DIỆN DARK MODE)
    private void setupProfilePage(JPanel panel) {
        // Đổi nền thành màu xám đen đồng bộ với trang Cài đặt
        panel.setBackground(new Color(25, 25, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("HỒ SƠ CÁ NHÂN", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 22));
        title.setForeground(Color.WHITE); // Chữ trắng sáng
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);

        gbc.gridwidth = 1;
        JLabel nameLbl = new JLabel("Tên tài khoản: ");
        nameLbl.setFont(new Font("Helvetica", Font.BOLD, 16));
        nameLbl.setForeground(Color.LIGHT_GRAY); // Màu xám nhạt
        gbc.gridy = 1; gbc.gridx = 0; panel.add(nameLbl, gbc);

        JLabel nameVal = new JLabel("Đang tải...");
        nameVal.setFont(new Font("Helvetica", Font.PLAIN, 16));
        nameVal.setForeground(new Color(90, 140, 255)); // Tên User màu xanh công nghệ nổi bật
        gbc.gridx = 1; panel.add(nameVal, gbc);

        JLabel roleLbl = new JLabel("Quyền hạn: ");
        roleLbl.setFont(new Font("Helvetica", Font.BOLD, 16));
        roleLbl.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 2; gbc.gridx = 0; panel.add(roleLbl, gbc);

        JLabel roleVal = new JLabel(userRole);
        roleVal.setFont(new Font("Helvetica", Font.PLAIN, 16));
        roleVal.setForeground(Color.WHITE); // Quyền hạn màu trắng
        gbc.gridx = 1; panel.add(roleVal, gbc);

        Timer t = new Timer(1000, e -> nameVal.setText(avatarButton.getUsername()));
        t.start();
    }

    //  TRANG CÀI ĐẶT (ĐÃ FIX GIAO DIỆN DARK MODE)
    private void setupSettingsPage(JPanel panel) {
        // Đổi nền thành màu Dark Mode xám đen
        panel.setBackground(new Color(25, 25, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 22));
        title.setForeground(Color.WHITE); // Chữ trắng
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);

        gbc.gridwidth = 1;
        JLabel idLbl = new JLabel("ID Tài khoản: "); idLbl.setFont(new Font("Helvetica", Font.BOLD, 15));
        idLbl.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 1; gbc.gridx = 0; panel.add(idLbl, gbc);

        idVal = new JLabel("Đang tải..."); idVal.setFont(new Font("Helvetica", Font.PLAIN, 15));
        idVal.setForeground(new Color(90, 140, 255)); // Đổi màu ID thành xanh cho nổi bật
        gbc.gridx = 1; panel.add(idVal, gbc);

        SwingUtilities.invokeLater(() -> out.println(gson.toJson(new Request("GET_PROFILE", avatarButton.getUsername()))));

        JLabel nameLbl = new JLabel("Đổi tên hiển thị: "); nameLbl.setFont(new Font("Helvetica", Font.BOLD, 15));
        nameLbl.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 2; gbc.gridx = 0; panel.add(nameLbl, gbc);

        JTextField nameField = new JTextField(15);
        nameField.setBackground(new Color(40, 40, 45)); // Ô nhập màu xám đậm
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        gbc.gridx = 1; panel.add(nameField, gbc);

        JLabel passLbl = new JLabel("Đổi mật khẩu mới: "); passLbl.setFont(new Font("Helvetica", Font.BOLD, 15));
        passLbl.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 3; gbc.gridx = 0; panel.add(passLbl, gbc);

        JPasswordField passField = new JPasswordField(15);
        passField.setBackground(new Color(40, 40, 45));
        passField.setForeground(Color.WHITE);
        passField.setCaretColor(Color.WHITE);
        gbc.gridx = 1; panel.add(passField, gbc);

        JButton saveBtn = new JButton("Áp dụng thay đổi");
        saveBtn.setBackground(new Color(0, 100, 210));
        saveBtn.setForeground(Color.WHITE);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(saveBtn, gbc);

        // ... (Phần trên của setupSettingsPage giữ nguyên)

        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newPass = new String(passField.getPassword()).trim();
            String currentName = avatarButton.getUsername();
            String finalName = newName.isEmpty() ? currentName : newName;

            String payload = String.format("{\"oldUser\":\"%s\",\"newUser\":\"%s\",\"newPass\":\"%s\"}", currentName, finalName, newPass);
            out.println(gson.toJson(new Request("UPDATE_PROFILE", payload)));

            if (!newName.isEmpty()) avatarButton.setUsername(newName);
            JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu Hệ thống thành công!");
            switchPage("Tất cả");
        });
    }

    // =====================================
    // TRANG NẠP TIỀN
    // =====================================
    private void setupDepositPage(JPanel panel) {
        panel.setBackground(new Color(25, 25, 28)); // Chuẩn Dark Mode
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("CỔNG THANH TOÁN TỰ ĐỘNG", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);

        JLabel info = new JLabel("<html><center>Vui lòng quét mã QR dưới đây và ghi lời nhắn:<br><b style='color:#5a8cff; font-size:16px;'>NAP " + avatarButton.getUsername() + "</b></center></html>", SwingConstants.CENTER);
        info.setFont(new Font("Helvetica", Font.PLAIN, 15));
        info.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 1; panel.add(info, gbc);

        // QR Code to, rõ, sắc nét 300x300
        JLabel qrLabel = new JLabel("Lỗi không tải được ảnh QR", SwingConstants.CENTER);
        qrLabel.setForeground(Color.RED);
        try {
            ImageIcon qrIcon = new ImageIcon(new ImageIcon("src/main/java/frontend/icons/qr-donate.jpg").getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH));
            qrLabel.setIcon(qrIcon);
            qrLabel.setText("");
        } catch (Exception e) {}
        gbc.gridy = 2; panel.add(qrLabel, gbc);

        gbc.gridwidth = 1;
        JLabel amountLbl = new JLabel("Số tiền đã nạp (VNĐ):");
        amountLbl.setFont(new Font("Helvetica", Font.BOLD, 15));
        amountLbl.setForeground(Color.WHITE);
        gbc.gridy = 3; gbc.gridx = 0; panel.add(amountLbl, gbc);

        JTextField amountField = new JTextField(15);
        amountField.setBackground(new Color(40, 40, 45));
        amountField.setForeground(Color.WHITE);
        amountField.setCaretColor(Color.WHITE);
        amountField.setFont(new Font("Helvetica", Font.BOLD, 16));
        gbc.gridx = 1; panel.add(amountField, gbc);

        JButton btnDone = new JButton("XÁC NHẬN ĐÃ CHUYỂN KHOẢN");
        btnDone.setBackground(new Color(0, 150, 80));
        btnDone.setForeground(Color.WHITE);
        btnDone.setFont(new Font("Helvetica", Font.BOLD, 14));
        btnDone.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(btnDone, gbc);

        btnDone.addActionListener(event -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new Exception();

                btnDone.setText("ĐANG CHỜ NGÂN HÀNG XÁC NHẬN...");
                btnDone.setBackground(Color.GRAY);
                btnDone.setEnabled(false);

                // Giả lập call API ngân hàng mất 3 giây
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        String payload = String.format("{\"username\":\"%s\",\"amount\":%f}", avatarButton.getUsername(), amount);
                        out.println(gson.toJson(new network.Request("DEPOSIT", payload)));

                        SwingUtilities.invokeLater(() -> {
                            client.ui.auction.NotificationToast.show(this, "✅ Nạp thành công " + amount + " VNĐ!");
                            amountField.setText("");
                            btnDone.setText("XÁC NHẬN ĐÃ CHUYỂN KHOẢN");
                            btnDone.setBackground(new Color(0, 150, 80));
                            btnDone.setEnabled(true);
                            switchPage("Tất cả"); // Nạp xong đá về trang chủ
                        });
                    } catch (Exception ex) {}
                }).start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ! Vui lòng nhập số.");
            }
        });
    }

    // =====================================
    // TRANG DONATE CHO DEV
    // =====================================
    private void setupDonatePage(JPanel panel) {
        panel.setBackground(new Color(25, 25, 28)); // Chuẩn Dark Mode
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("☕ CẢM ƠN BẠN ĐÃ ỦNG HỘ DEV ☕", SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 26));
        title.setForeground(new Color(255, 100, 200)); // Màu hường cho nó cảm xúc
        gbc.gridx = 0; gbc.gridy = 0; panel.add(title, gbc);

        JLabel info = new JLabel("<html><center>Mọi sự đóng góp của bạn đều là động lực to lớn<br>giúp hệ thống Auction ngày càng hoàn thiện hơn!</center></html>", SwingConstants.CENTER);
        info.setFont(new Font("Helvetica", Font.PLAIN, 15));
        info.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 1; panel.add(info, gbc);

        // QR Code SIÊU TO KHỔNG LỒ 400x400
        JLabel qrLabel = new JLabel("Lỗi không tải được ảnh QR", SwingConstants.CENTER);
        qrLabel.setForeground(Color.RED);
        try {
            ImageIcon qrIcon = new ImageIcon(new ImageIcon("src/main/java/frontend/icons/qr-donate.jpg").getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH));
            qrLabel.setIcon(qrIcon);
            qrLabel.setText("");
        } catch (Exception e) {}
        gbc.gridy = 2; panel.add(qrLabel, gbc);

        JButton backBtn = new JButton("Quay lại trang chủ");
        backBtn.setBackground(new Color(80, 80, 90));
        backBtn.setForeground(Color.WHITE);
        backBtn.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = 3; panel.add(backBtn, gbc);

        backBtn.addActionListener(e -> switchPage("Tất cả"));
    } // Đóng hàm setupDonatePage

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