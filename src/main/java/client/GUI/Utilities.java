package client.GUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Utilities {
    public static JPanel createRoundedPanel(boolean isOpaque, int borderThickness, int row, int col) {
        JPanel panel = new JPanel();
        panel.setOpaque(isOpaque);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(borderThickness),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setLayout(new GridLayout(row, col, 10, 10));
        return panel;
    }

    public static JTextField createRoundedTextField() {
        JTextField textField = new JTextField(15);
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createCompoundBorder(
                textField.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return textField;
    }

    public static JPasswordField createRoundedPasswordField() {
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setOpaque(false);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                passwordField.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return passwordField;
    }

    public static JButton createRoundedButton(String text) {
        JButton button = new JButton(text);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                button.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return button;
    }

    public static JLabel createBackground(String path, int width, int height) {
        ImageIcon backgroundImageIcon = new ImageIcon(path); // Replace with the path to your image
        Image backgroundImage = backgroundImageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(backgroundImage));
    }

    static class RoundedBorder implements Border {
        private final int arc;

        public RoundedBorder(int arc) {
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Set the background color with 70% transparency
            Color backgroundColor = new Color(255, 255, 255, (int) (255 * 0.5));
            g2d.setColor(backgroundColor);

            // Fill the rounded rectangle with the background color
            g2d.fill(new RoundRectangle2D.Double(x, y, width - 1, height - 1, arc, arc));

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}

