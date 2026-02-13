package com.thefryup.pomodoropilot;

import java.awt.*;
import java.util.List;

public class SpaceRaycast {
    
    public static class RayResult {
        public double distance; // Normalized 0-1 (0 = far, 1 = close)
        public double endX, endY;
        public boolean hit;
        
        public RayResult(double distance, double endX, double endY, boolean hit) {
            this.distance = distance;
            this.endX = endX;
            this.endY = endY;
            this.hit = hit;
        }
    }
    
    /**
     * Cast 18 rays (every 20 degrees) from ship center to detect meteors
     * Rays are relative to ship's rotation (0° = ship's forward direction)
     */
    public static RayResult[] castRays(Ship ship, List<Meteor> meteors) {
        RayResult[] results = new RayResult[18];
        double centerX = ship.getCenterX();
        double centerY = ship.getCenterY();
        int maxRayLength = 120; // Maximum ray distance (60% of 200)
        
        for (int i = 0; i < 18; i++) {
            // Ray angle relative to ship's rotation
            // Ship rotation: 0 = up, so subtract 90 to make 0 = forward
            double angle = ship.rotation - 90 + (i * 20); // Rotate with ship
            results[i] = castRay(centerX, centerY, angle, meteors, maxRayLength);
        }
        
        return results;
    }
    
    /**
     * Cast a single ray and find nearest meteor or screen edge
     */
    private static RayResult castRay(double startX, double startY, double angleDegrees, 
                                     List<Meteor> meteors, int maxLength) {
        double angleRad = Math.toRadians(angleDegrees);
        double dx = Math.cos(angleRad);
        double dy = Math.sin(angleRad);
        
        double closestDist = maxLength;
        boolean hit = false;
        
        // Check intersection with screen edges (320x240)
        double edgeDist = rayScreenEdgeIntersection(startX, startY, dx, dy);
        if (edgeDist >= 0 && edgeDist < closestDist) {
            closestDist = edgeDist;
            hit = true;
        }
        
        // Check intersection with each meteor
        for (Meteor meteor : meteors) {
            // Simple circle approximation for meteor
            double meteorCenterX = meteor.getCenterX();
            double meteorCenterY = meteor.getCenterY();
            double meteorRadius = Math.max(meteor.width, meteor.height) / 2.0;
            
            // Ray-circle intersection
            double dist = rayCircleIntersection(startX, startY, dx, dy, 
                                               meteorCenterX, meteorCenterY, meteorRadius);
            
            if (dist >= 0 && dist < closestDist) {
                closestDist = dist;
                hit = true;
            }
        }
        
        // Calculate end point
        double endX = startX + dx * closestDist;
        double endY = startY + dy * closestDist;
        
        // Normalize distance (0 = far, 1 = close)
        double normalizedDist = hit ? (1.0 - (closestDist / maxLength)) : 0.0;
        
        return new RayResult(normalizedDist, endX, endY, hit);
    }
    
    /**
     * Calculate ray intersection with screen edges (0,0 to 320,240)
     * Returns distance to nearest edge, or -1 if ray doesn't hit edges within reasonable distance
     */
    private static double rayScreenEdgeIntersection(double rayX, double rayY, double rayDx, double rayDy) {
        double minDist = Double.MAX_VALUE;
        boolean foundHit = false;
        
        // Left edge (x = 0)
        if (rayDx < 0) {
            double t = -rayX / rayDx;
            if (t > 0) {
                double hitY = rayY + rayDy * t;
                if (hitY >= 0 && hitY <= 240) {
                    minDist = Math.min(minDist, t);
                    foundHit = true;
                }
            }
        }
        
        // Right edge (x = 320)
        if (rayDx > 0) {
            double t = (320 - rayX) / rayDx;
            if (t > 0) {
                double hitY = rayY + rayDy * t;
                if (hitY >= 0 && hitY <= 240) {
                    minDist = Math.min(minDist, t);
                    foundHit = true;
                }
            }
        }
        
        // Top edge (y = 0)
        if (rayDy < 0) {
            double t = -rayY / rayDy;
            if (t > 0) {
                double hitX = rayX + rayDx * t;
                if (hitX >= 0 && hitX <= 320) {
                    minDist = Math.min(minDist, t);
                    foundHit = true;
                }
            }
        }
        
        // Bottom edge (y = 240)
        if (rayDy > 0) {
            double t = (240 - rayY) / rayDy;
            if (t > 0) {
                double hitX = rayX + rayDx * t;
                if (hitX >= 0 && hitX <= 320) {
                    minDist = Math.min(minDist, t);
                    foundHit = true;
                }
            }
        }
        
        return foundHit ? minDist : -1;
    }
    
    /**
     * Calculate ray-circle intersection distance
     * Returns -1 if no intersection, otherwise distance to intersection
     */
    private static double rayCircleIntersection(double rayX, double rayY, double rayDx, double rayDy,
                                                double circleX, double circleY, double radius) {
        // Vector from ray origin to circle center
        double fx = circleX - rayX;
        double fy = circleY - rayY;
        
        // Project circle center onto ray
        double projection = fx * rayDx + fy * rayDy;
        
        // If projection is negative, circle is behind ray
        if (projection < 0) return -1;
        
        // Find closest point on ray to circle center
        double closestX = rayX + rayDx * projection;
        double closestY = rayY + rayDy * projection;
        
        // Distance from closest point to circle center
        double distToCenter = Math.sqrt(
            (closestX - circleX) * (closestX - circleX) + 
            (closestY - circleY) * (closestY - circleY)
        );
        
        // Check if ray intersects circle
        if (distToCenter <= radius) {
            // Calculate distance to intersection point
            double distAlongRay = Math.sqrt(radius * radius - distToCenter * distToCenter);
            return projection - distAlongRay;
        }
        
        return -1;
    }
    
    /**
     * Draw raycasts for visualization
     */
    public static void drawRays(Graphics2D g2d, Ship ship, RayResult[] rays) {
        double centerX = ship.getCenterX();
        double centerY = ship.getCenterY();
        
        for (int i = 0; i < rays.length; i++) {
            RayResult ray = rays[i];
            
            // Only draw rays that hit something
            if (ray.hit) {
                // Color based on distance (green = far, red = close)
                float colorValue = (float)(1.0 - ray.distance);
                Color rayColor = new Color(1.0f - colorValue, colorValue, 0, 0.6f);
                
                g2d.setColor(rayColor);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine((int)centerX, (int)centerY, (int)ray.endX, (int)ray.endY);
                
                // Draw small circle at hit point
                g2d.fillOval((int)ray.endX - 2, (int)ray.endY - 2, 4, 4);
            }
        }
    }
}
