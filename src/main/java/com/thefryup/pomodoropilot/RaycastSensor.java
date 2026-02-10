package com.thefryup.pomodoropilot;

import java.awt.*;
import java.util.ArrayList;

public class RaycastSensor {
    private static final int RAY_LENGTH = 150;
    private static final int PIPE_WIDTH = 50;
    private static final int PIPE_GAP = 80;
    
    public static class RayResult {
        public double distance; // 0.0 to 1.0 (normalized)
        public boolean hit;
        public double endX, endY;
        
        public RayResult(double distance, boolean hit, double endX, double endY) {
            this.distance = distance;
            this.hit = hit;
            this.endX = endX;
            this.endY = endY;
        }
    }
    
    public static RayResult[] castRays(double planeX, double planeY, ArrayList<GamePanel.Obstacle> obstacles) {
        RayResult[] results = new RayResult[7];
        
        // Ray angles in degrees
        double[] angles = {0, -25, 25, -45, 45, 90, -90};
        // Ray 5 (straight down) and Ray 6 (straight up) are shorter
        int[] rayLengths = {RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH / 3, RAY_LENGTH / 3};
        
        for (int i = 0; i < 7; i++) {
            results[i] = castRay(planeX, planeY, angles[i], obstacles, rayLengths[i]);
        }
        
        return results;
    }
    
    private static RayResult castRay(double startX, double startY, double angleDegrees, ArrayList<GamePanel.Obstacle> obstacles, int maxLength) {
        double angleRad = Math.toRadians(angleDegrees);
        double dx = Math.cos(angleRad);
        double dy = Math.sin(angleRad);
        
        double minDistance = maxLength;
        boolean hit = false;
        
        // Check collision along ray
        for (double dist = 0; dist <= maxLength; dist += 1.0) {
            double x = startX + dx * dist;
            double y = startY + dy * dist;
            
            // Check screen boundaries
            // Upward rays (negative angles) ignore top boundary, except straight up (-90)
            // Only downward ray (90 degrees) detects bottom boundary
            // All rays detect side boundaries
            boolean hitBoundary = false;
            
            if (x < 0 || x >= 320) {
                hitBoundary = true; // Side boundaries
            } else if (angleDegrees >= 0 && y < 0) {
                hitBoundary = true; // Top boundary (only for non-upward rays)
            } else if (angleDegrees == -90 && y < 0) {
                hitBoundary = true; // Top boundary (for straight up ray)
            } else if (angleDegrees == 90 && y >= 240) {
                hitBoundary = true; // Bottom boundary (only for downward ray)
            }
            
            if (hitBoundary) {
                minDistance = Math.min(minDistance, dist);
                hit = true;
                break;
            }
            
            // Check obstacles
            for (GamePanel.Obstacle obs : obstacles) {
                if (x >= obs.x && x <= obs.x + PIPE_WIDTH) {
                    int topPipeHeight = obs.gapY - PIPE_GAP/2;
                    int bottomPipeY = obs.gapY + PIPE_GAP/2;
                    
                    // Check top mountain (triangle pointing down)
                    if (topPipeHeight > 0 && pointInTriangle(x, y, 
                            obs.x + PIPE_WIDTH/2.0, topPipeHeight,
                            obs.x, 0,
                            obs.x + PIPE_WIDTH, 0)) {
                        minDistance = Math.min(minDistance, dist);
                        hit = true;
                        break;
                    }
                    
                    // Check bottom mountain (triangle pointing up)
                    if (bottomPipeY < 240 && pointInTriangle(x, y,
                            obs.x + PIPE_WIDTH/2.0, bottomPipeY,
                            obs.x, 240,
                            obs.x + PIPE_WIDTH, 240)) {
                        minDistance = Math.min(minDistance, dist);
                        hit = true;
                        break;
                    }
                }
            }
            
            if (hit) break;
        }
        
        double endX = startX + dx * minDistance;
        double endY = startY + dy * minDistance;
        double normalizedDistance = minDistance / maxLength;
        
        return new RayResult(normalizedDistance, hit, endX, endY);
    }
    
    private static boolean pointInTriangle(double px, double py, double x1, double y1, double x2, double y2, double x3, double y3) {
        double d1 = sign(px, py, x1, y1, x2, y2);
        double d2 = sign(px, py, x2, y2, x3, y3);
        double d3 = sign(px, py, x3, y3, x1, y1);
        
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        
        return !(hasNeg && hasPos);
    }
    
    private static double sign(double px, double py, double x1, double y1, double x2, double y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }
    
    public static void drawRays(Graphics2D g2d, double planeX, double planeY, RayResult[] rays) {
        for (int i = 0; i < rays.length; i++) {
            RayResult ray = rays[i];
            
            // Only draw rays that hit something
            if (ray.hit) {
                // Color based on distance: green (far) to red (close)
                // distance is 0.0 (close) to 1.0 (far)
                float distance = (float)ray.distance;
                int red = (int)(255 * (1.0f - distance));   // More red when close
                int green = (int)(255 * distance);          // More green when far
                
                g2d.setColor(new Color(red, green, 0, 180));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine((int)planeX, (int)planeY, (int)ray.endX, (int)ray.endY);
            }
        }
    }
}
