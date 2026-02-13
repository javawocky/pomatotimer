package com.thefryup.pomodoropilot;

public class Ship {
    public double x, y;           // Position
    public double vx, vy;         // Velocity
    public double rotation;       // Angle in degrees (0 = up, 90 = right, 180 = down, 270 = left)
    public double angularVelocity; // Rotation speed
    public boolean thrusting;     // Is thrust active
    public boolean alive;
    public int colorIndex;        // 0-3 for different ship colors
    
    private static final double ROTATION_SPEED = 4.0;      // Degrees per frame
    private static final double THRUST_POWER = 0.125;      // Acceleration when thrusting (50% of original)
    private static final double MAX_VELOCITY = 3.0;        // Maximum speed (50% of original)
    private static final double FRICTION = 0.99;           // Velocity decay (0.99 = slight friction)
    
    private int width = 24;  // Ship sprite width (same as plane game)
    private int height = 24; // Ship sprite height
    
    public Ship(double x, double y, int colorIndex) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.rotation = 0; // Start facing up
        this.angularVelocity = 0;
        this.thrusting = false;
        this.alive = true;
        this.colorIndex = colorIndex;
    }
    
    public void update(double rotationInput, boolean thrustInput) {
        if (!alive) return;
        
        // Apply rotation input (-1 = left, 0 = none, 1 = right)
        angularVelocity = rotationInput * ROTATION_SPEED;
        rotation += angularVelocity;
        
        // Normalize rotation to 0-360
        while (rotation < 0) rotation += 360;
        while (rotation >= 360) rotation -= 360;
        
        // Apply thrust if active
        thrusting = thrustInput;
        if (thrusting) {
            // Convert rotation to radians (0 degrees = up = -90 in standard coords)
            double angleRad = Math.toRadians(rotation - 90);
            vx += Math.cos(angleRad) * THRUST_POWER;
            vy += Math.sin(angleRad) * THRUST_POWER;
        }
        
        // Apply friction
        vx *= FRICTION;
        vy *= FRICTION;
        
        // Limit max velocity
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed > MAX_VELOCITY) {
            vx = (vx / speed) * MAX_VELOCITY;
            vy = (vy / speed) * MAX_VELOCITY;
        }
        
        // Update position
        x += vx;
        y += vy;
        
        // Screen boundaries - die if touching edge
        if (x <= 0 || x >= 320 - width || y <= 0 || y >= 240 - height) {
            alive = false;
        }
    }
    
    public double getCenterX() {
        return x + width / 2.0;
    }
    
    public double getCenterY() {
        return y + height / 2.0;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    // Get normalized velocity for neural network input (-1 to 1)
    public double getNormalizedVx() {
        return Math.max(-1.0, Math.min(1.0, vx / MAX_VELOCITY));
    }
    
    public double getNormalizedVy() {
        return Math.max(-1.0, Math.min(1.0, vy / MAX_VELOCITY));
    }
    
    // Get heading components for neural network input
    public double getHeadingSin() {
        return Math.sin(Math.toRadians(rotation));
    }
    
    public double getHeadingCos() {
        return Math.cos(Math.toRadians(rotation));
    }
}
