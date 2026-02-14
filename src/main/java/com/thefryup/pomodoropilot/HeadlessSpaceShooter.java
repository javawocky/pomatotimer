package com.thefryup.pomodoropilot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeadlessSpaceShooter {
    private SpaceEvolutionManager evolutionManager;
    private List<Meteor> meteors;
    private SpatialGrid spatialGrid;
    private Random rand;
    private int frameCounter;
    private int spawnTimer;
    private int maxGenerations;
    
    public HeadlessSpaceShooter(int maxGenerations) {
        this.maxGenerations = maxGenerations;
        this.rand = new Random();
        this.meteors = new ArrayList<>();
        this.spatialGrid = new SpatialGrid();
        
        double startX = 160 - 12;
        double startY = (240 * 2 / 3) - 12;
        this.evolutionManager = new SpaceEvolutionManager(startX, startY);
        
        this.frameCounter = 0;
        this.spawnTimer = 0;
    }
    
    public void run() {
        System.out.println("Starting headless training...");
        long startTime = System.currentTimeMillis();
        
        while (evolutionManager.getGeneration() < maxGenerations) {
            update();
            
            // Print stats every 100 generations
            if (evolutionManager.allDead() && evolutionManager.getGeneration() % 100 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                double gensPerSec = evolutionManager.getGeneration() / (elapsed / 1000.0);
                System.out.printf("Gen %d/%d | Best Fitness: %.0f | Best Score: %d | %.2f gen/sec%n",
                    evolutionManager.getGeneration(),
                    maxGenerations,
                    evolutionManager.getBestFitnessEver(),
                    evolutionManager.getBestScoreEver(),
                    gensPerSec);
            }
        }
        
        // Final results
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("\n=== TRAINING COMPLETE ===");
        System.out.println("Total generations: " + evolutionManager.getGeneration());
        System.out.println("Best fitness ever: " + evolutionManager.getBestFitnessEver());
        System.out.println("Best score ever: " + evolutionManager.getBestScoreEver());
        System.out.println("Total time: " + (totalTime / 1000.0) + " seconds");
        System.out.println("Average: " + (evolutionManager.getGeneration() / (totalTime / 1000.0)) + " gen/sec");
        
        System.exit(0);
    }
    
    private void update() {
        frameCounter++;
        spawnTimer++;
        
        // Handle game over state (all ships dead)
        if (evolutionManager.allDead()) {
            restartGame();
            return;
        }
        
        // Update spatial grid with current meteors
        spatialGrid.update(meteors);
        
        // All AI ships think and act in parallel with spatial optimization
        evolutionManager.getPopulation().parallelStream()
            .filter(ship -> ship.alive)
            .forEach(ship -> ship.thinkWithGrid(spatialGrid));
        
        // Spawn meteors
        int secondsElapsed = frameCounter / 60;
        int spawnInterval = Math.max(30, 55 - (int)(secondsElapsed * 0.55));
        
        if (spawnTimer >= spawnInterval) {
            spawnMeteor(secondsElapsed);
            spawnTimer = 0;
        }
        
        // Update meteors and check for scoring (any edge)
        meteors.parallelStream().forEach(meteor -> {
            meteor.update();
            
            if (!meteor.scored && (meteor.y > 240 || meteor.y < -meteor.height || 
                                   meteor.x < -meteor.width || meteor.x > 320)) {
                meteor.scored = true;
                for (AIShip ship : evolutionManager.getPopulation()) {
                    if (ship.alive) {
                        ship.addScore(10);
                    }
                }
            }
        });
        
        // Check collisions - parallel
        evolutionManager.getPopulation().parallelStream()
            .filter(ship -> ship.alive)
            .forEach(ship -> {
                for (Meteor meteor : meteors) {
                    if (checkCollision(ship, meteor)) {
                        ship.alive = false;
                        break;
                    }
                }
            });
        
        // Remove off-screen meteors
        meteors.removeIf(Meteor::isOffScreen);
    }
    
    private void spawnMeteor(int secondsElapsed) {
        // Simple meteor spawning - just use type 0 for headless
        int side = rand.nextInt(4);
        double x, y, vx, vy;
        double size = 20; // Approximate meteor size
        
        switch (side) {
            case 0: // Top
                x = rand.nextDouble() * (320 - size);
                y = -size;
                vx = (rand.nextDouble() - 0.5) * 0.3;
                vy = 0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5);
                break;
            case 1: // Bottom
                x = rand.nextDouble() * (320 - size);
                y = 240;
                vx = (rand.nextDouble() - 0.5) * 0.3;
                vy = -(0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5));
                break;
            case 2: // Left
                x = -size;
                y = rand.nextDouble() * (240 - size);
                vx = 0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5);
                vy = (rand.nextDouble() - 0.5) * 0.3;
                break;
            default: // Right
                x = 320;
                y = rand.nextDouble() * (240 - size);
                vx = -(0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5));
                vy = (rand.nextDouble() - 0.5) * 0.3;
                break;
        }
        
        meteors.add(new Meteor(x, y, vx, vy, null, 0)); // null image for headless
    }
    
    private boolean checkCollision(AIShip ship, Meteor meteor) {
        // Simple bounding box collision
        double shipSize = 24;
        double meteorSize = 20;
        
        double dx = ship.getCenterX() - meteor.getCenterX();
        double dy = ship.getCenterY() - meteor.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        return distance < (shipSize + meteorSize) / 2.5; // Slightly generous hitbox
    }
    
    private void restartGame() {
        double startX = 160 - 12;
        double startY = (240 * 2 / 3) - 12;
        
        evolutionManager.evolveNextGeneration(startX, startY);
        meteors.clear();
        frameCounter = 0;
        spawnTimer = 0;
    }
}
