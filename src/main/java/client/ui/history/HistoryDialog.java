package client.ui.history;

import javax.swing.*;
import java.awt.*;

public class HistoryDialog extends JDialog {

    private JTextArea historyArea;

    public HistoryDialog(Frame parent, String historyText) {

        super(parent, "Auction History", true);

        setSize(450, 550);

        setLocationRelativeTo(parent);


        JPanel background = new JPanel() {

            Image bg =
                    new ImageIcon(
                            "src/main/java/frontend/history_bg.jpg"
                    ).getImage();

            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                g.drawImage(
                        bg,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        this
                );

                // overlay kính mờ
                g.setColor(new Color(0,0,0,150));

                g.fillRect(
                        0,
                        0,
                        getWidth(),
                        getHeight()
                );
            }
        };


        background.setLayout(new BorderLayout());


        historyArea =
                new JTextArea(historyText);


        historyArea.setEditable(false);

        historyArea.setOpaque(false);

        historyArea.setForeground(
                new Color(0,255,150)
        );

        historyArea.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        14
                )
        );


        JScrollPane scroll =
                new JScrollPane(historyArea);

        scroll.setOpaque(false);

        scroll.getViewport().setOpaque(false);


        background.add(scroll);

        setContentPane(background);
    }
}