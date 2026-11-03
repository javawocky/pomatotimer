package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

public class RotateSpriteTest {

    private BufferedImage opaqueSquare(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) img.setRGB(x, y, 0xFFFF0000);
        return img;
    }

    @Test
    public void testRotatedCanvasIsPaddedSymmetricallyAndHoldsTheWholeSprite() {
        BufferedImage src = opaqueSquare(24);
        for (int deg = -85; deg <= 85; deg += 5) {
            BufferedImage out = GamePanel.rotateSprite(src, deg);
            assertEquals(out.getWidth(), out.getHeight());
            assertEquals(0, (out.getWidth() - 24) % 2, "Padding must be symmetric to keep the sprite centred");
            int opaque = 0;
            for (int y = 0; y < out.getHeight(); y++) for (int x = 0; x < out.getWidth(); x++) {
                if ((out.getRGB(x, y) >>> 24) != 0) opaque++;
            }
            assertEquals(24 * 24, opaque, 24 * 4, "Rotation should not clip the sprite at " + deg + " degrees");
        }
    }

    @Test
    public void testAreaOutsideTheRotatedSpriteIsTransparent() {
        // The artifact fix relies on the canvas edges being fully transparent
        BufferedImage out = GamePanel.rotateSprite(opaqueSquare(24), 45);
        int last = out.getWidth() - 1;
        assertEquals(0, out.getRGB(0, 0) >>> 24);
        assertEquals(0, out.getRGB(last, 0) >>> 24);
        assertEquals(0, out.getRGB(0, last) >>> 24);
        assertEquals(0, out.getRGB(last, last) >>> 24);
        int c = out.getWidth() / 2;
        assertNotEquals(0, out.getRGB(c, c) >>> 24, "Centre should still be the sprite");
    }

    @Test
    public void testNoseUpRotatesCounterClockwiseOnScreen() {
        // Mark the nose (right edge); nose-up should move it toward the top of the canvas
        BufferedImage src = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        for (int y = 10; y < 14; y++) for (int x = 20; x < 24; x++) src.setRGB(x, y, 0xFF00FF00);
        BufferedImage out = GamePanel.rotateSprite(src, 45);
        int sumY = 0, n = 0;
        for (int y = 0; y < out.getHeight(); y++) for (int x = 0; x < out.getWidth(); x++) {
            if ((out.getRGB(x, y) >>> 24) != 0) { sumY += y; n++; }
        }
        assertTrue(n > 0);
        assertTrue(sumY / (double) n < out.getHeight() / 2.0, "Nose-up should lift the nose above centre");
    }
}
