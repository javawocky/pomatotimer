package com.thefryup.pomodoropilot;

import java.util.List;

public class AIShip extends Ship {
    public RecurrentNeuralNetwork brain;
    public double fitness;
    public int score;
    public int survivalFrames;
    
    // Store closest obstacle for visualization
    public double closestObstacleX;
    public double closestObstacleY;
    public double closestObstacleDist;
    
    // Ray cache for lazy evaluation
    private SpaceRaycast.RayResult[] cachedRays = new SpaceRaycast.RayResult[18];
    private int[] rayFramesSinceUpdate = new int[18];
    private static final int INACTIVE_RAY_UPDATE_INTERVAL = 5;
    
    // Recurrent state (exposed for batched processing)
    public double[] recurrentState = new double[48];
    
    // Track actions for fitness penalties
    private double totalRotation;  // Accumulated absolute rotation
    private int thrustCount;       // Number of thrust activations
    
    public AIShip(double x, double y, int colorIndex) {
        super(x, y, colorIndex);
        this.brain = new RecurrentNeuralNetwork();
        this.fitness = 0;
        this.score = 0;
        this.survivalFrames = 0;
        this.totalRotation = 0;
        this.thrustCount = 0;
        initRayCache();
        resetRecurrentState();
    }
    
    public AIShip(double x, double y, int colorIndex, RecurrentNeuralNetwork brain) {
        super(x, y, colorIndex);
        this.brain = brain;
        this.fitness = 0;
        this.score = 0;
        this.survivalFrames = 0;
        this.totalRotation = 0;
        this.thrustCount = 0;
        initRayCache();
        resetRecurrentState();
    }
    
    public void resetRecurrentState() {
        for (int i = 0; i < recurrentState.length; i++) {
            recurrentState[i] = 0;
        }
    }
    
    private void initRayCache() {
        for (int i = 0; i < 18; i++) {
            cachedRays[i] = new SpaceRaycast.RayResult(0, 0, 0, false);
            rayFramesSinceUpdate[i] = 999; // Force initial calculation
        }
    }
    
    /**
     * Prepare inputs for neural network (for batched processing)
     */
    public double[] prepareInputs(List<Meteor> meteors) {
        if (!alive) return new double[23];
        
        // Lazy ray evaluation
        updateRaysLazy(meteors);
        
        // Normalize velocity
        double normalizedVx = Math.max(-1.0, Math.min(1.0, vx / 10.0));
        double normalizedVy = Math.max(-1.0, Math.min(1.0, vy / 10.0));
        
        // Decompose rotation angle
        double angleRad = Math.toRadians(rotation % 360);
        double sinAngle = Math.sin(angleRad);
        double cosAngle = Math.cos(angleRad);
        
        // Normalize angular velocity
        double normalizedAngularVel = Math.max(-1.0, Math.min(1.0, angularVelocity / 10.0));
        
        // Calculate screen edge distances (normalized to 0-1, where 1 = close to edge)
        double centerX = getCenterX();
        double centerY = getCenterY();
        double distToNorth = centerY / 120.0;           // Distance to top (y=0)
        double distToSouth = (240 - centerY) / 120.0;   // Distance to bottom (y=240)
        double distToWest = centerX / 160.0;            // Distance to left (x=0)
        double distToEast = (320 - centerX) / 160.0;    // Distance to right (x=320)
        
        // Clamp to [0, 1]
        distToNorth = Math.max(0, Math.min(1, distToNorth));
        distToSouth = Math.max(0, Math.min(1, distToSouth));
        distToWest = Math.max(0, Math.min(1, distToWest));
        distToEast = Math.max(0, Math.min(1, distToEast));
        
        // Build input array: 18 rays + 4 edges + 5 state
        double[] inputs = new double[27];
        for (int i = 0; i < 18; i++) {
            inputs[i] = cachedRays[i].distance;
        }
        inputs[18] = distToNorth;
        inputs[19] = distToSouth;
        inputs[20] = distToWest;
        inputs[21] = distToEast;
        inputs[22] = normalizedVx;
        inputs[23] = normalizedVy;
        inputs[24] = sinAngle;
        inputs[25] = cosAngle;
        inputs[26] = normalizedAngularVel;
        
        return inputs;
    }
    
    /**
     * Apply neural network outputs to ship controls
     */
    public void applyOutputs(double[] outputs) {
        if (!alive) return;
        
        // Output interpretation with thresholds
        boolean leftPressed = outputs[0] > 0.5;
        boolean rightPressed = outputs[1] > 0.5;
        boolean thrustPressed = outputs[2] > 0.65;
        
        // Mutual exclusion
        if (leftPressed && rightPressed) {
            if (outputs[0] > outputs[1]) {
                rightPressed = false;
            } else {
                leftPressed = false;
            }
        }
        
        // Calculate rotation command
        double rotationCommand = 0;
        if (leftPressed) rotationCommand = -1;
        if (rightPressed) rotationCommand = 1;
        
        // Track for penalties
        totalRotation += Math.abs(rotationCommand);
        if (thrustPressed) thrustCount++;
        
        // Update ship
        update(rotationCommand, thrustPressed);
        survivalFrames++;
    }
    
    /**
     * AI makes a decision based on sensor inputs
     */
    public void think(List<Meteor> meteors) {
        if (!alive) return;
        
        // Lazy ray evaluation: only recalculate rays that need updating
        updateRaysLazy(meteors);
        
        // Normalize velocity to [-1, 1] range (assuming max velocity ~10)
        double normalizedVx = Math.max(-1.0, Math.min(1.0, vx / 10.0));
        double normalizedVy = Math.max(-1.0, Math.min(1.0, vy / 10.0));
        
        // Decompose rotation angle into sin/cos for better learning
        double angleRad = Math.toRadians(rotation % 360);
        double sinAngle = Math.sin(angleRad);
        double cosAngle = Math.cos(angleRad);
        
        // Normalize angular velocity to [-1, 1] range (assuming max ~10 degrees/frame)
        double normalizedAngularVel = Math.max(-1.0, Math.min(1.0, angularVelocity / 10.0));
        
        // Neural network inputs: [18 raycasts, 4 edges, vx, vy, sin, cos, angular_vel]
        double[] inputs = new double[27];
        
        // Raycasts (18 inputs) - use cached values
        for (int i = 0; i < 18; i++) {
            inputs[i] = cachedRays[i].distance; // Already normalized 0-1
        }
        
        // Screen edge distances (4 inputs)
        double centerX = getCenterX();
        double centerY = getCenterY();
        inputs[18] = Math.max(0, Math.min(1, centerY / 120.0));           // North
        inputs[19] = Math.max(0, Math.min(1, (240 - centerY) / 120.0));   // South
        inputs[20] = Math.max(0, Math.min(1, centerX / 160.0));           // West
        inputs[21] = Math.max(0, Math.min(1, (320 - centerX) / 160.0));   // East
        
        // Velocity (2 inputs)
        inputs[22] = normalizedVx;
        inputs[23] = normalizedVy;
        
        // Rotation state (3 inputs: sin, cos, angular velocity)
        inputs[24] = sinAngle;
        inputs[25] = cosAngle;
        inputs[26] = normalizedAngularVel;
        
        // Get neural network output
        double[] outputs = brain.predict(inputs);
        
        // Output interpretation with thresholds
        // Output 0: Left rotation (threshold 0.5)
        // Output 1: Right rotation (threshold 0.5)
        // Output 2: Thrust (threshold 0.65 - higher to reduce overuse)
        boolean leftPressed = outputs[0] > 0.5;
        boolean rightPressed = outputs[1] > 0.5;
        boolean thrustPressed = outputs[2] > 0.65;
        
        // Mutual exclusion: if both rotate flags true, disable the weaker one
        if (leftPressed && rightPressed) {
            if (outputs[0] > outputs[1]) {
                rightPressed = false;
            } else {
                leftPressed = false;
            }
        }
        
        // Calculate rotation command
        double rotationCommand = 0;
        if (leftPressed) rotationCommand = -1;
        if (rightPressed) rotationCommand = 1;
        
        // Track rotation for penalty
        totalRotation += Math.abs(rotationCommand);
        
        // Track thrust for penalty
        if (thrustPressed) thrustCount++;
        
        // Update ship with AI commands
        update(rotationCommand, thrustPressed);
        
        // Track survival
        survivalFrames++;
    }
    
    /**
     * Calculate fitness: heavily reward survival and meteors, penalize excessive actions
     */
    public double getFitness() {
        // Heavily reward survival time and meteors survived
        double survivalReward = survivalFrames * 2.0;  // 2 points per frame survived
        double meteorReward = score * 20.0;            // 20 points per meteor (score is meteors * 10)
        
        // Penalize excessive rotation and thrust
        double rotationPenalty = totalRotation * 0.5;  // 0.5 penalty per rotation command
        double thrustPenalty = thrustCount * 0.3;      // 0.3 penalty per thrust activation
        
        fitness = survivalReward + meteorReward - rotationPenalty - thrustPenalty;
        return Math.max(0, fitness); // Ensure non-negative
    }
    
    /**
     * Award points when meteor passes
     */
    public void addScore(int points) {
        score += points;
    }
    
    /**
     * Lazy ray evaluation with spatial grid
     */
    private void updateRaysLazy(List<Meteor> meteors) {
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        for (int i = 0; i < 18; i++) {
            rayFramesSinceUpdate[i]++;
            
            boolean isActive = cachedRays[i].distance > 0.7;
            boolean needsUpdate = isActive || rayFramesSinceUpdate[i] >= INACTIVE_RAY_UPDATE_INTERVAL;
            
            if (needsUpdate) {
                double angle = rotation - 90 + (i * 20);
                cachedRays[i] = SpaceRaycast.castSingleRay(centerX, centerY, angle, meteors, 120);
                rayFramesSinceUpdate[i] = 0;
            }
        }
    }
    
    /**
     * Lazy ray evaluation with spatial grid (optimized)
     */
    private void updateRaysLazyWithGrid(SpatialGrid grid) {
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        for (int i = 0; i < 18; i++) {
            rayFramesSinceUpdate[i]++;
            
            boolean isActive = cachedRays[i].distance > 0.7;
            boolean needsUpdate = isActive || rayFramesSinceUpdate[i] >= INACTIVE_RAY_UPDATE_INTERVAL;
            
            if (needsUpdate) {
                double angle = rotation - 90 + (i * 20);
                cachedRays[i] = SpaceRaycast.castSingleRay(centerX, centerY, angle, grid, 120);
                rayFramesSinceUpdate[i] = 0;
            }
        }
    }
    
    /**
     * Prepare inputs with spatial grid optimization
     */
    public double[] prepareInputsWithGrid(SpatialGrid grid) {
        if (!alive) return new double[27];
        
        updateRaysLazyWithGrid(grid);
        
        double normalizedVx = Math.max(-1.0, Math.min(1.0, vx / 10.0));
        double normalizedVy = Math.max(-1.0, Math.min(1.0, vy / 10.0));
        
        double angleRad = Math.toRadians(rotation % 360);
        double sinAngle = Math.sin(angleRad);
        double cosAngle = Math.cos(angleRad);
        
        double normalizedAngularVel = Math.max(-1.0, Math.min(1.0, angularVelocity / 10.0));
        
        // Calculate screen edge distances
        double centerX = getCenterX();
        double centerY = getCenterY();
        double distToNorth = Math.max(0, Math.min(1, centerY / 120.0));
        double distToSouth = Math.max(0, Math.min(1, (240 - centerY) / 120.0));
        double distToWest = Math.max(0, Math.min(1, centerX / 160.0));
        double distToEast = Math.max(0, Math.min(1, (320 - centerX) / 160.0));
        
        double[] inputs = new double[27];
        for (int i = 0; i < 18; i++) {
            inputs[i] = cachedRays[i].distance;
        }
        inputs[18] = distToNorth;
        inputs[19] = distToSouth;
        inputs[20] = distToWest;
        inputs[21] = distToEast;
        inputs[22] = normalizedVx;
        inputs[23] = normalizedVy;
        inputs[24] = sinAngle;
        inputs[25] = cosAngle;
        inputs[26] = normalizedAngularVel;
        
        return inputs;
    }
    
    /**
     * Think with spatial grid optimization
     */
    public void thinkWithGrid(SpatialGrid grid) {
        if (!alive) return;
        
        double[] inputs = prepareInputsWithGrid(grid);
        double[] outputs = brain.predict(inputs);
        applyOutputs(outputs);
    }
}
