package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class LoadingOverlay extends JPanel {

    private float angle = 0;

    public LoadingOverlay() {
        setOpaque(false);

        Timer timer = new Timer(16, e -> {
            angle += 10;
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(0,0,0,150));
        g2.fillRect(0,0,getWidth(),getHeight());

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(4));

        g2.drawArc(getWidth()/2 - 20, getHeight()/2 - 20,
                40, 40, (int) angle, 270);
    }
}