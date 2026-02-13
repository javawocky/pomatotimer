package com.thefryup.pomodoropilot;

import javax.swing.*;

public class SpaceShooterTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Shooter - Physics Test");
            SpaceShooterPanel panel = new SpaceShooterPanel();
            
            frame.add(panel);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            panel.requestFocusInWindow();
        });
    }
}
