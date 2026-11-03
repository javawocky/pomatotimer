package com.thefryup.pomodoropilot;

public class AIPlane extends FlightBody {
    public int score;
    public int survivalTime;
    public boolean alive;
    public NeuralNetwork brain;
    public int colorIndex; // 0=Blue, 1=Red, 2=Green, 3=Yellow
    public int jumpCooldown;
    public double targetY = 120; // Target Y position (gap center)
    public double targetDistance = 0; // Distance to target
    public double target2Y = 120; // Second target Y position
    public double target2Distance = 0; // Distance to second target
    /** Last stick input in FLY mode (-1..1), kept for visualisation. */
    public double lastControl = 0;
    /**
     * Frames already "survived" to reach this run's start. In super training a run begins
     * partway along the course, so fitness is credited from the rewind point, not from zero.
     */
    public int startOffset = 0;
    
    public AIPlane(int colorIndex) {
        this(colorIndex, new NeuralNetwork());
    }
    
    public AIPlane(int colorIndex, NeuralNetwork brain) {
        resetFlight(100);
        this.score = 0;
        this.survivalTime = 0;
        this.alive = true;
        this.brain = brain;
        this.colorIndex = colorIndex;
        this.jumpCooldown = 0;
    }
    
    /** Bouncy-mode update, kept for backwards compatibility. */
    public void update(RaycastSensor.RayResult[] rays, int planeHeight, double targetY, double targetDistance, double target2Y, double target2Distance) {
        update(rays, planeHeight, targetY, targetDistance, target2Y, target2Distance, FlightMode.BOUNCY);
    }
    
    public void update(RaycastSensor.RayResult[] rays, int planeHeight, double targetY, double targetDistance,
                       double target2Y, double target2Distance, FlightMode mode) {
        if (!alive) return;
        
        this.targetY = targetY;
        this.targetDistance = targetDistance;
        this.target2Y = target2Y;
        this.target2Distance = target2Distance;
        
        survivalTime++;
        
        double output = brain.predict(buildInputs(rays, mode));
        
        if (mode == FlightMode.FLY) {
            // Sigmoid output 0..1 maps to a stick position -1..1 (0.5 = centred)
            lastControl = (output - 0.5) * 2.0;
            stepFly(lastControl);
        } else {
            // Jump if output > 0.5 and not too high (no cooldown!)
            stepBouncy(output > 0.5 && y > 15);
        }
        
        if (y + planeHeight > 240) {
            alive = false;
        }
    }
    
    double[] buildInputs(RaycastSensor.RayResult[] rays, FlightMode mode) {
        // 9 raycasts + 5 target inputs + 1 velocity + 2 attitude
        double[] inputs = new double[NeuralNetwork.INPUT_SIZE];
        for (int i = 0; i < 9; i++) {
            inputs[i] = rays[i].distance;
        }
        
        // Target height relative to plane (-1 to 1, negative = below target)
        double heightDiff = (targetY - y) / 120.0; // Normalize to screen height
        inputs[9] = clamp(heightDiff, -1.0, 1.0);
        
        // Distance to target (0 to 1, closer = higher value)
        inputs[10] = clamp(1.0 - (targetDistance / 320.0), 0.0, 1.0);
        
        // Second target height relative to plane
        double heightDiff2 = (target2Y - y) / 120.0;
        inputs[11] = clamp(heightDiff2, -1.0, 1.0);
        
        // Distance to second target
        inputs[12] = clamp(1.0 - (target2Distance / 320.0), 0.0, 1.0);
        
        // Slope between first and second target (-1 to 1)
        double slope = target2Distance > 0 ? (target2Y - targetY) / Math.max(1.0, target2Distance) : 0.0;
        inputs[13] = clamp(slope / 120.0, -1.0, 1.0); // Normalize by screen height
        
        // Y velocity normalized. Bouncy: max fall ~10. Fly: vertical speeds are ~3 px/frame.
        double velScale = mode == FlightMode.FLY ? 4.0 : 10.0;
        inputs[14] = clamp(velY / velScale, -1.0, 1.0);
        
        // Attitude - always 0 in bouncy mode
        inputs[15] = clamp(pitch / MAX_PITCH_DEG, -1.0, 1.0);
        inputs[16] = clamp(pitchRate / MAX_PITCH_RATE, -1.0, 1.0);
        return inputs;
    }
    
    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
    
    /** Bouncy-mode collision with an axis-aligned square hitbox. */
    public void checkCollision(GamePanel.Obstacle obs, int planeX, int planeWidth, int planeHeight) {
        checkCollision(obs, planeX, planeWidth, FlightMode.BOUNCY);
    }
    
    public void checkCollision(GamePanel.Obstacle obs, int planeX, int spriteSize, FlightMode mode) {
        if (!alive) return;
        double collisionPitch = mode == FlightMode.FLY ? pitch : 0.0;
        if (Collision.planeHitsObstacle(planeX, y, spriteSize, mode.hitboxWidth, mode.hitboxHeight, collisionPitch, obs)) {
            alive = false;
        }
    }
    
    /** Fitness credited per obstacle passed, so clearing a hard one is clearly rewarded. */
    public static final double OBSTACLE_BONUS = 25.0;
    /** Obstacles already cleared to reach this run's start (super training rewind point). */
    public int startObstacleOffset = 0;
    
    public int getObstaclesPassed() {
        return score / 10;
    }
    
    /** Total obstacles cleared on the course this run, counting the rewind head start. */
    public int getTotalObstaclesPassed() {
        return startObstacleOffset + getObstaclesPassed();
    }
    
    public double getFitness() {
        // Distance travelled from the course start (startOffset covers rewound super-training runs),
        // plus a per-obstacle bonus. Both count from the rewind point, so reaching and then clearing
        // the sticking point scores far higher than dying at the start of the segment - otherwise a
        // rewound run that starts near the hard obstacle would score almost nothing for passing it.
        return startOffset + survivalTime + OBSTACLE_BONUS * getTotalObstaclesPassed();
    }
}
