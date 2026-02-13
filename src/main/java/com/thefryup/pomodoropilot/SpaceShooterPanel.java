package com.thefryup.pomodoropilot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceShooterPanel extends JPanel {
    private BufferedImage[] shipImages; // 4 colors
    private BufferedImage[] engineImages; // 5 engine sprites
    private BufferedImage[] meteorImages; // Various meteor types
    private BufferedImage background;
    
    private AIShip testShip; // AI-controlled ship for testing
    private SpaceEvolutionManager evolutionManager; // Manages AI population
    private List<Meteor> meteors;
    private Random rand;
    
    private int frameCounter = 0;
    private int spawnTimer = 0;
    private double backgroundScrollY = 0;
    private int gameOverTimer = 0; // Delay before restart
    private int score = 0;
    private int highScore = 0;
    
    // Test controls
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean thrustPressed = false;
    private boolean showRaycasts = true; // Toggle with R key
    private boolean turboMode = false; // Toggle with T key
    private boolean fastForwardMode = false;
    private int fastForwardTarget = 0;
    private boolean rightArrowReleased = true;
    
    public SpaceShooterPanel() {
        setPreferredSize(new Dimension(320, 240));
        setBackground(Color.BLACK);
        
        rand = new Random();
        meteors = new ArrayList<>();
        
        loadImages();
        
        // Create evolution manager with 10 AI ships
        double startX = 160 - 12;
        double startY = (240 * 2 / 3) - 12;
        evolutionManager = new SpaceEvolutionManager(startX, startY);
        testShip = evolutionManager.getPopulation().get(0); // For compatibility
        
        // Keyboard controls for testing
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                    leftPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                    rightPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    thrustPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_F) {
                    toggleFullscreen();
                }
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    showRaycasts = !showRaycasts;
                    System.out.println("Raycasts: " + (showRaycasts ? "ON" : "OFF"));
                }
                if (e.getKeyCode() == KeyEvent.VK_T) {
                    turboMode = !turboMode;
                    System.out.println("Turbo Mode: " + (turboMode ? "ON" : "OFF"));
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT && rightArrowReleased) {
                    if (!fastForwardMode) {
                        fastForwardMode = true;
                        fastForwardTarget = evolutionManager.getGeneration() + 50;
                        rightArrowReleased = false;
                        System.out.println("Fast forwarding 50 generations to gen " + fastForwardTarget);
                    }
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (!fastForwardMode) {
                        rightArrowReleased = true;
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                    leftPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                    rightPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    thrustPressed = false;
                }
            }
        });
        
        setFocusable(true);
        
        // Game loop
        new Thread(() -> {
            while (true) {
                // In fast forward mode, run updates without rendering
                if (fastForwardMode) {
                    update();
                    if (evolutionManager.getGeneration() >= fastForwardTarget) {
                        fastForwardMode = false;
                        rightArrowReleased = true;
                        System.out.println("Fast forward complete at gen " + evolutionManager.getGeneration());
                        // Force one repaint after fast forward
                        SwingUtilities.invokeLater(() -> {
                            repaint();
                            Toolkit.getDefaultToolkit().sync();
                        });
                    }
                    // No rendering during fast forward, just yield
                    Thread.yield();
                } else {
                    SwingUtilities.invokeLater(() -> {
                        update();
                        repaint();
                        Toolkit.getDefaultToolkit().sync();
                    });
                    
                    try {
                        if (!turboMode) {
                            Thread.sleep(16); // ~60 FPS (16ms)
                        } else {
                            Thread.sleep(1); // ~1000 FPS in turbo (1ms)
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
    }
    
    private void loadImages() {
        try {
            // Load ship images and scale to 24x24
            shipImages = new BufferedImage[4];
            shipImages[0] = scaleImage(ImageIO.read(getClass().getResourceAsStream("/spaceshooter/playerShip1_blue.png")), 24, 24);
            shipImages[1] = scaleImage(ImageIO.read(getClass().getResourceAsStream("/spaceshooter/playerShip1_green.png")), 24, 24);
            shipImages[2] = scaleImage(ImageIO.read(getClass().getResourceAsStream("/spaceshooter/playerShip1_orange.png")), 24, 24);
            shipImages[3] = scaleImage(ImageIO.read(getClass().getResourceAsStream("/spaceshooter/playerShip1_red.png")), 24, 24);
            
            // Load engine images and scale proportionally
            engineImages = new BufferedImage[5];
            for (int i = 0; i < 5; i++) {
                BufferedImage orig = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/engine" + (i + 1) + ".png"));
                engineImages[i] = scaleImage(orig, 12, 12);
            }
            
            // Load meteor images and scale to 1/3 size
            meteorImages = new BufferedImage[4];
            BufferedImage m1 = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/meteorBrown_big1.png"));
            BufferedImage m2 = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/meteorBrown_med1.png"));
            BufferedImage m3 = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/meteorGrey_big1.png"));
            BufferedImage m4 = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/meteorGrey_med1.png"));
            meteorImages[0] = scaleImage(m1, m1.getWidth() / 3, m1.getHeight() / 3);
            meteorImages[1] = scaleImage(m2, m2.getWidth() / 3, m2.getHeight() / 3);
            meteorImages[2] = scaleImage(m3, m3.getWidth() / 3, m3.getHeight() / 3);
            meteorImages[3] = scaleImage(m4, m4.getWidth() / 3, m4.getHeight() / 3);
            
            // Load background and scale to 1/3
            BufferedImage origBg = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/black.png"));
            background = scaleImage(origBg, origBg.getWidth() / 3, origBg.getHeight() / 3);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }
    
    private void update() {
        frameCounter++;
        spawnTimer++;
        
        // Handle game over state (all ships dead)
        if (evolutionManager.allDead()) {
            gameOverTimer++;
            if (gameOverTimer > 90) { // 1.5 seconds at 60 FPS
                restartGame();
            }
            return; // Don't update anything else when all dead
        }
        
        // All AI ships think and act
        for (AIShip ship : evolutionManager.getPopulation()) {
            if (ship.alive) {
                ship.think(meteors);
            }
        }
        
        // Spawn meteors with increasing difficulty (1.1x faster ramp)
        // Calculate seconds elapsed
        int secondsElapsed = frameCounter / 60;
        
        // Spawn interval decreases over time (1.1x faster)
        // Reaches minimum (30 frames) at ~164 seconds instead of 180
        int spawnInterval = Math.max(30, 120 - (int)(secondsElapsed * 0.55));
        
        if (spawnTimer >= spawnInterval) {
            spawnMeteor(secondsElapsed);
            spawnTimer = 0;
        }
        
        // Update meteors and check for scoring
        for (Meteor meteor : meteors) {
            meteor.update();
            
            // Award points when meteor exits bottom (to all alive ships)
            if (!meteor.scored && meteor.y > 240) {
                meteor.scored = true;
                for (AIShip ship : evolutionManager.getPopulation()) {
                    if (ship.alive) {
                        ship.addScore(10);
                    }
                }
            }
        }
        
        // Check collisions for all ships
        double hitboxShrink = 0.1;
        
        for (AIShip ship : evolutionManager.getPopulation()) {
            if (!ship.alive) continue;
            
            double shipHitX = ship.x + ship.getWidth() * hitboxShrink;
            double shipHitY = ship.y + ship.getHeight() * hitboxShrink;
            double shipHitW = ship.getWidth() * 0.8;
            double shipHitH = ship.getHeight() * 0.8;
            
            for (Meteor meteor : meteors) {
                if (shipHitX < meteor.x + meteor.width &&
                    shipHitX + shipHitW > meteor.x &&
                    shipHitY < meteor.y + meteor.height &&
                    shipHitY + shipHitH > meteor.y) {
                    
                    ship.alive = false;
                    break;
                }
            }
        }
        
        // Remove off-screen meteors
        meteors.removeIf(Meteor::isOffScreen);
        
        // Scroll background slowly
        backgroundScrollY += 0.2;
        if (backgroundScrollY >= background.getHeight()) {
            backgroundScrollY = 0;
        }
    }
    
    private void restartGame() {
        System.out.println("All ships dead. Evolving generation " + evolutionManager.getGeneration() + "...");
        
        // Clear meteors
        meteors.clear();
        
        // Evolve next generation
        double startX = 160 - 12;
        double startY = (240 * 2 / 3) - 12;
        evolutionManager.evolveNextGeneration(startX, startY);
        testShip = evolutionManager.getPopulation().get(0);
        
        // Reset timers
        frameCounter = 0;
        spawnTimer = 0;
        gameOverTimer = 0;
        backgroundScrollY = 0;
        score = 0;
    }
    
    private void spawnMeteor(int secondsElapsed) {
        int type = rand.nextInt(meteorImages.length);
        BufferedImage img = meteorImages[type];
        double x = rand.nextDouble() * (320 - img.getWidth());
        double y = -img.getHeight();
        
        // Horizontal drift stays constant
        double vx = (rand.nextDouble() - 0.5) * 0.3;
        
        // Downward speed increases over time (1.1x faster)
        // Reaches max speed at ~164 seconds instead of 180
        double baseSpeed = 0.5 + (secondsElapsed / 164.0) * 1.0;
        double speedVariation = 0.3 + (secondsElapsed / 164.0) * 0.5;
        double vy = baseSpeed + rand.nextDouble() * speedVariation;
        
        meteors.add(new Meteor(x, y, vx, vy, img, type));
    }
    
    private void toggleFullscreen() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        if (device.getFullScreenWindow() == frame) {
            device.setFullScreenWindow(null);
            frame.dispose();
            frame.setUndecorated(false);
            frame.setVisible(true);
        } else {
            frame.dispose();
            frame.setUndecorated(true);
            frame.setVisible(true);
            device.setFullScreenWindow(frame);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Calculate scaling to fit window while maintaining aspect ratio
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double scaleX = panelWidth / 320.0;
        double scaleY = panelHeight / 240.0;
        double scale = Math.min(scaleX, scaleY);
        
        int scaledWidth = (int)(320 * scale);
        int scaledHeight = (int)(240 * scale);
        int offsetX = (panelWidth - scaledWidth) / 2;
        int offsetY = (panelHeight - scaledHeight) / 2;
        
        // Fill background with black
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, panelWidth, panelHeight);
        
        // Translate and scale to game coordinates
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        // Draw scrolling background (tiled)
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        
        // Tile horizontally and vertically
        for (int x = 0; x < 320; x += bgWidth) {
            for (int y = (int)backgroundScrollY - bgHeight; y < 240; y += bgHeight) {
                g2d.drawImage(background, x, y, null);
            }
        }
        
        // Draw meteors
        for (Meteor meteor : meteors) {
            g2d.drawImage(meteor.image, (int)meteor.x, (int)meteor.y, null);
        }
        
        // Draw raycasts for all ships if enabled
        if (showRaycasts) {
            for (AIShip ship : evolutionManager.getPopulation()) {
                if (ship.alive) {
                    SpaceRaycast.RayResult[] rays = SpaceRaycast.castRays(ship, meteors);
                    SpaceRaycast.drawRays(g2d, ship, rays);
                }
            }
        }
        
        // Draw all alive ships
        for (AIShip ship : evolutionManager.getPopulation()) {
            if (ship.alive) {
                drawRotatedShip(g2d, ship);
            }
        }
        
        // Draw debug info
        g2d.setColor(Color.WHITE);
        g2d.drawString("Gen: " + evolutionManager.getGeneration(), 10, 20);
        g2d.drawString("Alive: " + evolutionManager.getAliveCount(), 10, 35);
        g2d.drawString("Best Score: " + evolutionManager.getBestScoreThisGen(), 10, 50);
        g2d.drawString("High Score: " + evolutionManager.getBestScoreEver(), 10, 65);
        g2d.drawString("Time: " + (frameCounter / 60) + "s", 10, 80);
        g2d.drawString("Meteors: " + meteors.size(), 10, 95);
        
        // Draw fitness graph (bottom-left)
        drawFitnessGraph(g2d);
        
        // Draw neural network visualization (bottom-right)
        drawNeuralNetwork(g2d);
    }
    
    private void drawRotatedShip(Graphics2D g2d, Ship ship) {
        BufferedImage shipImg = shipImages[ship.colorIndex];
        
        // Save transform
        var oldTransform = g2d.getTransform();
        
        // Rotate around ship center
        g2d.rotate(Math.toRadians(ship.rotation), ship.getCenterX(), ship.getCenterY());
        
        // Draw ship
        g2d.drawImage(shipImg, (int)ship.x, (int)ship.y, null);
        
        // Draw thruster if active
        if (ship.thrusting) {
            BufferedImage engineImg = engineImages[(frameCounter / 3) % engineImages.length];
            int engineX = (int)(ship.getCenterX() - engineImg.getWidth() / 2.0);
            int engineY = (int)(ship.y + ship.getHeight());
            g2d.drawImage(engineImg, engineX, engineY, null);
        }
        
        // Restore transform
        g2d.setTransform(oldTransform);
    }
    
    private void drawFitnessGraph(Graphics2D g2d) {
        List<Double> history = evolutionManager.getFitnessHistory();
        if (history.isEmpty()) return;
        
        int graphX = 10;
        int graphY = 177;
        int graphWidth = 105;
        int graphHeight = 53;
        
        // Semi-transparent background
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRect(graphX, graphY, graphWidth, graphHeight);
        
        // Border
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawRect(graphX, graphY, graphWidth, graphHeight);
        
        // Find max fitness for scaling
        double maxFitness = history.stream().mapToDouble(d -> d).max().orElse(1.0);
        if (maxFitness < 10) maxFitness = 10;
        
        // Draw line graph
        g2d.setColor(new Color(0, 255, 0, 180));
        g2d.setStroke(new BasicStroke(1));
        
        for (int i = 1; i < history.size(); i++) {
            double fitness1 = history.get(i - 1);
            double fitness2 = history.get(i);
            
            int x1 = graphX + (int)((i - 1) * graphWidth / (double)Math.max(1, history.size() - 1));
            int y1 = graphY + graphHeight - (int)(fitness1 / maxFitness * graphHeight);
            
            int x2 = graphX + (int)(i * graphWidth / (double)Math.max(1, history.size() - 1));
            int y2 = graphY + graphHeight - (int)(fitness2 / maxFitness * graphHeight);
            
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw 5-generation moving average in red
        if (history.size() >= 5) {
            g2d.setColor(new Color(255, 0, 0, 180));
            g2d.setStroke(new BasicStroke(2));
            
            for (int i = 4; i < history.size(); i++) {
                double avg1 = (history.get(i - 4) + history.get(i - 3) + history.get(i - 2) + history.get(i - 1) + history.get(i)) / 5.0;
                
                int x1 = graphX + (int)(i * graphWidth / (double)Math.max(1, history.size() - 1));
                int y1 = graphY + graphHeight - (int)(avg1 / maxFitness * graphHeight);
                
                if (i + 1 < history.size()) {
                    double avg2 = (history.get(i - 3) + history.get(i - 2) + history.get(i - 1) + history.get(i) + history.get(i + 1)) / 5.0;
                    int x2 = graphX + (int)((i + 1) * graphWidth / (double)Math.max(1, history.size() - 1));
                    int y2 = graphY + graphHeight - (int)(avg2 / maxFitness * graphHeight);
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }
    
    private void drawNeuralNetwork(Graphics2D g2d) {
        AIShip best = evolutionManager.getBestShip();
        if (best == null || best.brain.getLastInputs() == null) return;
        
        int netX = 205;
        int netY = 177;
        int netWidth = 105;
        int netHeight = 53;
        
        // Background
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRect(netX, netY, netWidth, netHeight);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawRect(netX, netY, netWidth, netHeight);
        
        double[] inputs = best.brain.getLastInputs();
        double[] hidden = best.brain.getLastHidden();
        double[] outputs = best.brain.getLastOutputs();
        
        if (outputs == null || outputs.length < 3) return;
        
        int nodeSize = 3;
        int[] layerX = {netX + 10, netX + 50, netX + 90};
        
        g2d.setStroke(new BasicStroke(1));
        
        // Draw connections (simplified - just show strong ones)
        final float threshold = 0.05f;
        
        // Input to Hidden (first 8 raycasts)
        for (int i = 0; i < Math.min(8, inputs.length); i++) {
            int y1 = netY + 5 + i * 5;
            double input = inputs[i];
            for (int j = 0; j < Math.min(8, hidden.length); j++) {
                float activation = (float)Math.abs(input * hidden[j]);
                if (activation > threshold) {
                    int y2 = netY + 5 + j * 5;
                    g2d.setColor(new Color(activation, activation * 0.5f, 0, 0.3f));
                    g2d.drawLine(layerX[0], y1, layerX[1], y2);
                }
            }
        }
        
        // Hidden to Outputs (3 outputs: left, right, thrust)
        for (int out = 0; out < 3; out++) {
            int y2 = netY + 15 + out * 10;
            for (int i = 0; i < Math.min(8, hidden.length); i++) {
                float activation = (float)Math.abs(hidden[i]);
                if (activation > threshold) {
                    int y1 = netY + 5 + i * 5;
                    g2d.setColor(new Color(activation, activation * 0.5f, 0, 0.3f));
                    g2d.drawLine(layerX[1], y1, layerX[2], y2);
                }
            }
        }
        
        // Draw nodes - Input layer (first 8 raycasts)
        for (int i = 0; i < Math.min(8, inputs.length); i++) {
            int y = netY + 5 + i * 5;
            float val = (float)inputs[i];
            g2d.setColor(new Color(1 - val, val, 0));
            g2d.fillOval(layerX[0] - 1, y - 1, nodeSize, nodeSize);
        }
        
        // Hidden layer (first 8)
        for (int i = 0; i < Math.min(8, hidden.length); i++) {
            int y = netY + 5 + i * 5;
            float val = (float)((hidden[i] + 1) * 0.5);
            g2d.setColor(new Color(1 - val, val, 0));
            g2d.fillOval(layerX[1] - 1, y - 1, nodeSize, nodeSize);
        }
        
        // Outputs (left, right, thrust)
        String[] labels = {"L", "R", "T"};
        for (int i = 0; i < 3; i++) {
            int y = netY + 15 + i * 10;
            float val = (float)outputs[i];
            g2d.setColor(new Color(1 - val, val, 0));
            g2d.fillOval(layerX[2] - 1, y - 1, nodeSize, nodeSize);
            
            // Draw green ring if activated (> 0.5)
            if (outputs[i] > 0.5) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(0.8f));
                g2d.drawOval(layerX[2] - 2, y - 2, nodeSize + 2, nodeSize + 2);
            }
            
            // Label
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 6));
            g2d.drawString(labels[i], layerX[2] + 5, y + 2);
        }
    }
}
