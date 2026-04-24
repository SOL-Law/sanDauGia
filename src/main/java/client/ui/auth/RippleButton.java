package client.ui.auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class RippleButton extends JButton {

    private int x, y;
    private float radius = 0;
    private float alpha = 0;

    public RippleButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setForeground(Color.WHITE);
        setBackground(new Color(0, 180, 255));

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
                radius = 0;
                alpha = 0.5f;
                animate();
            }
        });
    }

    private void animate() {
        Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            radius += 10;
            alpha -= 0.03f;

            if (alpha <= 0) timer.stop();
            repaint();
        });
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(getBackground());
        g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);

        if (alpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.WHITE);
            g2.fillOval(x - (int)radius/2, y - (int)radius/2, (int)radius, (int)radius);
        }

        super.paintComponent(g);
    }
}