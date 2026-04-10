package frontend;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class AuctionUI extends JFrame {

    private JTextArea auctionInfo;
    private JTextField bidField;
    private JButton bidButton;
    private JButton refreshButton;

    private PrintWriter out;
    private Gson gson;

    public AuctionUI(PrintWriter out, Gson gson) {
        this.out = out;
        this.gson = gson;

        setTitle("Phòng đấu giá");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== TEXT HIỂN THỊ =====
        auctionInfo = new JTextArea();
        auctionInfo.setEditable(false);
        add(new JScrollPane(auctionInfo), BorderLayout.CENTER);

        // ===== PANEL DƯỚI =====
        JPanel bottom = new JPanel();

        bidField = new JTextField(10);
        bidButton = new JButton("Đặt giá");
        refreshButton = new JButton("Refresh");

        bottom.add(new JLabel("Giá: "));
        bottom.add(bidField);
        bottom.add(bidButton);
        bottom.add(refreshButton);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENT: BID =====
        bidButton.addActionListener(e -> {
            String price = bidField.getText();

            if (price.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập giá!");
                return;
            }

            Request req = new Request("PLACE_BID", price);
            out.println(gson.toJson(req));
        });

        // ===== EVENT: REFRESH =====
        refreshButton.addActionListener(e -> {
            Request req = new Request("GET_AUCTION", "");
            out.println(gson.toJson(req));
        });
    }

    // ===== UPDATE DATA =====
    public void updateAuctionInfo(String data) {
        auctionInfo.setText(data);
    }
}