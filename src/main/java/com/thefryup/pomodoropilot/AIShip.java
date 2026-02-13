package com.thefryup.pomodoropilot;

import java.util.List;

public class AIShip extends Ship {
    public SpaceNeuralNetwork brain;
    public double fitness;
    public int score;
    public int survivalFrames;
    
    // Store closest obstacle for visualization
    public double closestObstacleX;
    public double closestObstacleY;
    public double closestObstacleDist;
    
    // Track movement for fitness bonus
    private double totalVelocity;
    
    public AIShip(double x, double y, int colorIndex) {
        super(x, y, colorIndex);
        this.brain = new SpaceNeuralNetwork();
        this.fitness = 0;
        this.score = 0;
        this.survivalFrames = 0;
        this.totalVelocity = 0;
    }
    
    public AIShip(double x, double y, int colorIndex, SpaceNeuralNetwork brain) {
        super(x, y, colorIndex);
        this.brain = brain;
        this.fitness = 0;
        this.score = 0;
        this.survivalFrames = 0;
        this.totalVelocity = 0;
    }
    
    /**
     * AI makes a decision based on sensor inputs
     */
    public void think(List<Meteor> meteors) {
        if (!alive) return;
        
        final double MAX_DETECTION_DISTANCE = 220; // Detection range in pixels
        
        // Get raycast data (18 rays at 20 degree intervals)
        SpaceRaycast.RayResult[] rays = SpaceRaycast.castRays(this, meteors);
        
        // Find closest obstacle (meteor or screen edge)
        double closestDist = Double.MAX_VALUE;
        double closestX = 0;
        double closestY = 0;
        
        // Check meteors
        for (Meteor meteor : meteors) {
            double dx = meteor.getCenterX() - getCenterX();
            double dy = meteor.getCenterY() - getCenterY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist < closestDist) {
                closestDist = dist;
                closestX = meteor.getCenterX();
                closestY = meteor.getCenterY();
            }
        }
        
        // Check screen edges
        double[] edges = {
            x,                              // left edge
            320 - x - getWidth(),           // right edge
            y,                              // top edge
            240 - y - getHeight()           // bottom edge
        };
        double[] edgeX = {0, 320, getCenterX(), getCenterX()};
        double[] edgeY = {getCenterY(), getCenterY(), 0, 240};
        
        for (int i = 0; i < 4; i++) {
            if (edges[i] < closestDist) {
                closestDist = edges[i];
                closestX = edgeX[i];
                closestY = edgeY[i];
            }
        }
        
        // Store for visualization
        this.closestObstacleX = closestX;
        this.closestObstacleY = closestY;
        this.closestObstacleDist = closestDist;
        
        // If beyond max detection distance, treat as no obstacle
        double normalizedDist;
        double normalizedX;
        double normalizedY;
        double normalizedAngle;
        
        if (closestDist > MAX_DETECTION_DISTANCE) {
            // No obstacle detected - all obstacle inputs are 0
            normalizedDist = 0;
            normalizedX = 0;
            normalizedY = 0;
            normalizedAngle = 0;
        } else {
            // Calculate relative position and angle to closest obstacle
            double dx = closestX - getCenterX();
            double dy = closestY - getCenterY();
            double angleToObstacle = Math.atan2(dy, dx);
            
            // Normalize inputs
            normalizedDist = 1.0 - (closestDist / MAX_DETECTION_DISTANCE); // 1 = close, 0 = far
            normalizedX = dx / 160.0; // -1 to 1
            normalizedY = dy / 120.0; // -1 to 1
            normalizedAngle = angleToObstacle / Math.PI; // -1 to 1
        }
        
        double normalizedVx = getNormalizedVx();
        double normalizedVy = getNormalizedVy();
        
        // Neural network inputs: [18 raycasts, obstacleX, obstacleY, distance, angle, vx, vy]
        double[] inputs = new double[24];
        
        // Raycasts (18 inputs)
        for (int i = 0; i < 18; i++) {
            inputs[i] = rays[i].distance; // Already normalized 0-1
        }
        
        // Closest obstacle (4 inputs)
        inputs[18] = normalizedX;
        inputs[19] = normalizedY;
        inputs[20] = normalizedDist;
        inputs[21] = normalizedAngle;
        
        // Velocity (2 inputs)
        inputs[22] = normalizedVx;
        inputs[23] = normalizedVy;
        
        // Get neural network output
        double[] outputs = brain.predict(inputs);
        
        // Output 0: Left rotation (0-1, threshold at 0.5)
        // Output 1: Right rotation (0-1, threshold at 0.5)
        // Output 2: Thrust (0-1, threshold at 0.5)
        boolean leftPressed = outputs[0] > 0.5;
        boolean rightPressed = outputs[1] > 0.5;
        boolean thrustPressed = outputs[2] > 0.5;
        
        // Calculate rotation command
        double rotationCommand = 0;
        if (leftPressed && !rightPressed) rotationCommand = -1;
        if (rightPressed && !leftPressed) rotationCommand = 1;
        
        // Update ship with AI commands
        update(rotationCommand, thrustPressed);
        
        // Track survival and movement
        survivalFrames++;
        
        // Accumulate velocity (linear movement, not rotation)
        double speed = Math.sqrt(vx * vx + vy * vy);
        totalVelocity += speed;
    }
    
    /**
     * Calculate fitness based on survival time, score, and movement
     */
    public double getFitness() {
        // Fitness = survival time + score bonus + movement bonus
        // Movement bonus: average velocity per frame (encourages moving, not just spinning)
        double avgVelocity = survivalFrames > 0 ? totalVelocity / survivalFrames : 0;
        fitness = survivalFrames + (score * 10) + (avgVelocity * 5);
        return fitness;
    }
    
    /**
     * Award points when meteor passes
     */
    public void addScore(int points) {
        score += points;
    }
}
