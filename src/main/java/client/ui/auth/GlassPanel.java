package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class GlassPanel extends JPanel {

    public GlassPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // 🔥 DARK GLASS (không còn xanh)
        g2.setColor(new Color(20, 20, 20, 220));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        // viền nhẹ
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);

        super.paintComponent(g);
    }
}