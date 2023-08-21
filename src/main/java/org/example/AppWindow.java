package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
        topNavBits.setBackground(Color.BLACK);

        JButton resetButton = new JButton("Skip");
        resetButton.addActionListener(new ActionListener() {

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

        topNavBits.add(resetButton);
        topNavBits.add(exitButton);

        add(topNavBits, BorderLayout.NORTH);

        textLabel = new JLabel();
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textLabel.setVerticalAlignment(SwingConstants.CENTER);
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
    }

}
