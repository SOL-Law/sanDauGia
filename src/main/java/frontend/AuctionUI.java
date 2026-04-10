package frontend;

import javax.swing.*;
import java.awt.*;

public class AuctionUI extends JFrame {

    private JTextArea auctionInfo;
    private JTextField bidField;
    private JButton bidButton;

    public AuctionUI() {

        setTitle("Phòng đấu giá");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Hiển thị thông tin =====
        auctionInfo = new JTextArea();
        auctionInfo.setEditable(false);
        auctionInfo.setText("Thông tin phiên đấu giá sẽ hiển thị ở đây...");
        add(new JScrollPane(auctionInfo), BorderLayout.CENTER);

        // ===== Panel nhập giá =====
        JPanel bottomPanel = new JPanel();

        bidField = new JTextField(10);
        bidButton = new JButton("Đặt giá");

        bottomPanel.add(new JLabel("Giá: "));
        bottomPanel.add(bidField);
        bottomPanel.add(bidButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }
}