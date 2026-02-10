package com.thefryup.pomodoropilot;

public class AIPlane {
    public double y;
    public double velY;
    public int score;
    public int survivalTime;
    public boolean alive;
    public NeuralNetwork brain;
    public int colorIndex; // 0=Blue, 1=Red, 2=Green, 3=Yellow
    public int jumpCooldown;
    public double targetY = 120; // Target Y position (gap center)
    public double targetDistance = 0; // Distance to target
    
    private static final double GRAVITY = 0.5;
    private static final double JUMP_STRENGTH = -4.5;
    private static final int JUMP_COOLDOWN_FRAMES = 12;
    
    public AIPlane(int colorIndex) {
        this.y = 100;
        this.velY = 0;
        this.score = 0;
        this.survivalTime = 0;
        this.alive = true;
        this.brain = new NeuralNetwork();
        this.colorIndex = colorIndex;
        this.jumpCooldown = 0;
    }
    
    public AIPlane(int colorIndex, NeuralNetwork brain) {
        this.y = 100;
        this.velY = 0;
        this.score = 0;
        this.survivalTime = 0;
        this.alive = true;
        this.brain = brain;
        this.colorIndex = colorIndex;
        this.jumpCooldown = 0;
    }
    
    public void update(RaycastSensor.RayResult[] rays, int planeHeight, double targetY, double targetDistance) {
        if (!alive) return;
        
        this.targetY = targetY;
        this.targetDistance = targetDistance;
        
        survivalTime++;
        
        // Prepare inputs for neural network (7 raycasts + 2 target inputs)
        double[] inputs = new double[9];
        for (int i = 0; i < 7; i++) {
            inputs[i] = rays[i].distance;
        }
        
        // Target height relative to plane (-1 to 1, negative = below target)
        double heightDiff = (targetY - y) / 120.0; // Normalize to screen height
        inputs[7] = Math.max(-1.0, Math.min(1.0, heightDiff));
        
        // Distance to target (0 to 1, closer = higher value)
        inputs[8] = Math.max(0.0, Math.min(1.0, 1.0 - (targetDistance / 320.0)));
        
        // Get decision from neural network
        double output = brain.predict(inputs);
        
        // Jump if output > 0.5 and not too high (no cooldown!)
        if (output > 0.5 && y > 15) {
            velY = JUMP_STRENGTH;
        }
        
        // Physics
        velY += GRAVITY;
        y += velY;
        
        // Check boundaries
        if (y < 10) {
            y = 10;
            velY = 0;
        }
        
        if (y + planeHeight > 240) {
            alive = false;
        }
    }
    
    public void checkCollision(GamePanel.Obstacle obs, int planeX, int planeWidth, int planeHeight) {
        if (!alive) return;
        
        int PIPE_WIDTH = 50;
        int PIPE_GAP = 80;
        
        if (planeX + planeWidth > obs.x && planeX < obs.x + PIPE_WIDTH) {
            int topPipeHeight = obs.gapY - PIPE_GAP/2;
            int bottomPipeY = obs.gapY + PIPE_GAP/2;
            
            // Top mountain collision
            if (topPipeHeight > 0 && checkTriangleCollision(
                    planeX, y, planeWidth, planeHeight,
                    obs.x + PIPE_WIDTH/2.0, topPipeHeight,
                    obs.x, 0,
                    obs.x + PIPE_WIDTH, 0)) {
                alive = false;
            }
            
            // Bottom mountain collision
            if (bottomPipeY < 240 && checkTriangleCollision(
                    planeX, y, planeWidth, planeHeight,
                    obs.x + PIPE_WIDTH/2.0, bottomPipeY,
                    obs.x, 240,
                    obs.x + PIPE_WIDTH, 240)) {
                alive = false;
            }
        }
    }
    
    private boolean checkTriangleCollision(double rectX, double rectY, double rectW, double rectH,
                                          double x1, double y1, double x2, double y2, double x3, double y3) {
        double[][] corners = {
            {rectX, rectY},
            {rectX + rectW, rectY},
            {rectX, rectY + rectH},
            {rectX + rectW, rectY + rectH}
        };
        
        for (double[] corner : corners) {
            if (pointInTriangle(corner[0], corner[1], x1, y1, x2, y2, x3, y3)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean pointInTriangle(double px, double py, double x1, double y1, double x2, double y2, double x3, double y3) {
        double d1 = sign(px, py, x1, y1, x2, y2);
        double d2 = sign(px, py, x2, y2, x3, y3);
        double d3 = sign(px, py, x3, y3, x1, y1);
        
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        
        return !(hasNeg && hasPos);
    }
    
    private double sign(double px, double py, double x1, double y1, double x2, double y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }
    
    public double getFitness() {
        // Simple fitness based on survival time (distance traveled)
        return survivalTime;
    }
}
