package frontend;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;

public class AuctionUI extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JTextField bidField;
    private JButton bidButton;

    private JLabel statusLabel;
    private JLabel timerLabel;

    private JTextArea logArea;

    private PrintWriter out;
    private Gson gson;

    public AuctionUI(PrintWriter out, Gson gson) {
        this.out = out;
        this.gson = gson;

        setTitle("🔥 PHÒNG ĐẤU GIÁ REALTIME");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        // ===== BACKGROUND =====
        ImageIcon bg = new ImageIcon("src/main/java/frontend/background2.jpg");
        JLabel background = new JLabel(bg);
        background.setLayout(new BorderLayout());

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.setOpaque(false);

        statusLabel = new JLabel("Trạng thái: ĐANG ĐẤU", JLabel.LEFT);
        statusLabel.setForeground(Color.GREEN);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        timerLabel = new JLabel("⏱ Thời gian: --", JLabel.RIGHT);
        timerLabel.setForeground(Color.YELLOW);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(statusLabel);
        topPanel.add(timerLabel);

        // ===== TABLE =====
        String[] columns = {"Item", "Price", "Leader"};
        model = new DefaultTableModel(columns, 0);

        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // ===== LOG AREA =====
        logArea = new JTextArea(8, 20);
        logArea.setEditable(false);
        logArea.setBackground(new Color(0, 0, 0, 150));
        logArea.setForeground(Color.WHITE);

        JScrollPane logScroll = new JScrollPane(logArea);

        // ===== RIGHT PANEL =====
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(new JLabel("📜 Lịch sử đấu giá"), BorderLayout.NORTH);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        // ===== CENTER =====
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setOpaque(false);
        centerPanel.add(scrollPane);
        centerPanel.add(rightPanel);

        // ===== BOTTOM PANEL =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);

        JLabel label = new JLabel("Nhập giá:");
        label.setForeground(Color.WHITE);

        bidField = new JTextField(10);

        bidButton = new JButton("Đặt giá");

        bottomPanel.add(label);
        bottomPanel.add(bidField);
        bottomPanel.add(bidButton);

        bidButton.addActionListener(e -> placeBid());

        // ===== ADD =====
        background.add(topPanel, BorderLayout.NORTH);
        background.add(centerPanel, BorderLayout.CENTER);
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
    // UPDATE TABLE
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

                // 🔥 log realtime
                logArea.append("Update: " + parts[0] + " -> " + parts[1] + " (" + parts[2] + ")\n");
            }
        });
    }

    // =========================
    // UPDATE TIMER
    // =========================
    public void updateTimer(String time) {
        SwingUtilities.invokeLater(() -> {
            timerLabel.setText("⏱ Thời gian: " + time);
        });
    }

    // =========================
    // AUCTION END
    // =========================
    public void auctionEnded(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("ĐÃ KẾT THÚC");
            statusLabel.setForeground(Color.RED);

            JOptionPane.showMessageDialog(this, message);
        });
    }
}