package client.ui.auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AnimatedButton extends JButton {

    public AnimatedButton(String text) {
        super(text);

        setBackground(new Color(0, 200, 255));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setFont(new Font("Segoe UI", Font.BOLD, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(getBackground().brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(getBackground().darker());
            }
        });
    }
}