package client.ui.auction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class DesktopItemIcon extends JPanel {

    private boolean hovered = false;

    private boolean selected = false;

    private String itemName;

    public DesktopItemIcon(
            String name,
            String price,
            String leader,
            Consumer<String> onClick
    ) {

        this.itemName = name;

        setPreferredSize(
                new Dimension(130,160)
        );

        setLayout(
                new BoxLayout(
                        this,
                        BoxLayout.Y_AXIS
                )
        );

        setOpaque(false);


        JLabel icon =
                new JLabel(
                        loadIcon(name)
                );

        icon.setAlignmentX(
                CENTER_ALIGNMENT
        );


        JLabel nameLabel =
                new JLabel(name);

        nameLabel.setForeground(Color.WHITE);

        nameLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );


        JLabel priceLabel =
                new JLabel(price + " VNĐ");

        priceLabel.setForeground(
                new Color(0,255,150)
        );

        priceLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );


        JLabel leaderLabel =
                new JLabel("👑 " + leader);

        leaderLabel.setForeground(
                Color.LIGHT_GRAY
        );

        leaderLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );


        add(icon);

        add(Box.createVerticalStrut(6));

        add(nameLabel);

        add(priceLabel);

        add(leaderLabel);


        addMouseListener(
                new MouseAdapter() {

                    public void mouseEntered(
                            MouseEvent e
                    ) {

                        hovered = true;

                        repaint();
                    }

                    public void mouseExited(
                            MouseEvent e
                    ) {

                        hovered = false;

                        repaint();
                    }

                    public void mouseClicked(
                            MouseEvent e
                    ) {

                        selected = true;

                        onClick.accept(itemName);

                        repaint();
                    }
                }
        );
    }


    private ImageIcon loadIcon(String name) {

        String path =
                "src/main/java/frontend/icons/"
                        + name.toLowerCase()
                        + ".png";

        ImageIcon icon =
                new ImageIcon(path);

        if (icon.getIconWidth() <= 0) {

            icon =
                    new ImageIcon(
                            "src/main/java/frontend/icons/default.png"
                    );
        }

        Image img =
                icon.getImage()
                        .getScaledInstance(
                                80,
                                80,
                                Image.SCALE_SMOOTH
                        );

        return new ImageIcon(img);
    }


    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g;


        if (hovered) {

            g2.setColor(
                    new Color(0,150,255,80)
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    20,
                    20
            );
        }


        if (selected) {

            g2.setColor(
                    new Color(0,200,255)
            );

            g2.setStroke(
                    new BasicStroke(3)
            );

            g2.drawRoundRect(
                    2,
                    2,
                    getWidth()-4,
                    getHeight()-4,
                    20,
                    20
            );
        }
    }
}