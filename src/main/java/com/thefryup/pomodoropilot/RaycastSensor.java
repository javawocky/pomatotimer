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
    
    /** Ray angles relative to the plane's nose, in screen degrees (negative = up). */
    static final double[] RAY_ANGLES = {0, -15, 15, -30, 30, -45, 45, 90, -90};
    private static final int[] RAY_LENGTHS = {RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH / 3, RAY_LENGTH / 3};
    /**
     * FLY mode fan: 9 full-length rays evenly spread from 85 degrees up to 85 degrees down.
     * Same index layout as RAY_ANGLES (centre, then pairs outward, then down/up).
     */
    static final double[] FLY_RAY_ANGLES = {0, -21.25, 21.25, -42.5, 42.5, -63.75, 63.75, 85, -85};
    private static final int[] FLY_RAY_LENGTHS = {RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH, RAY_LENGTH};
    static final int FLOOR_RAY = 7;   // belly sensor
    static final int CEILING_RAY = 8; // canopy sensor
    
    public static RayResult[] castRays(double planeX, double planeY, ArrayList<GamePanel.Obstacle> obstacles) {
        return castRays(planeX, planeY, 0.0, obstacles);
    }
    
    /**
     * Cast the sensor fan relative to the plane's pitch.
     * @param pitchDeg plane pitch in degrees, positive = nose up (rotates every ray upward)
     */
    public static RayResult[] castRays(double planeX, double planeY, double pitchDeg, ArrayList<GamePanel.Obstacle> obstacles) {
        return castRays(planeX, planeY, pitchDeg, obstacles, false);
    }
    
    /**
     * @param boundsOnAllRays when true every ray sees the floor and ceiling (needed in FLY mode,
     *                        where steep pitch swings the belly/canopy rays nearly horizontal)
     */
    public static RayResult[] castRays(double planeX, double planeY, double pitchDeg, ArrayList<GamePanel.Obstacle> obstacles,
                                       boolean boundsOnAllRays) {
        return castRays(planeX, planeY, pitchDeg, obstacles, boundsOnAllRays, RAY_ANGLES, RAY_LENGTHS);
    }
    
    /** FLY mode sensors: the wide +/-85 degree fan, every ray seeing floor and ceiling. */
    public static RayResult[] castFlyRays(double planeX, double planeY, double pitchDeg, ArrayList<GamePanel.Obstacle> obstacles) {
        return castRays(planeX, planeY, pitchDeg, obstacles, true, FLY_RAY_ANGLES, FLY_RAY_LENGTHS);
    }
    
    private static RayResult[] castRays(double planeX, double planeY, double pitchDeg, ArrayList<GamePanel.Obstacle> obstacles,
                                        boolean boundsOnAllRays, double[] angles, int[] rayLengths) {
        RayResult[] results = new RayResult[9];
        
        for (int i = 0; i < 9; i++) {
            // Screen y points down, so nose-up pitch subtracts from the screen angle
            double worldAngle = angles[i] - pitchDeg;
            results[i] = castRay(planeX, planeY, worldAngle, i, obstacles, rayLengths[i], boundsOnAllRays);
        }
        
        return results;
    }
    
    private static RayResult castRay(double startX, double startY, double angleDegrees, int rayIndex, ArrayList<GamePanel.Obstacle> obstacles, int maxLength,
                                     boolean boundsOnAllRays) {
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
            // All rays detect side boundaries.
            // Only the canopy ray detects the top boundary, only the belly ray the bottom,
            // unless boundsOnAllRays (FLY mode), where every ray sees floor and ceiling.
            // (Roles are by sensor, so they still work when the fan is rotated by pitch.)
            boolean hitBoundary = false;
            
            if (x < 0 || x >= 320) {
                hitBoundary = true; // Side boundaries
            } else if ((boundsOnAllRays || rayIndex == CEILING_RAY) && y < 0) {
                hitBoundary = true; // Top boundary
            } else if ((boundsOnAllRays || rayIndex == FLOOR_RAY) && y >= 240) {
                hitBoundary = true; // Bottom boundary
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
