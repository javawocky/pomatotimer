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
    private GamePanel gamePanel;
    private boolean skip;
    private JPanel topNavBits;

    public AppWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
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

        gamePanel = new GamePanel();
        gamePanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleNavVisible();
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        add(gamePanel, BorderLayout.CENTER);

        setSize(320, 240);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setTimeText(String text) {
        gamePanel.setTimeText(text);
    }

    public void startWork() {
        gamePanel.startWork();
    }

    public void startBreak() {
        gamePanel.startBreak();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppWindow frame = new AppWindow();
            frame.setTimeText("00:00");
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
