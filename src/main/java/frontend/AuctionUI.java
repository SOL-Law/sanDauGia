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
import java.text.SimpleDateFormat;
import java.util.Date;

import client.ui.auction.UserProfileButton;

public class AuctionUI extends JFrame {

    private JTextField bidField;
    private JButton bidButton;

    private JTextArea historyArea;
    private JLabel timerLabel;

    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    private String userRole;
    private AuctionPanel auctionPanel;

    private String selectedItem = null;

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

        // 🔥 FIX: request initial data SAU khi UI + listener ổn định
        SwingUtilities.invokeLater(() -> {
            requestInitialData();
        });
    }

    // =========================
    // REQUEST INIT DATA
    // =========================
    private void requestInitialData() {
        out.println(gson.toJson(
                new Request("GET_AUCTION", "")
        ));
    }

    // =========================
    // LISTEN SERVER
    // =========================
    private void startListeningServer() {

        new Thread(() -> {

            try {

                String msg;

                while ((msg = in.readLine()) != null) {
                    System.out.println(">>> Server response : " + msg);

                    Request res = gson.fromJson(msg, Request.class);

                    switch (res.getType()) {

                        case "UPDATE_AUCTION":

                            SwingUtilities.invokeLater(() -> {
                                updateAuctionInfo(res.getPayload());
                            });

                            break;

                        case "START_SESSION":
                            startNewSession("Phiên đấu giá bắt đầu!");
                            break;

                        case "END_SESSION":
                            auctionEnded("Phiên đấu giá kết thúc!");
                            break;

                        case "TIMER":
                            updateTimer(Integer.parseInt(res.getPayload()));
                            break;
                    }
                }

            } catch (Exception e) {
                System.out.println("Server disconnected");
            }

        }).start();
    }

    // =========================
    // INIT UI
    // =========================
    private void initUI() {

        JPanel background = new JPanel() {

            Image bg = new ImageIcon(
                    "src/main/java/frontend/background2.jpg"
            ).getImage();

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

                g.setColor(new Color(0, 0, 0, 80));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        background.setLayout(new BorderLayout());

        // ========================= TOP =========================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel(" PHÒNG ĐẤU GIÁ REALTIME");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        timerLabel = new JLabel(" --s");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timerLabel.setForeground(Color.YELLOW);

        JButton historyButton = new JButton("📜 History");
        historyButton.addActionListener(e -> showHistoryPopup());

        // GỌI CÁI AVATAR TRÒN RA (Truyền tên user vào, tạm thời mình để cứng là userRole)
        UserProfileButton avatarButton = new UserProfileButton(userRole);

        // --- SẮP XẾP LẠI GÓC PHẢI TRÊN CÙNG ---
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5)); // Thêm khoảng cách cho đẹp
        rightTop.setOpaque(false);

        rightTop.add(timerLabel);
        rightTop.add(historyButton);
        rightTop.add(avatarButton); //  Nhét Avatar vào cuối cùng

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(rightTop, BorderLayout.EAST);

        // ========================= AUCTION PANEL =========================
        auctionPanel = new AuctionPanel(item -> {
            selectedItem = item;
            bidButton.setEnabled(true);
        });

        // ========================= HISTORY =========================
        historyArea = new JTextArea();
        historyArea.setEditable(false);

        // ========================= BOTTOM =========================
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);

        JButton startButton = new JButton("Bắt đầu");

        if ("ADMIN".equals(userRole)) {
            bottomPanel.add(startButton);
        }

        startButton.addActionListener(e ->
                out.println(gson.toJson(new Request("START_SESSION", "")))
        );

        JLabel label = new JLabel("Nhập giá:");
        label.setForeground(Color.WHITE);

        bidField = new JTextField(10);

        bidButton = new JButton("Đặt giá");
        bidButton.setEnabled(false);

        JButton uploadBtn = new JButton("Đăng sản phẩm");

        uploadBtn.addActionListener(e ->
                new UploadDialog(this, out, gson).setVisible(true)
        );

        bottomPanel.add(label);
        bottomPanel.add(bidField);
        bottomPanel.add(bidButton);
        bottomPanel.add(uploadBtn);

        bidButton.addActionListener(e -> placeBid());

        // ========================= ADD MAIN =========================
        background.add(topPanel, BorderLayout.NORTH);
        background.add(auctionPanel, BorderLayout.CENTER);
        background.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(background);
    }

    // ========================= HISTORY POPUP =========================
    private void showHistoryPopup() {
        new HistoryDialog(this, historyArea.getText()).setVisible(true);
    }

    // ========================= BID =========================
    private void placeBid() {

        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm trước!");
            return;
        }

        try {

            int price = Integer.parseInt(bidField.getText());

            String payload = String.format(
                    "{\"item\":\"%s\",\"price\":%d}",
                    selectedItem,
                    price
            );

            out.println(gson.toJson(
                    new Request("PLACE_BID", payload)
            ));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
        }
    }

    // ========================= UPDATE =========================
    public void updateAuctionInfo(String data) {

        if (data == null || data.replace(" ", "").isEmpty()) {
            return;
        }

        auctionPanel.loadItems(data);

        String[] items = data.split(";");

        for (String item : items) {

            if (item.isEmpty()) continue;

            String[] parts = item.split("\\|");

            if (parts.length < 3) continue;

            addHistory(parts[0], parts[1], parts[2]);
        }
    }

    // ========================= HISTORY =========================
    private void addHistory(String item, String price, String user) {

        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        historyArea.append(
                String.format("[%s] %s → %s VNĐ (%s)\n",
                        time, item, price, user
                )
        );
    }

    // ========================= SESSION =========================
    public void startNewSession(String message) {
        bidButton.setEnabled(true);
        bidField.setText("");
        JOptionPane.showMessageDialog(this, message);
    }

    public void auctionEnded(String message) {

        SwingUtilities.invokeLater(() -> {
            timerLabel.setText(" KẾT THÚC");
            bidButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, message);
        });
    }

    public void updateTimer(int time) {

        SwingUtilities.invokeLater(() -> {
            timerLabel.setText(time > 0 ? " " + time + "s" : " KẾT THÚC");
        });
    }
}