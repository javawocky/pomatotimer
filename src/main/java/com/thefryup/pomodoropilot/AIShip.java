package com.thefryup.pomodoropilot;

import java.util.List;

public class AIShip extends Ship {
    public SpaceNeuralNetwork brain;
    public double fitness;
    public int score;
    public int survivalFrames;
    
    public AIShip(double x, double y, int colorIndex) {
        super(x, y, colorIndex);
        this.brain = new SpaceNeuralNetwork();
        this.fitness = 0;
        this.score = 0;
        this.survivalFrames = 0;
    }
    
    public AIShip(double x, double y, int colorIndex, SpaceNeuralNetwork brain) {
        super(x, y, colorIndex);
        this.brain = brain;
        this.fitness = 0;
        this.score = 0;
        this.survivalFrames = 0;
    }
    
    /**
     * AI makes a decision based on sensor inputs
     */
    public void think(List<Meteor> meteors) {
        if (!alive) return;
        
        // Get raycast data (18 values)
        SpaceRaycast.RayResult[] rays = SpaceRaycast.castRays(this, meteors);
        
        // Build neural network inputs (22 total)
        double[] inputs = new double[22];
        
        // Raycasts (18 inputs)
        for (int i = 0; i < 18; i++) {
            inputs[i] = rays[i].distance; // Already normalized 0-1
        }
        
        // Velocity (2 inputs, normalized -1 to 1)
        inputs[18] = getNormalizedVx();
        inputs[19] = getNormalizedVy();
        
        // Heading (2 inputs, sin and cos for smooth rotation)
        inputs[20] = getHeadingSin();
        inputs[21] = getHeadingCos();
        
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
        // If both or neither pressed, rotationCommand stays 0
        
        // Update ship with AI commands
        update(rotationCommand, thrustPressed);
        
        // Track survival
        survivalFrames++;
    }
    
    /**
     * Calculate fitness based on survival time and score
     */
    public double getFitness() {
        // Fitness = survival time + score bonus
        fitness = survivalFrames + (score * 10);
        return fitness;
    }
    
    /**
     * Award points when meteor passes
     */
    public void addScore(int points) {
        score += points;
    }
}
