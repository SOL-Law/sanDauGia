package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class AuctionItemCard extends JPanel {

    private String itemName;
    private boolean hovered = false;
    private boolean selected = false;

    private Consumer<String> onClick;

    private JLabel priceLabel;
    private JLabel leaderLabel;

    public AuctionItemCard(String name,
                           String price,
                           String leader,
                           Consumer<String> onClick) {

        this.itemName = name;
        this.onClick = onClick;

        // QUAN TRỌNG
        setPreferredSize(new Dimension(220,260));

        setLayout(new BorderLayout());

        setBackground(new Color(35,35,35));

        setBorder(
                BorderFactory.createLineBorder(
                        new Color(60,60,60),
                        2
                )
        );

        setCursor(new Cursor(Cursor.HAND_CURSOR));


        // ===== IMAGE PLACEHOLDER =====

        JPanel image = new JPanel();

        image.setPreferredSize(
                new Dimension(200,120)
        );

        image.setBackground(
                new Color(70,70,70)
        );

        JLabel imgText =
                new JLabel("IMAGE");

        imgText.setForeground(Color.WHITE);

        image.add(imgText);


        // ===== INFO PANEL =====

        JPanel info = new JPanel();

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS
                )
        );

        info.setBackground(
                new Color(35,35,35)
        );


        JLabel nameLabel =
                new JLabel(name);

        nameLabel.setForeground(Color.WHITE);

        nameLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );


        priceLabel =
                new JLabel("Giá: " + price);

        priceLabel.setForeground(
                new Color(0,255,150)
        );


        leaderLabel =
                new JLabel("Leader: " + leader);

        leaderLabel.setForeground(
                Color.LIGHT_GRAY
        );


        JButton bidBtn =
                new JButton("Đặt giá");

        bidBtn.setBackground(
                new Color(0,150,255)
        );

        bidBtn.setForeground(Color.WHITE);


        info.add(nameLabel);

        info.add(Box.createVerticalStrut(5));

        info.add(priceLabel);

        info.add(leaderLabel);

        info.add(Box.createVerticalStrut(10));

        info.add(bidBtn);


        add(image, BorderLayout.NORTH);

        add(info, BorderLayout.CENTER);


        // ===== CLICK EVENT =====

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {

                hovered = true;

                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {

                hovered = false;

                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                selected = true;

                onClick.accept(itemName);

                repaint();
            }
        });
    }


    public void update(String price,
                       String leader) {

        priceLabel.setText("Giá: " + price);

        leaderLabel.setText("Leader: " + leader);
    }


    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g;


        if (hovered) {

            g2.setColor(
                    new Color(0,0,0,80)
            );

            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );
        }


        if (selected) {

            g2.setColor(
                    new Color(0,150,255)
            );

            g2.setStroke(
                    new BasicStroke(3)
            );

            g2.drawRect(
                    2,
                    2,
                    getWidth()-4,
                    getHeight()-4
            );
        }
    }
}