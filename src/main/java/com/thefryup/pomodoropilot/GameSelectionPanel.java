package com.thefryup.pomodoropilot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameSelectionPanel extends JPanel {
    private int selectedGame = 0; // 0 = Plane Game, 1 = Space Shooter
    private long startTime;
    private static final int AUTO_SELECT_SECONDS = 10;
    private boolean selectionMade = false;
    private JFrame parentFrame;
    
    public GameSelectionPanel(JFrame frame) {
        this.parentFrame = frame;
        this.startTime = System.currentTimeMillis();
        
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.BLACK);
        setFocusable(true);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (selectionMade) return;
                
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedGame = 1 - selectedGame; // Toggle between 0 and 1
                    repaint();
                }
                
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    makeSelection();
                }
            }
        });
        
        // Timer to check for auto-select
        Timer timer = new Timer(100, e -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsed >= AUTO_SELECT_SECONDS && !selectionMade) {
                selectedGame = 0; // Auto-select Plane Game
                makeSelection();
            }
            repaint();
        });
        timer.start();
    }
    
    private void makeSelection() {
        selectionMade = true;
        
        if (selectedGame == 0) {
            // Launch Plane Game through AppWindow
            SwingUtilities.invokeLater(() -> {
                parentFrame.dispose();
                AppWindow appWindow = new AppWindow();
                appWindow.setVisible(true);
                appWindow.startWork();
            });
        } else {
            // Launch Space Shooter
            SwingUtilities.invokeLater(() -> {
                parentFrame.getContentPane().removeAll();
                SpaceShooterPanel spacePanel = new SpaceShooterPanel();
                parentFrame.add(spacePanel);
                parentFrame.revalidate();
                parentFrame.repaint();
                spacePanel.requestFocusInWindow();
            });
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "POMODORO PILOT";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 100);
        
        // Subtitle
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String subtitle = "Select Game Mode";
        fm = g2d.getFontMetrics();
        int subtitleX = (getWidth() - fm.stringWidth(subtitle)) / 2;
        g2d.drawString(subtitle, subtitleX, 150);
        
        // Game options
        int optionY = 250;
        int spacing = 80;
        
        // Plane Game option
        drawOption(g2d, "PLANE GAME", optionY, selectedGame == 0);
        
        // Space Shooter option
        drawOption(g2d, "SPACE SHOOTER", optionY + spacing, selectedGame == 1);
        
        // Instructions
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(Color.GRAY);
        String instructions = "↑↓ to select  •  ENTER to confirm";
        fm = g2d.getFontMetrics();
        int instrX = (getWidth() - fm.stringWidth(instructions)) / 2;
        g2d.drawString(instructions, instrX, 450);
        
        // Auto-select countdown
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        long remaining = AUTO_SELECT_SECONDS - elapsed;
        if (remaining > 0 && !selectionMade) {
            String countdown = "Auto-selecting Plane Game in " + remaining + "s";
            fm = g2d.getFontMetrics();
            int countX = (getWidth() - fm.stringWidth(countdown)) / 2;
            g2d.drawString(countdown, countX, 420);
        }
    }
    
    private void drawOption(Graphics2D g2d, String text, int y, boolean selected) {
        FontMetrics fm;
        
        if (selected) {
            // Selected option - larger and highlighted
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.setColor(Color.YELLOW);
            
            // Draw arrow
            g2d.drawString("►", 150, y);
        } else {
            // Unselected option
            g2d.setFont(new Font("Arial", Font.PLAIN, 32));
            g2d.setColor(Color.LIGHT_GRAY);
        }
        
        fm = g2d.getFontMetrics();
        g2d.drawString(text, 200, y);
    }
}
