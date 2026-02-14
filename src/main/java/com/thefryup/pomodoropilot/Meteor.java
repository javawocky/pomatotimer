package com.thefryup.pomodoropilot;

import java.awt.image.BufferedImage;

public class Meteor {
    public double x, y;
    public double vx, vy;
    public int width, height;
    public BufferedImage image;
    public boolean scored; // Has this meteor been scored yet
    public int type; // 0-3 for different meteor types
    
    public Meteor(double x, double y, double vx, double vy, BufferedImage image, int type) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.image = image;
        this.width = image != null ? image.getWidth() : 20; // Default size for headless
        this.height = image != null ? image.getHeight() : 20;
        this.scored = false;
        this.type = type;
    }
    
    public void update() {
        x += vx;
        y += vy;
    }
    
    public boolean isOffScreen() {
        // Fully off bottom of screen
        return y > 240;
    }
    
    public double getCenterX() {
        return x + width / 2.0;
    }
    
    public double getCenterY() {
        return y + height / 2.0;
    }
}
