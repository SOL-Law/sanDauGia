package frontend;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuctionUI extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JTextField bidField;
    private JButton bidButton;
    private JButton refreshButton;

    private JTextArea historyArea;
    private JLabel timerLabel;

    private PrintWriter out;
    private Gson gson;
    private String userRole;

    public AuctionUI(PrintWriter out, Gson gson,String role) {
        this.out = out;
        this.gson = gson;
        this.userRole = role;
        setTitle("PHÒNG ĐẤU GIÁ REALTIME - Quyền: " + this.userRole); // Đổi title cho ngầu

        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        // ===== BACKGROUND PANEL (CUSTOM VẼ ẢNH) =====
        JPanel background = new JPanel() {
            Image bg = new ImageIcon("src/main/java/frontend/background2.jpg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // vẽ ảnh full màn
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

                // 🔥 phủ lớp mờ (glass effect)
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        background.setLayout(new BorderLayout());

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel(" PHÒNG ĐẤU GIÁ REALTIME");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        timerLabel = new JLabel(" --s");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timerLabel.setForeground(Color.YELLOW);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(timerLabel, BorderLayout.EAST);

        // ===== TABLE =====
        String[] columns = {"Item", "Price", "Leader"};
        model = new DefaultTableModel(columns, 0);

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Danh sách sản phẩm"));

        // ===== HISTORY =====
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setOpaque(false);
        historyArea.setForeground(new Color(0, 255, 150));
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 13));

        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setOpaque(false);
        historyScroll.getViewport().setOpaque(false);
        historyScroll.setBorder(BorderFactory.createTitledBorder(" Lịch sử đấu giá"));

        // ===== SPLIT =====
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                tableScroll,
                historyScroll
        );
        splitPane.setDividerLocation(550);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // ===== BOTTOM =====

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);

        JButton startButton = new JButton("Bắt đầu");

        // 3. Nếu là Admin thì gắn nút vào cái đáy (nhớ dùng đúng tên bottomPanel)
        if ("ADMIN".equals(this.userRole)) {
            bottomPanel.add(startButton);
        }

        // 4. Bắt sự kiện bấm nút
        startButton.addActionListener(e -> {
            out.println(gson.toJson(new Request("START_SESSION", "")));
        });

        JLabel label = new JLabel("Nhập giá:");
        label.setForeground(Color.WHITE);

        bidField = new JTextField(10);

        bidButton = new JButton("Đặt giá");
        bidButton.setEnabled(false);

        refreshButton = new JButton("Refresh");
        bidButton.setBackground(new Color(0, 150, 255));
        bidButton.setForeground(Color.WHITE);

        bottomPanel.add(label);
        bottomPanel.add(bidField);
        bottomPanel.add(bidButton);

        // ===== EVENT =====
        bidButton.addActionListener(e -> placeBid());

        // ===== ADD =====
        background.add(topPanel, BorderLayout.NORTH);
        background.add(splitPane, BorderLayout.CENTER);
        background.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(background);
    }

    // =========================
    // PLACE BID
    // =========================
    private void placeBid() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn item trước!");
            return;
        }

        String item = model.getValueAt(row, 0).toString();

        try {
            int price = Integer.parseInt(bidField.getText());

            String payload = String.format(
                    "{\"item\":\"%s\",\"price\":%d}",
                    item, price
            );

            Request req = new Request("PLACE_BID", payload);
            out.println(gson.toJson(req));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
        }
    }

    // =========================
    // UPDATE DATA
    // =========================
    public void updateAuctionInfo(String data) {

        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);

            String[] items = data.split(";");

            for (String item : items) {
                if (item.isEmpty()) continue;

                String[] parts = item.split("\\|");

                model.addRow(new Object[]{
                        parts[0],
                        parts[1],
                        parts[2]
                });

                addHistory(parts[0], parts[1], parts[2]);
            }
        });
    }
    // ===== THÊM HÀM NÀY ĐỂ MỞ KHÓA GIAO DIỆN =====
    public void startNewSession(String message) {
        // Mở khóa nút bấm
        bidButton.setEnabled(true);

        // (Tùy chọn) Xóa trắng ô nhập tiền của ván cũ đi cho sạch
        bidField.setText("");
        JOptionPane.showMessageDialog(this, message);
        // Hiện thông báo cho khí thế
    }

    // =========================
    // HISTORY
    // =========================
    private void addHistory(String item, String price, String user) {

        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        historyArea.append(String.format(
                "[%s] %s → %s VNĐ (%s)\n",
                time, item, price, user
        ));

        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    // =========================
    // AUCTION END
    // =========================
    public void auctionEnded(String message) {

        SwingUtilities.invokeLater(() -> {

            timerLabel.setText(" KẾT THÚC");
            bidButton.setEnabled(false);

            JOptionPane.showMessageDialog(this, message);
        });
    }

    // =========================
    // TIMER
    // =========================
    public void updateTimer(int time) {
        SwingUtilities.invokeLater(() -> {
            if (time > 0) {
                timerLabel.setText(" " + time + "s");
            } else {
                timerLabel.setText(" KẾT THÚC");
            }
            if (!bidButton.isEnabled()) {
                bidButton.setEnabled(true);
            }
        });
    }
}