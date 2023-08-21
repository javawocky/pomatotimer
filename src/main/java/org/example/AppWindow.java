package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AppWindow extends JFrame {
    private JLabel textLabel;
    private boolean skip;
    private JPanel topNavBits;

    public AppWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        // Button Panel
        this.topNavBits = new JPanel(new FlowLayout());
        this.topNavBits.setVisible(false);
        topNavBits.setBackground(Color.BLACK);

        JButton skipButton = new JButton("Skip");
        skipButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                resetTimer();
            }

        });

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }

        });

        topNavBits.add(skipButton);
        topNavBits.add(exitButton);

        add(topNavBits, BorderLayout.NORTH);

        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textLabel.setVerticalAlignment(SwingConstants.CENTER);

        textLabel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                toggleNavVisible();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

        });
        add(textLabel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeText();
            }
        });

        setSize(300, 200);
        setVisible(true);
    }

    public void setText(String text) {
        textLabel.setText(text);
        resizeText();
    }

    public void setColor(Color foreground, Color background) {
        textLabel.setForeground(foreground);
        textLabel.setBackground(background);
        textLabel.setOpaque(true);

        topNavBits.setBackground(background);
    }

    private void resizeText() {
        int width = getContentPane().getWidth();
        int height = getContentPane().getHeight();
        Font currentFont = textLabel.getFont();
        Font resizedFont = currentFont.deriveFont(Font.PLAIN, Math.min(width, height) / 2.5f);
        textLabel.setFont(resizedFont);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppWindow frame = new AppWindow();
            frame.setColor(Color.WHITE, Color.BLACK);
            frame.setText("AB");
        });
    }

    public boolean isSkip() {
        if (this.skip) {
            this.skip = false;
            return true;
        }

        return false;

    }

    private void resetTimer() {
        this.skip = true;
        this.topNavBits.setVisible(false);

    }

    private void toggleNavVisible() {
        this.topNavBits.setVisible(!this.topNavBits.isVisible());
    }
}
