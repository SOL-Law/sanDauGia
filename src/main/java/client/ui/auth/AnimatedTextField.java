package client.ui.auth;

import javax.swing.*;
import java.awt.*;

public class AnimatedTextField extends JTextField {

    private String placeholder;

    public AnimatedTextField(String placeholder) {
        this.placeholder = placeholder;

        // 🔥 QUAN TRỌNG (fix mất field)
        setPreferredSize(new Dimension(280, 40));
        setMaximumSize(new Dimension(280, 40));

        setBackground(new Color(30,30,30));
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);

        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.GRAY);
            g2.drawString(placeholder, 10, getHeight()/2 + 5);
        }
    }
}