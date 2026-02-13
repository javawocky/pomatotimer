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
        
        // Get raycast data (18 rays at 20 degree intervals)
        SpaceRaycast.RayResult[] rays = SpaceRaycast.castRays(this, meteors);
        
        double normalizedVx = getNormalizedVx();
        double normalizedVy = getNormalizedVy();
        
        // Normalize rotation angle to -1 to 1 (0 to 360 degrees)
        double normalizedAngle = (rotation % 360) / 180.0 - 1.0;
        
        // Angular velocity (how fast rotating) - normalize to reasonable range
        double normalizedAngularVel = angularVelocity / 5.0; // Assuming max ~5 degrees/frame
        
        // Neural network inputs: [18 raycasts, vx, vy, angle, angular velocity]
        double[] inputs = new double[22];
        
        // Raycasts (18 inputs)
        for (int i = 0; i < 18; i++) {
            inputs[i] = rays[i].distance; // Already normalized 0-1
        }
        
        // Velocity (2 inputs)
        inputs[18] = normalizedVx;
        inputs[19] = normalizedVy;
        
        // Rotation state (2 inputs)
        inputs[20] = normalizedAngle;
        inputs[21] = normalizedAngularVel;
        
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
