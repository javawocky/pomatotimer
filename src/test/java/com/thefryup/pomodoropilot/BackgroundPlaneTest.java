package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BackgroundPlaneTest {
    
    @Test
    public void testBackgroundPlaneMovement() {
        GamePanel panel = new GamePanel();
        panel.startBreak();
        
        System.out.println("Initial state: isWorking=" + panel.isWorking() + ", isLanding=" + panel.isLanding());
        
        // Check initial spawn - should have 5 planes
        assertEquals(5, panel.getBackgroundPlaneCount(), "Should spawn 5 planes initially");
        
        // Get first plane
        GamePanel.BackgroundPlane plane = panel.getBackgroundPlane(0);
        double initialX = plane.x;
        
        // Check it spawned off-screen
        assertTrue(initialX < -25 || initialX > 320, "Plane should spawn off-screen, was at: " + initialX);
        
        // Advance 100 frames
        for (int i = 0; i < 100; i++) {
            panel.updateForTest();
            if (i == 60 || i == 61) {
                System.out.println("Frame " + i + ": isWorking=" + panel.isWorking() + ", isLanding=" + panel.isLanding() + ", planes=" + panel.getBackgroundPlaneCount());
            }
        }
        
        // Check plane moved
        plane = panel.getBackgroundPlane(0);
        if (plane != null) {
            assertNotEquals(initialX, plane.x, "Plane should have moved");
            System.out.println("After 100 frames, plane at x=" + plane.x);
        } else {
            System.out.println("Plane was removed after 100 frames");
        }
        
        // Advance 500 more frames to ensure continuous spawning
        int planeCountBefore = panel.getBackgroundPlaneCount();
        for (int i = 0; i < 500; i++) {
            panel.updateForTest();
            if (i % 100 == 0) {
                System.out.println("Frame " + i + ": " + panel.getBackgroundPlaneCount() + " planes, isWorking=" + panel.isWorking() + ", isLanding=" + panel.isLanding());
            }
        }
        
        // Should still have planes
        assertTrue(panel.getBackgroundPlaneCount() > 0, "Should still have planes after 600 frames");
    }
    
    @Test
    public void testPlaneHeightSeparation() {
        GamePanel panel = new GamePanel();
        
        // Spawn planes multiple times and check separation
        for (int test = 0; test < 10; test++) {
            panel.startBreak();
            
            System.out.println("\nTest " + test + " - Plane positions:");
            for (int i = 0; i < panel.getBackgroundPlaneCount(); i++) {
                GamePanel.BackgroundPlane p = panel.getBackgroundPlane(i);
                System.out.println("  Plane " + i + ": y=" + p.y);
            }
            
            // Check all pairs for collision
            int planeHeight = 24;
            for (int i = 0; i < panel.getBackgroundPlaneCount(); i++) {
                GamePanel.BackgroundPlane p1 = panel.getBackgroundPlane(i);
                for (int j = i + 1; j < panel.getBackgroundPlaneCount(); j++) {
                    GamePanel.BackgroundPlane p2 = panel.getBackgroundPlane(j);
                    double distance = Math.abs(p1.y - p2.y);
                    assertTrue(distance >= planeHeight, 
                        "Planes " + i + " and " + j + " too close: y1=" + p1.y + ", y2=" + p2.y + ", distance=" + distance);
                }
            }
        }
    }
}
