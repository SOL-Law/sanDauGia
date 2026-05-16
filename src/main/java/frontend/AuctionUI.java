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
    private AuctionPanel auctionPanel;
    private String selectedItem = null;
    private UserProfileButton avatarButton;

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

    private void startListeningServer() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    Request res = gson.fromJson(msg, Request.class);
                    switch (res.getType()) {
                        case "UPDATE_AUCTION":
                            SwingUtilities.invokeLater(() -> updateAuctionInfo(res.getPayload()));
                            break;
                        case "UPDATE_BALANCE":
                            SwingUtilities.invokeLater(() -> {
                                double balance = Double.parseDouble(res.getPayload());
                                avatarButton.updateBalance(balance);
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
                    }
                }
            } catch (Exception e) {
                System.out.println("Server disconnected");
            }
        }).start();
    }

    private void initUI() {
        //  Đổi nền thành Trắng tinh khôi (Không dùng ảnh nữa)
        JPanel background = new JPanel(new BorderLayout());
        background.setBackground(Color.WHITE);

        // ========================= TOP (Chỉ giữ Avatar) =========================
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        topPanel.setBackground(Color.WHITE);
        avatarButton = new UserProfileButton("admin", out, gson);
        topPanel.add(avatarButton);

        // ========================= THANH TÌM KIẾM =========================
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchContainer.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel(" Tìm kiếm: ");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchLabel.setForeground(Color.BLACK); // Chữ đen

        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                out.println(gson.toJson(new Request("GET_AUCTION", "")));
            }
        });

        searchContainer.add(searchLabel);
        searchContainer.add(searchField);

        // ========================= CHIA TAB =========================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabbedPane.setBackground(Color.WHITE);

        homePanel = new AuctionPanel(item -> { selectedItem = item; bidButton.setEnabled(true); });
        artPanel = new AuctionPanel(item -> { selectedItem = item; bidButton.setEnabled(true); });
        elecPanel = new AuctionPanel(item -> { selectedItem = item; bidButton.setEnabled(true); });
        vehiclePanel = new AuctionPanel(item -> { selectedItem = item; bidButton.setEnabled(true); });
        otherPanel = new AuctionPanel(item -> { selectedItem = item; bidButton.setEnabled(true); });

        tabbedPane.addTab(" Tất cả", homePanel);
        tabbedPane.addTab(" Nghệ thuật", artPanel);
        tabbedPane.addTab(" Điện tử", elecPanel);
        tabbedPane.addTab(" Xe cộ", vehiclePanel);
        tabbedPane.addTab(" Khác", otherPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(searchContainer, BorderLayout.NORTH);
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        // ========================= BOTTOM =========================
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Nhập giá:");
        label.setForeground(Color.BLACK); // Chữ đen
        bidField = new JTextField(15);

        bidButton = new JButton("Đặt giá");
        bidButton.setEnabled(false);
        bidButton.addActionListener(e -> placeBid());

        JButton uploadBtn = new JButton("Đăng sản phẩm");
        uploadBtn.addActionListener(e -> new UploadDialog(this, out, gson).setVisible(true));

        bottomPanel.add(label);
        bottomPanel.add(bidField);
        bottomPanel.add(bidButton);
        bottomPanel.add(uploadBtn);

        // ========================= ADD MAIN =========================
        background.add(topPanel, BorderLayout.NORTH);
        background.add(centerPanel, BorderLayout.CENTER);
        background.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(background);
    }

    private void placeBid() {
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm trước!");
            return;
        }
        try {
            int price = Integer.parseInt(bidField.getText());
            String currentUser = avatarButton.getUsername();
            String payload = String.format("{\"item\":\"%s\",\"price\":%d, \"username\":\"%s\"}", selectedItem, price, currentUser);
            out.println(gson.toJson(new Request("PLACE_BID", payload)));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
        }
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
            String name = item.split("\\|", -1)[0];

            if (!keyword.isEmpty() && !name.toLowerCase().contains(keyword)) continue;

            String category = "Khác", lowerName = name.toLowerCase();
            if (lowerName.contains("tranh") || lowerName.contains("tượng")) category = "Nghệ thuật";
            else if (lowerName.contains("laptop") || lowerName.contains("phone") || lowerName.contains("phím")) category = "Điện tử";
            else if (lowerName.contains("xe") || lowerName.contains("car") || lowerName.contains("bus")) category = "Xe cộ";

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
        bidButton.setEnabled(true);
        bidField.setText("");
        JOptionPane.showMessageDialog(this, message);
    }

    public void auctionEnded(String message) {
        SwingUtilities.invokeLater(() -> {
            bidButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, message);
        });
    }
}