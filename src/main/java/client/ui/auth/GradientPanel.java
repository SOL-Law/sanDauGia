package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 120, 255),
                getWidth(), getHeight(), new Color(0, 0, 50)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}