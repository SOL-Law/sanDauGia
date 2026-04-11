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

    private PrintWriter out;
    private Gson gson;

    public AuctionUI(PrintWriter out, Gson gson) {
        this.out = out;
        this.gson = gson;

        setTitle("Phòng đấu giá (Realtime)");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        // ===== BACKGROUND =====
        ImageIcon bg = new ImageIcon("src/main/java/frontend/background2.jpg");
        JLabel background = new JLabel(bg);
        background.setLayout(new BorderLayout());

        // ===== TABLE =====
        String[] columns = {"Item", "Price", "Leader"};
        model = new DefaultTableModel(columns, 0);

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // làm trong suốt
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // ===== PANEL BOTTOM =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false); // 🔥 trong suốt

        JLabel label = new JLabel("Nhập giá:");
        label.setForeground(Color.WHITE);

        bidField = new JTextField(10);

        bidButton = new JButton("Đặt giá");

        bottomPanel.add(label);
        bottomPanel.add(bidField);
        bottomPanel.add(bidButton);

        // ===== BUTTON =====
        bidButton.addActionListener(e -> placeBid());

        // ===== ADD =====
        background.add(scrollPane, BorderLayout.CENTER);
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
                    "{\"item\":\"%s\",\"price\":%d,\"user\":\"client\"}",
                    item, price
            );

            Request req = new Request("PLACE_BID", payload);
            out.println(gson.toJson(req));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
        }
    }

    // =========================
    // UPDATE UI
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
            }
        });
    }
}