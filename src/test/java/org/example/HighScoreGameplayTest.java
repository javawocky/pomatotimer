package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HighScoreGameplayTest {
    
    @Test
    public void testScoring() {
        GamePanel panel = new GamePanel();
        panel.startWork();
        
        // Add one obstacle
        panel.setObstacle(320, 120);
        
        // Update until it passes scoring point (x < 80-50 = 30)
        for (int i = 0; i < 100; i++) {
            panel.updatePublic();
            if (i % 10 == 0) {
                System.out.println("Frame " + i + ": Score = " + panel.getScore());
            }
        }
        
        System.out.println("Final score: " + panel.getScore());
        assertTrue(panel.getScore() >= 10, "Should have scored at least 10 points");
    }
    
    @Test
    public void testHighScoreAddedOnCrash() {
        GamePanel panel = new GamePanel();
        panel.startWork();
        
        String playerName = panel.getPlayerName();
        System.out.println("Player name: " + playerName);
        
        // Print initial high score table
        System.out.println("Initial high score table:");
        for (int i = 0; i < panel.getHighScoreTableSize(); i++) {
            GamePanel.HighScoreEntry entry = panel.getHighScoreEntry(i);
            System.out.println("  " + (i+1) + ": " + entry.name + " - " + entry.score);
        }
        
        // Manually set score to test high score logic
        panel.setScoreForTest(210);
        
        int currentScore = panel.getScore();
        System.out.println("\nCurrent score: " + currentScore);
        
        // Check the condition
        int lowestScore = panel.getHighScoreEntry(4).score;
        System.out.println("Lowest score in table: " + lowestScore);
        System.out.println("Should qualify: " + (currentScore > lowestScore));
        
        // Trigger collision
        panel.setObstacle(85, 50);
        panel.setPlaneY(120);
        panel.updatePublic();
        
        assertTrue(panel.isGameOver(), "Game should be over after collision");
        
        // Check if high score was added to table
        boolean foundPlayer = false;
        System.out.println("\nFinal high score table:");
        for (int i = 0; i < panel.getHighScoreTableSize(); i++) {
            GamePanel.HighScoreEntry entry = panel.getHighScoreEntry(i);
            System.out.println("  " + (i+1) + ": " + entry.name + " - " + entry.score);
            if (entry.name.equals(playerName) && entry.score == currentScore) {
                foundPlayer = true;
            }
        }
        
        System.out.println("\nShow new high score: " + panel.isShowNewHighScore());
        System.out.println("New high score value: " + panel.getNewHighScoreValue());
        
        assertTrue(foundPlayer, "Player '" + playerName + "' with score " + currentScore + " should be in high score table");
        assertTrue(panel.isShowNewHighScore(), "New high score message should be shown");
    }
    
    @Test
    public void testHighScoreUpdatedOnBreakStart() {
        GamePanel panel = new GamePanel();
        panel.startWork();
        
        String playerName = panel.getPlayerName();
        System.out.println("Player name: " + playerName);
        
        // Set a high score
        panel.setScoreForTest(210);
        
        System.out.println("Score before break: " + panel.getScore());
        
        // Start break (simulating timer ending)
        panel.startBreak();
        
        // Check if high score was added
        boolean foundPlayer = false;
        System.out.println("\nHigh score table after break:");
        for (int i = 0; i < panel.getHighScoreTableSize(); i++) {
            GamePanel.HighScoreEntry entry = panel.getHighScoreEntry(i);
            System.out.println("  " + (i+1) + ": " + entry.name + " - " + entry.score);
            if (entry.name.equals(playerName) && entry.score == 210) {
                foundPlayer = true;
            }
        }
        
        assertTrue(foundPlayer, "Player should be in high score table after break starts");
        assertTrue(panel.isShowNewHighScore(), "New high score message should be shown");
    }
}
