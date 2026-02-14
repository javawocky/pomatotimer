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
    
    // UI text rendering
    private BufferedImage[] letterImages = new BufferedImage[26];
    private BufferedImage[] numberImages = new BufferedImage[10];
    
    private AIShip testShip; // AI-controlled ship for testing
    private SpaceEvolutionManager evolutionManager; // Manages AI population
    private List<Meteor> meteors;
    private SpatialGrid spatialGrid;
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
    private boolean showRaycasts = false; // Toggle with R key (default OFF)
    private boolean turboMode = false; // Toggle with T key
    private boolean fastForwardMode = false;
    private int fastForwardTarget = 0;
    private boolean rightArrowReleased = true;
    
    // Player mode
    private boolean playerMode = false;
    private Ship playerShip;
    
    // Visualization caching
    private BufferedImage cachedFitnessGraph = null;
    private BufferedImage cachedNeuralNetwork = null;
    private int visualizationUpdateCounter = 0;
    private static final int VISUALIZATION_UPDATE_INTERVAL = 60; // Update once per second at 60fps
    
    public SpaceShooterPanel() {
        setPreferredSize(new Dimension(320, 240));
        setBackground(Color.BLACK);
        
        rand = new Random();
        meteors = new ArrayList<>();
        spatialGrid = new SpatialGrid();
        
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
                    // Force visualization refresh when exiting turbo mode
                    if (!turboMode) {
                        visualizationUpdateCounter = VISUALIZATION_UPDATE_INTERVAL;
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE && !playerMode) {
                    // Enter player mode
                    playerMode = true;
                    playerShip = new Ship(160, 160, 0);
                    rightArrowReleased = false; // Prevent fast forward from activating immediately
                    // Kill all AI ships so they don't interfere
                    for (AIShip ship : evolutionManager.getPopulation()) {
                        ship.alive = false;
                    }
                    System.out.println("Player mode activated!");
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT && rightArrowReleased && !playerMode) {
                    if (!fastForwardMode) {
                        fastForwardMode = true;
                        fastForwardTarget = evolutionManager.getGeneration() + 1000;
                        rightArrowReleased = false;
                        System.out.println("Fast forwarding 1000 generations to gen " + fastForwardTarget);
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
                        
                        // Immediately regenerate visualizations
                        if (cachedFitnessGraph == null) {
                            cachedFitnessGraph = new BufferedImage(230, 126, BufferedImage.TYPE_INT_ARGB);
                        }
                        Graphics2D gFit = cachedFitnessGraph.createGraphics();
                        gFit.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        gFit.setComposite(AlphaComposite.Clear);
                        gFit.fillRect(0, 0, 230, 126);
                        gFit.setComposite(AlphaComposite.SrcOver);
                        gFit.scale(2.0, 2.0);
                        drawFitnessGraphToImage(gFit);
                        gFit.dispose();
                        
                        if (cachedNeuralNetwork == null) {
                            cachedNeuralNetwork = new BufferedImage(230, 126, BufferedImage.TYPE_INT_ARGB);
                        }
                        Graphics2D gNet = cachedNeuralNetwork.createGraphics();
                        gNet.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        gNet.setComposite(AlphaComposite.Clear);
                        gNet.fillRect(0, 0, 230, 126);
                        gNet.setComposite(AlphaComposite.SrcOver);
                        gNet.scale(2.0, 2.0);
                        drawNeuralNetworkToImage(gNet);
                        gNet.dispose();
                        
                        visualizationUpdateCounter = 0;
                        
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
            
            // Load letters (A-Z) from SpaceShooter pack and scale to height 13.5 (75% of 18)
            for (int i = 0; i < 26; i++) {
                char letter = (char)('A' + i);
                BufferedImage let = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/letter" + letter + ".png"));
                int scaledWidth = (int)(let.getWidth() * (13.5 / let.getHeight()));
                letterImages[i] = scaleImage(let, scaledWidth, 13);
            }
            
            // Load numbers (0-9) from SpaceShooter pack and scale to height 13.5 (75% of 18)
            for (int i = 0; i < 10; i++) {
                BufferedImage num = ImageIO.read(getClass().getResourceAsStream("/spaceshooter/numeral" + i + ".png"));
                int scaledWidth = (int)(num.getWidth() * (13.5 / num.getHeight()));
                numberImages[i] = scaleImage(num, scaledWidth, 13);
            }
            
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
        
        if (playerMode) {
            // Player mode - control single ship
            // Reset game over timer since we're in player mode
            gameOverTimer = 0;
            
            double rotationInput = 0;
            if (leftPressed) rotationInput = -1;
            if (rightPressed) rotationInput = 1;
            
            boolean wasAlive = playerShip.alive;
            playerShip.update(rotationInput, thrustPressed);
            
            // Check if died from update (edges or rotation)
            if (wasAlive && !playerShip.alive) {
                System.out.println("Player died during update! Position: (" + playerShip.x + ", " + playerShip.y + ") Rotation accumulator: " + playerShip.rotationAccumulator);
            }
            
            // Check collision with meteors
            for (Meteor meteor : meteors) {
                if (playerShip.alive && checkCollision(playerShip, meteor)) {
                    playerShip.alive = false;
                    System.out.println("Player died! Hit meteor at (" + meteor.x + ", " + meteor.y + ")");
                    break;
                }
            }
            
            // If player died, return to AI mode
            if (!playerShip.alive) {
                playerMode = false;
                playerShip = null;
                rightArrowReleased = false; // Prevent fast forward from activating immediately
                // Restart the game to get fresh AI ships
                restartGame();
                return; // Exit update early
            }
        } else {
            // AI mode
            // Handle game over state (all ships dead)
            if (evolutionManager.allDead()) {
                // In turbo mode, restart immediately. Otherwise wait 1.5 seconds
                if (turboMode) {
                    restartGame();
                } else {
                    gameOverTimer++;
                    if (gameOverTimer > 90) { // 1.5 seconds at 60 FPS
                        restartGame();
                    }
                }
                return; // Don't update anything else when all dead
            }
            
            // Update spatial grid with current meteors
            spatialGrid.update(meteors);
            
            // All AI ships think and act in parallel with spatial optimization
            evolutionManager.getPopulation().parallelStream()
                .filter(ship -> ship.alive)
                .forEach(ship -> ship.thinkWithGrid(spatialGrid));
        }
        
        // Spawn meteors with increasing difficulty (1.1x faster ramp)
        // Calculate seconds elapsed
        int secondsElapsed = frameCounter / 60;
        
        // Spawn interval decreases over time (1.1x faster)
        // Reduced by 10% from previous rate (55 instead of 50)
        int spawnInterval = Math.max(30, 55 - (int)(secondsElapsed * 0.55));
        
        if (spawnTimer >= spawnInterval) {
            spawnMeteor(secondsElapsed);
            spawnTimer = 0;
        }
        
        // Update meteors and check for scoring
        meteors.parallelStream().forEach(meteor -> {
            meteor.update();
            
            if (playerMode) {
                // Award points to player when meteor exits any edge
                if (!meteor.scored && (meteor.y > 240 || meteor.y < -meteor.height || 
                                       meteor.x < -meteor.width || meteor.x > 320)) {
                    meteor.scored = true;
                    score += 10;
                }
            } else {
                // Award points when meteor exits any edge (to all alive ships)
                if (!meteor.scored && (meteor.y > 240 || meteor.y < -meteor.height || 
                                       meteor.x < -meteor.width || meteor.x > 320)) {
                    meteor.scored = true;
                    for (AIShip ship : evolutionManager.getPopulation()) {
                        if (ship.alive) {
                            ship.addScore(10);
                        }
                    }
                }
            }
        });
        
        // Check collisions (only for AI ships in AI mode) - parallel
        if (!playerMode) {
            double hitboxShrink = 0.1;
            
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
    
    private boolean checkCollision(Ship ship, Meteor meteor) {
        double hitboxShrink = 0.1;
        double shipHitX = ship.x + ship.getWidth() * hitboxShrink;
        double shipHitY = ship.y + ship.getHeight() * hitboxShrink;
        double shipHitW = ship.getWidth() * 0.8;
        double shipHitH = ship.getHeight() * 0.8;
        
        return shipHitX < meteor.x + meteor.width &&
               shipHitX + shipHitW > meteor.x &&
               shipHitY < meteor.y + meteor.height &&
               shipHitY + shipHitH > meteor.y;
    }
    
    private void spawnMeteor(int secondsElapsed) {
        int type = rand.nextInt(meteorImages.length);
        BufferedImage img = meteorImages[type];
        
        // Randomly choose spawn side: 0=top, 1=bottom, 2=left, 3=right
        int side = rand.nextInt(4);
        double x, y, vx, vy;
        
        switch (side) {
            case 0: // Top
                x = rand.nextDouble() * (320 - img.getWidth());
                y = -img.getHeight();
                vx = (rand.nextDouble() - 0.5) * 0.3;
                vy = 0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5);
                break;
            case 1: // Bottom
                x = rand.nextDouble() * (320 - img.getWidth());
                y = 240;
                vx = (rand.nextDouble() - 0.5) * 0.3;
                vy = -(0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5));
                break;
            case 2: // Left
                x = -img.getWidth();
                y = rand.nextDouble() * (240 - img.getHeight());
                vx = 0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5);
                vy = (rand.nextDouble() - 0.5) * 0.3;
                break;
            default: // Right
                x = 320;
                y = rand.nextDouble() * (240 - img.getHeight());
                vx = -(0.5 + (secondsElapsed / 164.0) * 1.0 + rand.nextDouble() * (0.3 + (secondsElapsed / 164.0) * 0.5));
                vy = (rand.nextDouble() - 0.5) * 0.3;
                break;
        }
        
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
        
        // Draw ships
        if (playerMode && playerShip != null) {
            // Draw player ship
            drawRotatedShip(g2d, playerShip);
        } else {
            // Draw all alive AI ships
            for (AIShip ship : evolutionManager.getPopulation()) {
                if (ship.alive) {
                    drawRotatedShip(g2d, ship);
                }
            }
        }
        
        // Draw raycasts if enabled
        if (showRaycasts && !playerMode) {
            for (AIShip ship : evolutionManager.getPopulation()) {
                if (ship.alive) {
                    // Get cached rays from ship
                    SpaceRaycast.RayResult[] rays = new SpaceRaycast.RayResult[18];
                    for (int i = 0; i < 18; i++) {
                        double angle = ship.rotation - 90 + (i * 20);
                        rays[i] = SpaceRaycast.castSingleRay(ship.getCenterX(), ship.getCenterY(), 
                                                             angle, spatialGrid, 120);
                    }
                    SpaceRaycast.drawRays(g2d, ship, rays);
                }
            }
        }
        
        // Draw UI text - skip in fast forward mode
        if (!fastForwardMode) {
            if (playerMode) {
                // Player mode: show current score and high score
                String scoreText = String.format("%05d", score);
                int scoreWidth = getTextWidth(scoreText);
                drawText(g2d, scoreText, 320 - scoreWidth - 15, 25);
                
                String highScoreText = "HI " + String.format("%05d", highScore);
                int hsWidth = getTextWidth(highScoreText);
                drawText(g2d, highScoreText, (320 - hsWidth) / 2, 25);
            } else {
                // AI mode: show GEN, ALIVE, best score, and high score
                String genText = "GEN " + evolutionManager.getGeneration();
                drawText(g2d, genText, 15, 45);
                
                String aliveText = "ALIVE " + evolutionManager.getAliveCount();
                drawText(g2d, aliveText, 15, 65);
                
                String scoreText = String.format("%05d", evolutionManager.getBestScoreThisGen());
                int scoreWidth = getTextWidth(scoreText);
                drawText(g2d, scoreText, 320 - scoreWidth - 15, 25);
                
                String highScoreText = "HI " + String.format("%05d", evolutionManager.getBestScoreEver());
                int hsWidth = getTextWidth(highScoreText);
                drawText(g2d, highScoreText, (320 - hsWidth) / 2, 25);
            }
        }
        
        // Draw fitness graph and neural network (only in AI mode and not in fast forward)
        if (!playerMode && !fastForwardMode) {
            visualizationUpdateCounter++;
            
            // Regenerate cached images once per second
            if (visualizationUpdateCounter >= VISUALIZATION_UPDATE_INTERVAL) {
                visualizationUpdateCounter = 0;
                
                // Create fitness graph cache at 2x resolution for better quality
                if (cachedFitnessGraph == null) {
                    cachedFitnessGraph = new BufferedImage(230, 126, BufferedImage.TYPE_INT_ARGB);
                }
                Graphics2D gFit = cachedFitnessGraph.createGraphics();
                gFit.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Clear the full image first (before scaling)
                gFit.setComposite(AlphaComposite.Clear);
                gFit.fillRect(0, 0, 230, 126);
                gFit.setComposite(AlphaComposite.SrcOver);
                // Now scale and draw
                gFit.scale(2.0, 2.0);
                drawFitnessGraphToImage(gFit);
                gFit.dispose();
                
                // Create neural network cache at 2x resolution for better quality
                if (cachedNeuralNetwork == null) {
                    cachedNeuralNetwork = new BufferedImage(230, 126, BufferedImage.TYPE_INT_ARGB);
                }
                Graphics2D gNet = cachedNeuralNetwork.createGraphics();
                gNet.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Clear the full image first (before scaling)
                gNet.setComposite(AlphaComposite.Clear);
                gNet.fillRect(0, 0, 230, 126);
                gNet.setComposite(AlphaComposite.SrcOver);
                // Now scale and draw
                gNet.scale(2.0, 2.0);
                drawNeuralNetworkToImage(gNet);
                gNet.dispose();
            }
            
            // Draw cached images scaled back down
            if (cachedFitnessGraph != null) {
                g2d.drawImage(cachedFitnessGraph, 5, 172, 115, 63, null);
            }
            if (cachedNeuralNetwork != null) {
                g2d.drawImage(cachedNeuralNetwork, 200, 172, 115, 63, null);
            }
        }
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
    
    private void drawFitnessGraphToImage(Graphics2D g2d) {
        List<Double> history = evolutionManager.getFitnessHistory();
        if (history.isEmpty()) return;
        
        int graphX = 5;
        int graphY = 5;
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
    
    private void drawNeuralNetworkToImage(Graphics2D g2d) {
        AIShip best = evolutionManager.getBestShip();
        if (best == null || best.brain.getLastInputs() == null) return;
        
        int netX = 5;
        int netY = 5;
        int netWidth = 105;
        int netHeight = 53;
        
        // Background
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRect(netX, netY, netWidth, netHeight);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawRect(netX, netY, netWidth, netHeight);
        
        double[] inputs = best.brain.getLastInputs();
        double[] h1 = best.brain.getLastHidden1();
        double[] h2 = best.brain.getLastHidden2();
        double[] outputs = best.brain.getLastOutput();
        
        if (outputs == null || outputs.length < 3 || inputs.length < 22) return;
        
        int nodeSize = 3;
        int[] layerX = {netX + 8, netX + 35, netX + 62, netX + 90};
        
        g2d.setStroke(new BasicStroke(1));
        
        // Draw connections (simplified - just show strong ones)
        final float threshold = 0.05f;
        
        // Input to H1 (show first 8 of 22 inputs)
        for (int i = 0; i < Math.min(8, inputs.length); i++) {
            int y1 = netY + 5 + i * 5;
            double input = inputs[i];
            for (int j = 0; j < Math.min(8, h1.length); j++) {
                float activation = (float)Math.abs(input * h1[j]);
                if (activation > threshold) {
                    int y2 = netY + 5 + j * 5;
                    g2d.setColor(new Color(activation, activation * 0.5f, 0, 0.3f));
                    g2d.drawLine(layerX[0], y1, layerX[1], y2);
                }
            }
        }
        
        // H1 to H2
        for (int i = 0; i < Math.min(8, h1.length); i++) {
            int y1 = netY + 5 + i * 5;
            for (int j = 0; j < Math.min(8, h2.length); j++) {
                float activation = (float)Math.abs(h1[i] * h2[j]);
                if (activation > threshold) {
                    int y2 = netY + 5 + j * 5;
                    g2d.setColor(new Color(activation, activation * 0.5f, 0, 0.3f));
                    g2d.drawLine(layerX[1], y1, layerX[2], y2);
                }
            }
        }
        
        // H2 to Outputs
        for (int out = 0; out < 3; out++) {
            int y2 = netY + 15 + out * 10;
            for (int i = 0; i < Math.min(8, h2.length); i++) {
                float activation = (float)Math.abs(h2[i]);
                if (activation > threshold) {
                    int y1 = netY + 5 + i * 5;
                    g2d.setColor(new Color(activation, activation * 0.5f, 0, 0.3f));
                    g2d.drawLine(layerX[2], y1, layerX[3], y2);
                }
            }
        }
        
        // Draw nodes - Input layer (first 8 of 22)
        for (int i = 0; i < Math.min(8, inputs.length); i++) {
            int y = netY + 5 + i * 5;
            float val = (float)Math.min(1.0, inputs[i]);
            g2d.setColor(new Color(1 - val, val, 0));
            g2d.fillOval(layerX[0] - 1, y - 1, nodeSize, nodeSize);
        }
        
        // Hidden layer 1 (first 8 nodes) - ReLU so 0 to positive
        for (int i = 0; i < Math.min(8, h1.length); i++) {
            int y = netY + 5 + i * 5;
            float val = (float)Math.min(1.0, Math.max(0, h1[i] / 2.0)); // Normalize ReLU output
            g2d.setColor(new Color(1 - val, val, 0));
            g2d.fillOval(layerX[1] - 1, y - 1, nodeSize, nodeSize);
        }
        
        // Hidden layer 2 (first 8 nodes)
        for (int i = 0; i < Math.min(8, h2.length); i++) {
            int y = netY + 5 + i * 5;
            float val = (float)Math.min(1.0, Math.max(0, h2[i] / 2.0));
            g2d.setColor(new Color(1 - val, val, 0));
            g2d.fillOval(layerX[2] - 1, y - 1, nodeSize, nodeSize);
        }
        
        // Outputs (left, right, thrust)
        String[] labels = {"L", "R", "T"};
        for (int i = 0; i < 3; i++) {
            int y = netY + 15 + i * 10;
            float val = (float)outputs[i];
            g2d.setColor(new Color(1 - val, val, 0));
            g2d.fillOval(layerX[3] - 1, y - 1, nodeSize, nodeSize);
            
            // Draw green ring if activated (> 0.5)
            if (outputs[i] > 0.5) {
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(0.8f));
                g2d.drawOval(layerX[3] - 2, y - 2, nodeSize + 2, nodeSize + 2);
            }
            
            // Label
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 6));
            g2d.drawString(labels[i], layerX[3] + 5, y + 2);
        }
    }
    
    /**
     * Draw text using letter/number images (same as plane game)
     */
    private void drawText(Graphics2D g2d, String text, int x, int y) {
        int currentX = x;
        for (char c : text.toCharArray()) {
            BufferedImage img = null;
            if (c >= '0' && c <= '9') {
                img = numberImages[c - '0'];
            } else if (c >= 'A' && c <= 'Z') {
                img = letterImages[c - 'A'];
            } else if (c >= 'a' && c <= 'z') {
                img = letterImages[c - 'a'];
            } else if (c == ' ') {
                currentX += 6;  // 75% of 8
                continue;
            }
            
            if (img != null) {
                g2d.drawImage(img, currentX, y - 13, null);
                currentX += img.getWidth() + 1;  // Add 1 pixel spacing between letters
            }
        }
    }
    
    /**
     * Calculate text width for positioning
     */
    private int getTextWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            if (c >= '0' && c <= '9') {
                width += numberImages[c - '0'].getWidth() + 1;  // Add 1 pixel spacing
            } else if (c >= 'A' && c <= 'Z') {
                width += letterImages[c - 'A'].getWidth() + 1;  // Add 1 pixel spacing
            } else if (c >= 'a' && c <= 'z') {
                width += letterImages[c - 'a'].getWidth() + 1;  // Add 1 pixel spacing
            } else if (c == ' ') {
                width += 6;  // 75% of 8
            }
        }
        return width;
    }
}
