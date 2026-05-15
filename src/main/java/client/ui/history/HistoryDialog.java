package client.ui.history;

import javax.swing.*;
import java.awt.*;

public class HistoryDialog extends JDialog {
    private JTextArea historyArea;

    // FIX LAG: Load ảnh 1 lần duy nhất
    private static final Image BG_IMAGE = new ImageIcon("src/main/java/frontend/history_bg.jpg").getImage();

    public HistoryDialog(Window parent, String historyText) {
        super(parent, "Auction History", ModalityType.APPLICATION_MODAL);
        setSize(450, 550);
        setLocationRelativeTo(parent);

        // Định nghĩa JPanel có đè hàm vẽ background
        JPanel background = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(BG_IMAGE, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0, 0, 0, 150)); // Lớp phủ tối cho dễ đọc chữ
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        historyArea = new JTextArea(historyText);
        historyArea.setEditable(false);
        historyArea.setOpaque(false);
        historyArea.setForeground(new Color(0, 255, 150)); // Màu xanh Matrix
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        background.add(scroll, BorderLayout.CENTER);
        setContentPane(background);
    }
}