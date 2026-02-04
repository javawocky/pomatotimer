package org.example;

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
}
