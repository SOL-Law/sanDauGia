package frontend;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.PrintWriter;

public class AuctionUI extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    private JLabel lblItem;
    private JLabel lblPrice;
    private JLabel lblLeader;

    private JTextField txtBid;
    private JButton btnBid;
    private JButton btnRefresh;

    private PrintWriter out;
    private Gson gson;

    public AuctionUI(PrintWriter out, Gson gson) {
        this.out = out;
        this.gson = gson;

        setTitle("Phòng đấu giá");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {

        // ===== TABLE ITEM =====
        String[] columns = {"Item", "Giá hiện tại", "Người dẫn đầu"};
        tableModel = new DefaultTableModel(columns, 0);

        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== PANEL CHI TIẾT =====
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        lblItem = new JLabel("Item: ");
        lblPrice = new JLabel("Giá: ");
        lblLeader = new JLabel("Leader: ");

        infoPanel.add(lblItem);
        infoPanel.add(lblPrice);
        infoPanel.add(lblLeader);

        add(infoPanel, BorderLayout.NORTH);

        // ===== PANEL BID =====
        JPanel bottomPanel = new JPanel();

        txtBid = new JTextField(10);
        btnBid = new JButton("Đặt giá");
        btnRefresh = new JButton("Refresh");

        bottomPanel.add(new JLabel("Giá:"));
        bottomPanel.add(txtBid);
        bottomPanel.add(btnBid);
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);

        // ===== EVENT =====
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                lblItem.setText("Item: " + tableModel.getValueAt(row, 0));
                lblPrice.setText("Giá: " + tableModel.getValueAt(row, 1));
                lblLeader.setText("Leader: " + tableModel.getValueAt(row, 2));
            }
        });

        btnBid.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Chọn item trước!");
                return;
            }

            String price = txtBid.getText();
            String item = tableModel.getValueAt(row, 0).toString();

            String payload = String.format("{\"item\":\"%s\", \"price\":%s}", item, price);
            Request req = new Request("PLACE_BID", payload);

            out.println(gson.toJson(req));
        });

        btnRefresh.addActionListener(e -> {
            Request req = new Request("GET_AUCTION", "");
            out.println(gson.toJson(req));
        });
    }

    // ===== UPDATE TỪ SERVER =====
    public void updateAuctionInfo(String data) {
        tableModel.setRowCount(0);

        // format: item|price|leader;item|price|leader
        String[] items = data.split(";");
        for (String i : items) {
            String[] parts = i.split("\\|");
            if (parts.length == 3) {
                tableModel.addRow(parts);
            }
        }
    }
}