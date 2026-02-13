package com.thefryup.pomodoropilot;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class CollisionDetector {
    // Cache alpha masks for meteor images
    private static Map<BufferedImage, boolean[][]> alphaMaskCache = new HashMap<>();
    
    /**
     * Check if ship collides with meteor using pixel-perfect collision
     */
    public static boolean checkCollision(Ship ship, Meteor meteor, BufferedImage shipImage) {
        // Broad phase: bounding box check
        if (!boundingBoxOverlap(ship.x, ship.y, ship.getWidth(), ship.getHeight(),
                                meteor.x, meteor.y, meteor.width, meteor.height)) {
            return false;
        }
        
        // Narrow phase: pixel-perfect collision using alpha channel
        boolean[][] meteorMask = getAlphaMask(meteor.image);
        boolean[][] shipMask = getAlphaMask(shipImage);
        
        // Check overlapping pixels
        int shipLeft = (int)ship.x;
        int shipTop = (int)ship.y;
        int meteorLeft = (int)meteor.x;
        int meteorTop = (int)meteor.y;
        
        // Calculate overlap region
        int overlapLeft = Math.max(shipLeft, meteorLeft);
        int overlapTop = Math.max(shipTop, meteorTop);
        int overlapRight = Math.min(shipLeft + ship.getWidth(), meteorLeft + meteor.width);
        int overlapBottom = Math.min(shipTop + ship.getHeight(), meteorTop + meteor.height);
        
        // Check each pixel in overlap region
        for (int x = overlapLeft; x < overlapRight; x++) {
            for (int y = overlapTop; y < overlapBottom; y++) {
                int shipX = x - shipLeft;
                int shipY = y - shipTop;
                int meteorX = x - meteorLeft;
                int meteorY = y - meteorTop;
                
                // Check if both have solid pixels at this position
                if (shipX >= 0 && shipX < ship.getWidth() && shipY >= 0 && shipY < ship.getHeight() &&
                    meteorX >= 0 && meteorX < meteor.width && meteorY >= 0 && meteorY < meteor.height) {
                    
                    if (shipMask[shipX][shipY] && meteorMask[meteorX][meteorY]) {
                        return true; // Collision detected
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Simple bounding box overlap check
     */
    private static boolean boundingBoxOverlap(double x1, double y1, int w1, int h1,
                                              double x2, double y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 &&
               y1 < y2 + h2 && y1 + h1 > y2;
    }
    
    /**
     * Get or create alpha mask for an image (true = solid pixel, false = transparent)
     */
    private static boolean[][] getAlphaMask(BufferedImage image) {
        // Check cache first
        if (alphaMaskCache.containsKey(image)) {
            return alphaMaskCache.get(image);
        }
        
        // Create alpha mask
        int width = image.getWidth();
        int height = image.getHeight();
        boolean[][] mask = new boolean[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                mask[x][y] = alpha > 128; // Consider pixel solid if alpha > 50%
            }
        }
        
        // Cache it
        alphaMaskCache.put(image, mask);
        
        return mask;
    }
    
    /**
     * Clear the alpha mask cache (call when loading new images)
     */
    public static void clearCache() {
        alphaMaskCache.clear();
    }
}
