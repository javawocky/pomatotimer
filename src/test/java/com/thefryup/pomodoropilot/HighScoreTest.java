package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HighScoreTest {
    
    @Test
    public void testHighScoreTableInitialization() {
        GamePanel panel = new GamePanel();
        
        assertEquals(5, panel.getHighScoreTableSize(), "Should have 5 initial high scores");
        
        // Check scores are in descending order
        for (int i = 0; i < panel.getHighScoreTableSize() - 1; i++) {
            GamePanel.HighScoreEntry current = panel.getHighScoreEntry(i);
            GamePanel.HighScoreEntry next = panel.getHighScoreEntry(i + 1);
            assertTrue(current.score >= next.score, "Scores should be in descending order");
        }
        
        // Check all entries have names
        for (int i = 0; i < panel.getHighScoreTableSize(); i++) {
            GamePanel.HighScoreEntry entry = panel.getHighScoreEntry(i);
            assertNotNull(entry.name, "Entry should have a name");
            assertTrue(entry.name.contains(" "), "Name should have first and last name");
        }
    }
    
    @Test
    public void testAddHighScore() {
        GamePanel panel = new GamePanel();
        
        // Add a score that should be in top 5
        panel.addHighScorePublic("TEST PILOT", 175);
        
        assertEquals(5, panel.getHighScoreTableSize(), "Should still have 5 entries");
        
        // Check the new score is in the table
        boolean found = false;
        for (int i = 0; i < panel.getHighScoreTableSize(); i++) {
            GamePanel.HighScoreEntry entry = panel.getHighScoreEntry(i);
            if (entry.name.equals("TEST PILOT") && entry.score == 175) {
                found = true;
                break;
            }
        }
        assertTrue(found, "New high score should be in table");
        
        // Verify still sorted
        for (int i = 0; i < panel.getHighScoreTableSize() - 1; i++) {
            GamePanel.HighScoreEntry current = panel.getHighScoreEntry(i);
            GamePanel.HighScoreEntry next = panel.getHighScoreEntry(i + 1);
            assertTrue(current.score >= next.score, "Scores should remain sorted");
        }
    }
    
    @Test
    public void testLowScoreNotAdded() {
        GamePanel panel = new GamePanel();
        
        // Get the lowest score
        int lowestScore = panel.getHighScoreEntry(4).score;
        
        // Add a score lower than the lowest
        panel.addHighScorePublic("LOW SCORE", lowestScore - 10);
        
        // Check it's not in the table
        boolean found = false;
        for (int i = 0; i < panel.getHighScoreTableSize(); i++) {
            GamePanel.HighScoreEntry entry = panel.getHighScoreEntry(i);
            if (entry.name.equals("LOW SCORE")) {
                found = true;
                break;
            }
        }
        assertFalse(found, "Score lower than top 5 should not be added");
    }
}
