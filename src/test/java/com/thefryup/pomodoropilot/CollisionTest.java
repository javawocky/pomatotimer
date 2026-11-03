package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CollisionTest {

    // Obstacle with peaks at x=105 (centre), top peak y=80, bottom peak y=160
    private final GamePanel.Obstacle obs = new GamePanel.Obstacle(80, 120);

    @Test
    public void testCentredInGapIsSafe() {
        assertFalse(Collision.planeHitsObstacle(93, 108, 24, 24, 24, 0, obs));
        assertFalse(Collision.planeHitsObstacle(93, 108, 24, 24, 20, 30, obs));
        assertFalse(Collision.planeHitsObstacle(93, 108, 24, 24, 20, -30, obs));
    }

    @Test
    public void testHitsMountains() {
        assertTrue(Collision.planeHitsObstacle(93, 60, 24, 24, 24, 0, obs), "Top mountain");
        assertTrue(Collision.planeHitsObstacle(93, 150, 24, 24, 24, 0, obs), "Bottom mountain");
    }

    @Test
    public void testPeakPokingIntoBoxBetweenCornersIsDetected() {
        // Bottom peak at (105, 160) pokes up into the box bottom edge; no box corner is inside the triangle
        assertTrue(Collision.planeHitsObstacle(93, 140, 24, 24, 24, 0, obs));
    }

    @Test
    public void testRotationMattersNearThePeak() {
        // Just above the bottom peak: level is clear, but a nose-down tilt drops the tail corner onto it
        double y = 160 - 12 - 10 - 1.5; // box bottom 1.5px above peak (hitbox 20 tall)
        GamePanel.Obstacle peakAtTail = new GamePanel.Obstacle(59, 120); // peak at x=84, under the tail end of the box
        assertFalse(Collision.planeHitsObstacle(80, y, 24, 24, 20, 0, peakAtTail), "Level should clear");
        assertTrue(Collision.planeHitsObstacle(80, y, 24, 24, 20, 20, peakAtTail), "Nose up lowers the tail onto the peak");
    }

    @Test
    public void testFarAwayObstacleNeverHits() {
        GamePanel.Obstacle far = new GamePanel.Obstacle(250, 60);
        for (int pitch = -30; pitch <= 30; pitch += 10) {
            assertFalse(Collision.planeHitsObstacle(80, 100, 24, 24, 20, pitch, far));
        }
    }
}
