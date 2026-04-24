package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class AnimatedBackground extends JPanel {

    private Image img1;
    private Image img2;

    private float alpha = 0f;
    private boolean showFirst = true;

    public AnimatedBackground(String path1, String path2) {
        img1 = loadImage(path1);
        img2 = loadImage(path2);
    }

    // 🔥 LOAD RESOURCE (QUAN TRỌNG NHẤT)
    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);

            if (url == null) {
                System.out.println("❌ Không tìm thấy ảnh: " + path);
                return null;
            }

            return new ImageIcon(url).getImage();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void switchBackground() {
        alpha = 0f;

        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            alpha += 0.05f;

            if (alpha >= 1f) {
                alpha = 0f;
                showFirst = !showFirst;
                timer.stop();
            }

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // ===== DRAW MAIN BG =====
        if (showFirst) {
            if (img1 != null)
                g2.drawImage(img1, 0, 0, getWidth(), getHeight(), this);
        } else {
            if (img2 != null)
                g2.drawImage(img2, 0, 0, getWidth(), getHeight(), this);
        }

        // ===== FADE EFFECT =====
        if (alpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha
            ));

            if (showFirst) {
                if (img2 != null)
                    g2.drawImage(img2, 0, 0, getWidth(), getHeight(), this);
            } else {
                if (img1 != null)
                    g2.drawImage(img1, 0, 0, getWidth(), getHeight(), this);
            }
        }

        // ===== DEBUG =====
        if (img1 == null) {
            g2.setColor(Color.RED);
            g2.drawString("Missing bg1", 50, 50);
        }
    }
}