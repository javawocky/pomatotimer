package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GamePanelTest {
    private GamePanel gamePanel;

    @BeforeEach
    public void setUp() {
        gamePanel = new GamePanel();
        gamePanel.startWork();
    }

    @Test
    public void testObstacleSpawning() {
        gamePanel.step(120);
        assertTrue(gamePanel.getObstacles().size() > 0, "At least 1 obstacle should spawn after 120 frames");
    }

    @Test
    public void testObstacleMovement() {
        gamePanel.setObstacle(320, 100);
        gamePanel.step(10);
        GamePanel.Obstacle obs = gamePanel.getObstacles().get(0);
        assertEquals(290, obs.x, 2, "Obstacle should move left by ~30 pixels");
    }

    @Test
    public void testObstacleRemoval() {
        gamePanel.setObstacle(-60, 100);
        gamePanel.step(1);
        assertEquals(0, gamePanel.getObstacles().size(), "Obstacle should be removed when x < -50");
    }

    @Test
    public void testPlaneStaysInBounds() {
        for (int i = 0; i < 300; i++) {
            gamePanel.step(1);
            double planeY = gamePanel.getPlaneY();
            assertTrue(planeY >= 10 && planeY <= 200, 
                "Plane Y should stay between 10 and 200, but was " + planeY + " at frame " + i);
        }
    }

    @Test
    public void testPlaneNavigatesToGap() {
        gamePanel.setObstacle(100, 80);
        double initialY = gamePanel.getPlaneY();
        gamePanel.step(20);
        double finalY = gamePanel.getPlaneY();
        assertTrue(finalY < initialY, 
            "Plane should move upward toward gap at y=80, initial=" + initialY + ", final=" + finalY);
    }

    @Test
    public void testPlaneAvoidsTopPipe() {
        gamePanel.setObstacle(100, 120);
        double initialY = 50;
        gamePanel.step(30);
        double finalY = gamePanel.getPlaneY();
        assertTrue(finalY > initialY, "Plane should move down away from top pipe");
    }

    @Test
    public void testPlaneAvoidsBottomPipe() {
        gamePanel.setObstacle(100, 80);
        double initialY = 150;
        gamePanel.step(30);
        double finalY = gamePanel.getPlaneY();
        assertTrue(finalY < initialY, "Plane should move up away from bottom pipe");
    }

    @Test
    public void testBreakModeStopsObstacles() {
        gamePanel.step(120);
        int obstacleCount = gamePanel.getObstacles().size();
        gamePanel.startBreak();
        gamePanel.step(100);
        assertEquals(obstacleCount, gamePanel.getObstacles().size(), 
            "No new obstacles should spawn during break mode");
    }

    @Test
    public void testBreakModePlaneLands() {
        gamePanel.startBreak();
        gamePanel.step(60);
        assertTrue(gamePanel.getPlaneY() >= 200, "Plane should land at y=200 after 60 frames");
    }

    @Test
    public void testWorkModeResetsState() {
        gamePanel.setObstacle(100, 100);
        gamePanel.step(50);
        gamePanel.startWork();
        assertEquals(0, gamePanel.getObstacles().size(), "Obstacles should be cleared");
        assertEquals(100, gamePanel.getPlaneY(), 0.1, "Plane should reset to y=100");
        assertEquals(0, gamePanel.getFrameCount(), "Frame counter should reset to 0");
    }
}
