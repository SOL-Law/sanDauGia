package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class FadePanel extends JPanel {

    private float alpha = 1f;

    public FadePanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha
        ));

        super.paintComponent(g);
    }
}