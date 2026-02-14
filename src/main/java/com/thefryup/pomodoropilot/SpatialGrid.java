package com.thefryup.pomodoropilot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spatial hash grid for fast meteor proximity queries
 * Divides 320x240 space into cells for O(1) neighbor lookups
 */
public class SpatialGrid {
    private static final int CELL_SIZE = 60; // Each cell is 60x60 pixels
    private static final int GRID_WIDTH = (320 / CELL_SIZE) + 1;  // 6 cells
    private static final int GRID_HEIGHT = (240 / CELL_SIZE) + 1; // 5 cells
    
    private Map<Integer, List<Meteor>> grid;
    
    public SpatialGrid() {
        grid = new HashMap<>();
    }
    
    /**
     * Clear and rebuild grid with current meteors
     */
    public void update(List<Meteor> meteors) {
        grid.clear();
        for (Meteor meteor : meteors) {
            int cellX = (int)(meteor.getCenterX() / CELL_SIZE);
            int cellY = (int)(meteor.getCenterY() / CELL_SIZE);
            int key = cellX + cellY * GRID_WIDTH;
            
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(meteor);
        }
    }
    
    /**
     * Get meteors near a point (returns meteors in same cell + 8 neighbors)
     */
    public List<Meteor> getNearby(double x, double y, double radius) {
        List<Meteor> nearby = new ArrayList<>();
        
        int cellX = (int)(x / CELL_SIZE);
        int cellY = (int)(y / CELL_SIZE);
        
        // Check 3x3 grid around point (includes diagonals)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int checkX = cellX + dx;
                int checkY = cellY + dy;
                
                if (checkX >= 0 && checkX < GRID_WIDTH && checkY >= 0 && checkY < GRID_HEIGHT) {
                    int key = checkX + checkY * GRID_WIDTH;
                    List<Meteor> cell = grid.get(key);
                    if (cell != null) {
                        nearby.addAll(cell);
                    }
                }
            }
        }
        
        return nearby;
    }
    
    /**
     * Get meteors along a ray path (checks all cells the ray passes through)
     */
    public List<Meteor> getAlongRay(double startX, double startY, double endX, double endY) {
        List<Meteor> result = new ArrayList<>();
        
        // Get all cells along the ray using DDA-like algorithm
        int x0 = (int)(startX / CELL_SIZE);
        int y0 = (int)(startY / CELL_SIZE);
        int x1 = (int)(endX / CELL_SIZE);
        int y1 = (int)(endY / CELL_SIZE);
        
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        
        int x = x0;
        int y = y0;
        
        while (true) {
            // Add meteors from current cell
            if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                int key = x + y * GRID_WIDTH;
                List<Meteor> cell = grid.get(key);
                if (cell != null) {
                    for (Meteor m : cell) {
                        if (!result.contains(m)) {
                            result.add(m);
                        }
                    }
                }
            }
            
            if (x == x1 && y == y1) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
        
        return result;
    }
}
