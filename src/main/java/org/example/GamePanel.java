package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GamePanel extends JPanel {
    private BufferedImage background, planeBlue1, planeBlue2, planeBlue3, rock, rockDown, ground, platform;
    private BufferedImage planeRed1, planeRed2, planeRed3, planeGreen1, planeGreen2, planeGreen3, planeYellow1, planeYellow2, planeYellow3;
    private BufferedImage[] planeFrames;
    private BufferedImage[] numberImages = new BufferedImage[10];
    private BufferedImage numberColon;
    private BufferedImage[] letterImages = new BufferedImage[26];
    private int scrollX = 0;
    private int groundScrollX = 0;
    private double planeY = 100;
    private double planeVelY = 0;
    private double targetY = 100;
    private int frameCounter = 0;
    private int jumpTimer = 0;
    private int spawnTimer = 0;
    private int platformX = -100;
    private boolean platformSpawned = false;
    private String currentPlaneColor = "Blue";
    private String currentTerrain = "";
    private int cachedGroundY = 0;
    private int cachedLandingY = 0;
    private boolean renderBackground = true;
    
    private ArrayList<BackgroundPlane> backgroundPlanes = new ArrayList<>();
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private boolean isWorking = true;
    private boolean isLanding = false;
    private boolean isGameOver = false;
    private boolean isManualControl = false;
    private boolean spacePressed = false;
    private boolean landingSequenceActive = false;
    private int gameOverTimer = 0;
    private int landingProgress = 0;
    private String timeText = "00:00";
    private int score = 0;
    private int highScore = 0;
    private int planeWidth = 24;
    private int planeHeight = 24;
    private static final int PIPE_WIDTH = 50;
    
    private static final String[] FIRST_NAMES = {
        "ACE", "SKY", "JET", "MAVERICK", "STORM", "THUNDER", "LIGHTNING", "ROCKET",
        "TURBO", "SONIC", "BLAZE", "FLASH", "VIPER", "EAGLE", "HAWK", "FALCON",
        "PHOENIX", "GHOST", "SHADOW", "STEALTH"
    };
    
    private static final String[] LAST_NAMES = {
        "BOMBER", "DIVER", "EAGLE", "FALCON", "HUNTER", "RIDER", "FLYER", "SOARER",
        "GLIDER", "CRUISER", "RACER", "STRIKER", "FIGHTER", "PILOT", "AVIATOR", "WINGS",
        "SKYWALKER", "CLOUDCHASER", "WINDRUNNER", "STORMBREAKER"
    };
    
    private String playerName;
    private java.util.List<HighScoreEntry> highScoreTable = new java.util.ArrayList<>();
    private int highScoreScrollX = 0;
    private int introTimer = 0;
    private boolean showIntro = false;
    private int newHighScoreTimer = 0;
    private boolean showNewHighScore = false;
    private int newHighScoreValue = 0;
    private static final int PIPE_GAP = 80;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_STRENGTH = -4;

    public GamePanel() {
        setPreferredSize(new Dimension(320, 240));
        setBackground(Color.BLACK);
        
        System.setProperty("sun.java2d.opengl", "true");
        
        initializeHighScoreTable();
        
        // Set high score to the highest score in the table
        if (highScoreTable.size() > 0) {
            highScore = highScoreTable.get(0).score;
        }
        
        playerName = generateRandomName();
        
        String[] colors = {"Blue", "Green", "Red", "Yellow"};
        String[] terrains = {"Grass", "Ice", "Snow"};
        currentPlaneColor = colors[(int)(Math.random() * colors.length)];
        currentTerrain = terrains[(int)(Math.random() * terrains.length)];
        
        loadImages();
        planeFrames = new BufferedImage[]{planeBlue1, planeBlue2, planeBlue3};
        
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F) {
                    toggleFullscreen();
                } else if (e.getKeyCode() == KeyEvent.VK_B) {
                    renderBackground = !renderBackground;
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (isWorking && !isGameOver && !spacePressed && !landingSequenceActive) {
                        isManualControl = true;
                        // Jump - halfway between -3 and -6
                        planeVelY = -4.5;
                        spacePressed = true;
                    }
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    spacePressed = false;
                }
            }
        });
        
        new Thread(() -> {
            while (true) {
                SwingUtilities.invokeLater(() -> {
                    update();
                    repaint();
                    Toolkit.getDefaultToolkit().sync();
                });
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
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

    private void loadImages() {
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/background.png"));
            BufferedImage p1 = ImageIO.read(getClass().getResourceAsStream("/plane" + currentPlaneColor + "1.png"));
            BufferedImage p2 = ImageIO.read(getClass().getResourceAsStream("/plane" + currentPlaneColor + "2.png"));
            BufferedImage p3 = ImageIO.read(getClass().getResourceAsStream("/plane" + currentPlaneColor + "3.png"));
            planeBlue1 = scaleImage(p1, planeWidth, planeHeight);
            planeBlue2 = scaleImage(p2, planeWidth, planeHeight);
            planeBlue3 = scaleImage(p3, planeWidth, planeHeight);
            
            planeRed1 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeRed1.png")), planeWidth, planeHeight);
            planeRed2 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeRed2.png")), planeWidth, planeHeight);
            planeRed3 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeRed3.png")), planeWidth, planeHeight);
            planeGreen1 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeGreen1.png")), planeWidth, planeHeight);
            planeGreen2 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeGreen2.png")), planeWidth, planeHeight);
            planeGreen3 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeGreen3.png")), planeWidth, planeHeight);
            planeYellow1 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeYellow1.png")), planeWidth, planeHeight);
            planeYellow2 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeYellow2.png")), planeWidth, planeHeight);
            planeYellow3 = scaleImage(ImageIO.read(getClass().getResourceAsStream("/planeYellow3.png")), planeWidth, planeHeight);
            
            BufferedImage r = ImageIO.read(getClass().getResourceAsStream("/rock" + currentTerrain + ".png"));
            BufferedImage rd = ImageIO.read(getClass().getResourceAsStream("/rock" + currentTerrain + "Down.png"));
            rock = scaleImage(r, PIPE_WIDTH, 40);
            rockDown = scaleImage(rd, PIPE_WIDTH, 40);
            BufferedImage g = ImageIO.read(getClass().getResourceAsStream("/ground" + currentTerrain + ".png"));
            BufferedImage gDesaturated = desaturateImage(g, 0.5f);
            ground = scaleImage(gDesaturated, gDesaturated.getWidth() / 2, gDesaturated.getHeight() / 2);
            BufferedImage p = ImageIO.read(getClass().getResourceAsStream("/buttonLarge.png"));
            platform = scaleImage(p, 80, 30);
            
            for (int i = 0; i < 10; i++) {
                BufferedImage num = ImageIO.read(getClass().getResourceAsStream("/number" + i + ".png"));
                int scaledWidth = (int)(num.getWidth() * 18.0 / num.getHeight());
                numberImages[i] = scaleImage(num, scaledWidth, 18);
            }
            
            BufferedImage colon = ImageIO.read(getClass().getResourceAsStream("/numbercolon.png"));
            int colonWidth = (int)(colon.getWidth() * 18.0 / colon.getHeight());
            numberColon = scaleImage(colon, colonWidth, 18);
            
            for (int i = 0; i < 26; i++) {
                char letter = (char)('A' + i);
                BufferedImage let = ImageIO.read(getClass().getResourceAsStream("/letter" + letter + ".png"));
                int scaledWidth = (int)(let.getWidth() * 18.0 / let.getHeight());
                letterImages[i] = scaleImage(let, scaledWidth, 18);
            }
            
            cachedGroundY = 240 - ground.getHeight();
            cachedLandingY = 240 - ground.getHeight() - planeHeight - 30;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage scaleImage(BufferedImage img, int w, int h) {
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.drawImage(img, 0, 0, w, h, null);
        g2d.dispose();
        return result;
    }
    
    private BufferedImage desaturateImage(BufferedImage img, float saturation) {
        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                float[] hsb = Color.RGBtoHSB(r, g, b, null);
                hsb[1] *= saturation;
                
                int newRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                int newR = (newRgb >> 16) & 0xff;
                int newG = (newRgb >> 8) & 0xff;
                int newB = newRgb & 0xff;
                
                newR = (int)(newR + (200 - newR) * 0.4);
                newG = (int)(newG + (200 - newG) * 0.4);
                newB = (int)(newB + (200 - newB) * 0.4);
                
                result.setRGB(x, y, (a << 24) | (newR << 16) | (newG << 8) | newB);
            }
        }
        return result;
    }

    private void update() {
        // Update intro timer
        if (showIntro) {
            introTimer++;
            if (introTimer >= 300) { // 5 seconds at 60fps
                showIntro = false;
            }
        }
        
        // Update new high score timer
        if (showNewHighScore) {
            newHighScoreTimer++;
            if (newHighScoreTimer >= 300) { // 5 seconds at 60fps
                showNewHighScore = false;
            }
        }
        
        if (isGameOver) {
            gameOverTimer++;
            if (gameOverTimer > 90) {
                isGameOver = false;
                gameOverTimer = 0;
                playerName = generateRandomName();
                startWork();
            }
            return;
        }
        
        if (!isWorking) {
            for (BackgroundPlane bp : backgroundPlanes) {
                bp.x += bp.speed;
                
                // Collision avoidance AI
                BackgroundPlane closest = null;
                double closestDist = Double.MAX_VALUE;
                for (BackgroundPlane other : backgroundPlanes) {
                    if (other == bp) continue;
                    double dist = Math.abs(other.x - bp.x);
                    if (dist < closestDist && dist < 100) {
                        closestDist = dist;
                        closest = other;
                    }
                }
                
                if (closest != null) {
                    double yDiff = Math.abs(closest.y - bp.y);
                    if (yDiff < planeHeight) {
                        // Too close vertically, adjust target
                        if (bp.y < closest.y) {
                            bp.targetY = Math.max(15, bp.y - 5);
                        } else {
                            bp.targetY = Math.min(115, bp.y + 5);
                        }
                    } else {
                        bp.targetY = bp.y;
                    }
                } else {
                    bp.targetY = bp.y;
                }
                
                // Lerp towards target
                bp.y += (bp.targetY - bp.y) * 0.1;
            }
            backgroundPlanes.removeIf(bp -> bp.x < -100 || bp.x > 420);
            
            if (backgroundPlanes.size() < 5 && Math.random() < 0.1) {
                int y = getAvailableHeight();
                if (y != -1) {
                    double speed = 1.5 + Math.random() * 1.5;
                    boolean goingLeft = Math.random() < 0.5;
                    int x = goingLeft ? 370 : -80;
                    if (goingLeft) speed = -speed;
                    backgroundPlanes.add(new BackgroundPlane(x, y, speed));
                }
            }
            
            // Scroll high score table
            highScoreScrollX -= 1;
            
            if (isLanding) {
                frameCounter++;
            }
        }
        
        if (isWorking && !isLanding) {
            scrollX -= 1.5;
            if (scrollX <= -background.getWidth()) {
                scrollX += background.getWidth();
            }

            groundScrollX -= 2.6;
            if (groundScrollX <= -ground.getWidth()) {
                groundScrollX += ground.getWidth();
            }

            frameCounter++;
            spawnTimer++;
            
            if (!platformSpawned) {
                // Calculate max obstacles based on time: 2 for first 3s, 3 for next 3s, etc.
                int secondsElapsed = frameCounter / 60;
                int maxObstacles = 2 + (secondsElapsed / 3);
                
                // Count only obstacles ahead of the player
                int planeX = 80;
                long obstaclesAhead = obstacles.stream().filter(obs -> obs.x + PIPE_WIDTH > planeX).count();
                
                // Much faster spawn ramp: reach 24 frames in 30 seconds (30 * 60 / 96 = 18.75)
                int spawnInterval = Math.max(24, 120 - (frameCounter / 19));
                
                if (spawnTimer >= spawnInterval && obstaclesAhead < maxObstacles) {
                    int gapY = 50 + (int)(Math.random() * 120);
                    obstacles.add(new Obstacle(320 + PIPE_WIDTH, gapY));
                    spawnTimer = 0;
                    // Debug: print spawn interval every 10 obstacles
                    if (score % 100 == 0) {
                        System.out.println("Sec: " + secondsElapsed + ", Max: " + maxObstacles + ", Ahead: " + obstaclesAhead + ", Total: " + obstacles.size() + ", Interval: " + spawnInterval);
                    }
                }
            }

            if (platformSpawned) {
                if (planeY < cachedLandingY) {
                    platformX -= 3;
                    if (platformX < 80) {
                        platformX = 80;
                    }
                } else {
                    platformX = 80;
                }
            }

            for (Obstacle obs : obstacles) {
                obs.x -= 3;
                if (!obs.scored && obs.x + PIPE_WIDTH < 80) {
                    obs.scored = true;
                    score += 10;
                    if (score > highScore) {
                        highScore = score;
                        saveHighScore();
                    }
                }
            }
            obstacles.removeIf(obs -> obs.x < -PIPE_WIDTH - 50);

            int planeX = 80;
            Obstacle nextObs = null;
            for (Obstacle obs : obstacles) {
                if (obs.x + PIPE_WIDTH > planeX && (nextObs == null || obs.x < nextObs.x)) {
                    nextObs = obs;
                }
            }

            // AI or manual control
            if (!isManualControl) {
                // AI control
                if (platformSpawned && platformX < 100) {
                    targetY = cachedLandingY;
                } else if (nextObs != null) {
                    targetY = nextObs.gapY;
                } else {
                    targetY = 100;
                }

                jumpTimer++;
                if (jumpTimer > 12) {
                    double predictedY = planeY + planeVelY * 8;
                    if (predictedY > targetY + 15 || planeY > targetY + 10) {
                        planeVelY = JUMP_STRENGTH;
                        jumpTimer = 0;
                    }
                }
            }

            // Physics for both AI and manual
            planeVelY += GRAVITY;
            planeY += planeVelY;

            // AI lerp smoothing
            if (!isManualControl) {
                double lerpFactor = 0.05;
                if (planeY > targetY) {
                    planeY += (targetY - planeY) * lerpFactor;
                }
            }

            // Top boundary
            if (planeY < 10) {
                planeY = 10;
                planeVelY = 0;
            }
            
            // Bottom boundary - game over in manual mode at screen bottom
            if (planeY + planeHeight > 240) {
                if (isManualControl) {
                    System.out.println("\n=== FELL OFF BOTTOM ===");
                    System.out.println("Player: " + playerName);
                    System.out.println("Score: " + score);
                    
                    isGameOver = true;
                    gameOverTimer = 0;
                    
                    if (score > highScore) {
                        highScore = score;
                    }
                    boolean qualifies = highScoreTable.size() < 5 || score > highScoreTable.get(4).score;
                    if (qualifies) {
                        addHighScore(playerName, score);
                        showNewHighScore = true;
                        newHighScoreTimer = 0;
                        newHighScoreValue = score;
                    }
                    System.out.println("========================\n");
                } else {
                    planeY = cachedGroundY - planeHeight;
                    planeVelY = 0;
                }
            }
            
            // AI mode bottom clamp
            if (!isManualControl && planeY > 200) {
                planeY = 200;
                planeVelY = 0;
            }

            // Collision detection with triangular mountain peaks
            for (Obstacle obs : obstacles) {
                if (planeX + planeWidth > obs.x && planeX < obs.x + PIPE_WIDTH) {
                    int topPipeHeight = obs.gapY - PIPE_GAP/2;
                    int bottomPipeY = obs.gapY + PIPE_GAP/2;
                    
                    boolean collision = false;
                    
                    // Top mountain (pointing down) - triangle collision
                    if (topPipeHeight > 0) {
                        // Triangle: peak at (obs.x + PIPE_WIDTH/2, topPipeHeight), base from (obs.x, 0) to (obs.x + PIPE_WIDTH, 0)
                        collision = collision || checkTriangleCollision(
                            planeX, planeY, planeWidth, planeHeight,
                            obs.x + PIPE_WIDTH/2, topPipeHeight,  // peak
                            obs.x, 0,  // base left
                            obs.x + PIPE_WIDTH, 0  // base right
                        );
                    }
                    
                    // Bottom mountain (pointing up) - triangle collision
                    if (bottomPipeY < 240) {
                        // Triangle: peak at (obs.x + PIPE_WIDTH/2, bottomPipeY), base from (obs.x, 240) to (obs.x + PIPE_WIDTH, 240)
                        collision = collision || checkTriangleCollision(
                            planeX, planeY, planeWidth, planeHeight,
                            obs.x + PIPE_WIDTH/2, bottomPipeY,  // peak
                            obs.x, 240,  // base left
                            obs.x + PIPE_WIDTH, 240  // base right
                        );
                    }
                    
                    if (collision) {
                        System.out.println("\n=== COLLISION DETECTED ===");
                        System.out.println("Player: " + playerName);
                        System.out.println("Score: " + score);
                        
                        isGameOver = true;
                        gameOverTimer = 0;
                        
                        // Update high score immediately
                        if (score > highScore) {
                            highScore = score;
                            System.out.println("New high score: " + highScore);
                        }
                        // Check if score qualifies for high score table
                        boolean qualifies = highScoreTable.size() < 5 || score > highScoreTable.get(4).score;
                        System.out.println("Qualifies for table: " + qualifies);
                        if (qualifies) {
                            addHighScore(playerName, score);
                            showNewHighScore = true;
                            newHighScoreTimer = 0;
                            newHighScoreValue = score;
                            System.out.println("Added to high score table");
                        }
                        System.out.println("========================\n");
                    }
                }
            }
        } else if (isLanding) {
            landingProgress++;
            // Move plane to landing position
            if (planeY < cachedLandingY) {
                planeY += 2;
                if (planeY > cachedLandingY) planeY = cachedLandingY;
            } else if (planeY > cachedLandingY) {
                planeY -= 2;
                if (planeY < cachedLandingY) planeY = cachedLandingY;
            }
            if (landingProgress > 60) {
                isLanding = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double scaleX = panelWidth / 320.0;
        double scaleY = panelHeight / 240.0;
        double scale = Math.min(scaleX, scaleY);
        
        int scaledWidth = (int)(320 * scale);
        int scaledHeight = (int)(240 * scale);
        int offsetX = (panelWidth - scaledWidth) / 2;
        int offsetY = (panelHeight - scaledHeight) / 2;
        
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, panelWidth, panelHeight);
        
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);

        if (renderBackground) {
            int bgWidth = background.getWidth();
            double bgX = scrollX % bgWidth;
            g2d.drawImage(background, (int)bgX, 0, null);
            g2d.drawImage(background, (int)(bgX - bgWidth), 0, null);
            g2d.drawImage(background, (int)(bgX + bgWidth), 0, null);

            int gWidth = ground.getWidth();
            double gX = groundScrollX % gWidth;
            g2d.drawImage(ground, (int)gX, cachedGroundY, null);
            g2d.drawImage(ground, (int)(gX - gWidth), cachedGroundY, null);
            g2d.drawImage(ground, (int)(gX + gWidth), cachedGroundY, null);
        }

        for (Obstacle obs : obstacles) {
            int topPipeHeight = obs.gapY - PIPE_GAP/2;
            int bottomPipeY = obs.gapY + PIPE_GAP/2;
            
            if (topPipeHeight > 0) {
                g2d.drawImage(rockDown, obs.x, 0, PIPE_WIDTH, topPipeHeight, null);
            }
            if (bottomPipeY < 240) {
                g2d.drawImage(rock, obs.x, bottomPipeY, PIPE_WIDTH, 240 - bottomPipeY, null);
            }
        }

        if (platformSpawned && platformX > -100) {
            g2d.drawImage(platform, platformX, cachedLandingY + planeHeight, null);
        }

        if (!isLanding) {
            g2d.drawImage(planeFrames[(frameCounter / 5) % 3], 80, (int)planeY, null);
        } else {
            if (platformX > -100) {
                g2d.drawImage(platform, 80, cachedLandingY + planeHeight, null);
            }
            g2d.drawImage(planeFrames[0], 80, (int)planeY, null);
        }
        
        if (!isWorking) {
            for (BackgroundPlane bp : backgroundPlanes) {
                BufferedImage[] frames;
                switch(bp.color) {
                    case 0: frames = new BufferedImage[]{planeBlue1, planeBlue2, planeBlue3}; break;
                    case 1: frames = new BufferedImage[]{planeRed1, planeRed2, planeRed3}; break;
                    case 2: frames = new BufferedImage[]{planeGreen1, planeGreen2, planeGreen3}; break;
                    default: frames = new BufferedImage[]{planeYellow1, planeYellow2, planeYellow3}; break;
                }
                BufferedImage planeImg = frames[(frameCounter / 5) % 3];
                if (bp.speed < 0) {
                    BufferedImage flipped = new BufferedImage(planeImg.getWidth(), planeImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gFlip = flipped.createGraphics();
                    gFlip.drawImage(planeImg, planeImg.getWidth(), 0, -planeImg.getWidth(), planeImg.getHeight(), null);
                    gFlip.dispose();
                    g2d.drawImage(flipped, (int)bp.x, (int)bp.y, null);
                } else {
                    g2d.drawImage(planeImg, (int)bp.x, (int)bp.y, null);
                }
            }
        }

        drawText(g2d, timeText, 15, 25);
        
        String scoreText = String.format("%05d", score);
        int scoreWidth = getTextWidth(scoreText);
        drawText(g2d, scoreText, 320 - scoreWidth - 15, 25);
        
        String highScoreText = "HI " + String.format("%05d", highScore);
        int hsWidth = getTextWidth(highScoreText);
        drawText(g2d, highScoreText, (320 - hsWidth) / 2, 25);
        
        // Draw scrolling high score table during break
        if (!isWorking) {
            String highScoreString = buildHighScoreString();
            int textWidth = getTextWidth(highScoreString);
            drawText(g2d, highScoreString, highScoreScrollX, cachedGroundY + 5);
            
            // Reset scroll when text goes off screen
            if (highScoreScrollX + textWidth < 0) {
                highScoreScrollX = 320;
            }
        }
        
        // Draw intro message
        if (showIntro && (introTimer / 15) % 2 == 0) { // Flash 4 times per second (60fps / 15 = 4Hz)
            String introMessage = "LETS GO " + playerName;
            int introWidth = getTextWidth(introMessage);
            drawText(g2d, introMessage, (320 - introWidth) / 2, 120);
        }
        
        // Draw new high score message
        if (showNewHighScore) {
            String message = "NEW HIGH SCORE";
            int messageWidth = getTextWidth(message);
            drawText(g2d, message, (320 - messageWidth) / 2, 110);
            
            // Flash score 6 times per second (60fps / 10 = 6Hz)
            if ((newHighScoreTimer / 10) % 2 == 0) {
                String newScoreText = String.format("%05d", newHighScoreValue);
                int newScoreWidth = getTextWidth(newScoreText);
                drawText(g2d, newScoreText, (320 - newScoreWidth) / 2, 130);
            }
        }

        if (isGameOver) {
            // Draw "GAME OVER" using letter images with red tint
            String gameOverText = "GAME OVER";
            int textWidth = getTextWidth(gameOverText);
            drawText(g2d, gameOverText, (320 - textWidth) / 2, 100, true);
        }
    }

    public void setTimeText(String time) {
        this.timeText = time;
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        int totalSeconds = minutes * 60 + seconds;
        
        if (isWorking && totalSeconds <= 5 && !platformSpawned) {
            platformSpawned = true;
            platformX = 320;
            // Activate landing sequence - switch to AI and disable manual control
            landingSequenceActive = true;
            isManualControl = false;
        }
    }

    public void startWork() {
        // Generate new player name for new round
        playerName = generateRandomName();
        
        isWorking = true;
        isLanding = false;
        isManualControl = false;  // Reset to AI control
        landingSequenceActive = false;  // Reset landing sequence
        scrollX = 0;
        groundScrollX = 0;
        planeY = 100;
        planeVelY = 0;
        targetY = 100;
        jumpTimer = 0;
        spawnTimer = 0;
        score = 0;
        platformX = -100;
        platformSpawned = false;
        obstacles.clear();
        frameCounter = 0;
        introTimer = 0;
        showIntro = true;
        
        System.out.println("\n=== STARTING WORK PHASE ===");
        System.out.println("Player: " + playerName);
        
        String[] colors = {"Blue", "Green", "Red", "Yellow"};
        String[] terrains = {"Grass", "Ice", "Snow"};
        currentPlaneColor = colors[(int)(Math.random() * colors.length)];
        currentTerrain = terrains[(int)(Math.random() * terrains.length)];
        loadImages();
        planeFrames = new BufferedImage[]{planeBlue1, planeBlue2, planeBlue3};
    }

    public void startBreak() {
        System.out.println("\n=== STARTING BREAK PHASE ===");
        System.out.println("Player: " + playerName);
        System.out.println("Current Score: " + score);
        System.out.println("Current High Score: " + highScore);
        
        System.out.println("\nHigh Score Table BEFORE update:");
        for (int i = 0; i < highScoreTable.size(); i++) {
            HighScoreEntry entry = highScoreTable.get(i);
            System.out.println("  " + (i+1) + ": " + entry.name + " - " + entry.score);
        }
        
        // Update high score table with current score
        if (score > highScore) {
            highScore = score;
            System.out.println("\nUpdated high score to: " + highScore);
        }
        
        boolean qualifies = highScoreTable.size() < 5 || score > highScoreTable.get(4).score;
        System.out.println("\nQualifies for high score table: " + qualifies);
        if (qualifies) {
            System.out.println("Adding " + playerName + " with score " + score);
            addHighScore(playerName, score);
            showNewHighScore = true;
            newHighScoreTimer = 0;
            newHighScoreValue = score;
        }
        
        System.out.println("\nHigh Score Table AFTER update:");
        for (int i = 0; i < highScoreTable.size(); i++) {
            HighScoreEntry entry = highScoreTable.get(i);
            System.out.println("  " + (i+1) + ": " + entry.name + " - " + entry.score);
        }
        System.out.println("========================\n");
        
        isWorking = false;
        isLanding = true;
        landingProgress = 0;
        planeVelY = 0;
        scrollX = 0;
        groundScrollX = 0;
        highScoreScrollX = 320;
        
        backgroundPlanes.clear();
        int targetPlanes = 5;
        int maxAttempts = 50;
        for (int i = 0; i < targetPlanes && maxAttempts > 0; i++) {
            int y = getAvailableHeight();
            if (y == -1) {
                maxAttempts--;
                i--;
                continue;
            }
            double speed = 1.5 + Math.random() * 1.5;
            boolean goingLeft = Math.random() < 0.5;
            int x = goingLeft ? 320 + 50 + (int)(Math.random() * 200) : -50 - (int)(Math.random() * 200);
            if (goingLeft) speed = -speed;
            backgroundPlanes.add(new BackgroundPlane(x, y, speed));
        }
    }
    
    private int getAvailableHeight() {
        int minSeparation = planeHeight + 10;
        for (int attempt = 0; attempt < 50; attempt++) {
            int y = 15 + (int)(Math.random() * 100);
            boolean tooClose = false;
            for (BackgroundPlane bp : backgroundPlanes) {
                if (Math.abs(bp.y - y) < minSeparation) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) return y;
        }
        return -1;
    }
    
    private void drawText(Graphics2D g2d, String text, int x, int y) {
        drawText(g2d, text, x, y, false);
    }
    
    private void drawText(Graphics2D g2d, String text, int x, int y, boolean redTint) {
        int currentX = x;
        for (char c : text.toCharArray()) {
            BufferedImage img = null;
            if (c >= '0' && c <= '9') {
                img = numberImages[c - '0'];
            } else if (c == ':') {
                img = numberColon;
            } else if (c >= 'A' && c <= 'Z') {
                img = letterImages[c - 'A'];
            } else if (c >= 'a' && c <= 'z') {
                img = letterImages[c - 'a'];
            } else if (c == ' ') {
                currentX += 8;
                continue;
            }
            
            if (img != null) {
                if (redTint) {
                    img = applyRedTint(img);
                }
                g2d.drawImage(img, currentX, y - 18, null);
                currentX += img.getWidth();
            }
        }
    }
    
    private BufferedImage applyRedTint(BufferedImage src) {
        BufferedImage tinted = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int pixel = src.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                
                // Tint blue towards red
                red = Math.min(255, red + blue / 2);
                blue = blue / 2;
                
                int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                tinted.setRGB(x, y, newPixel);
            }
        }
        return tinted;
    }
    
    private int getTextWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            if (c >= '0' && c <= '9') {
                width += numberImages[c - '0'].getWidth();
            } else if (c == ':') {
                width += numberColon.getWidth();
            } else if (c >= 'A' && c <= 'Z') {
                width += letterImages[c - 'A'].getWidth();
            } else if (c >= 'a' && c <= 'z') {
                width += letterImages[c - 'a'].getWidth();
            } else if (c == ' ') {
                width += 8;
            }
        }
        return width;
    }

    static class HighScoreEntry {
        String name;
        int score;
        HighScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    static class BackgroundPlane {
        double x, y, speed;
        int color;
        double targetY;
        BackgroundPlane(double x, int y, double speed) {
            this.x = x;
            this.y = y;
            this.targetY = y;
            this.speed = speed;
            this.color = (int)(Math.random() * 4);
        }
    }

    static class Obstacle {
        int x, gapY;
        boolean scored = false;
        Obstacle(int x, int gapY) {
            this.x = x;
            this.gapY = gapY;
        }
    }

    // Test accessor methods
    public void step(int frames) {
        for (int i = 0; i < frames; i++) {
            update();
        }
    }
    
    public void updateForTest() {
        update();
    }
    
    public int getBackgroundPlaneCount() {
        return backgroundPlanes.size();
    }
    
    public BackgroundPlane getBackgroundPlane(int index) {
        if (index < backgroundPlanes.size()) {
            return backgroundPlanes.get(index);
        }
        return null;
    }
    
    public boolean isLanding() {
        return isLanding;
    }

    public double getPlaneY() {
        return planeY;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public int getFrameCount() {
        return frameCounter;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setObstacle(int x, int gapY) {
        obstacles.add(new Obstacle(x, gapY));
    }
    
    public int getHighScoreTableSize() {
        return highScoreTable.size();
    }
    
    public HighScoreEntry getHighScoreEntry(int index) {
        return highScoreTable.get(index);
    }
    
    public void addHighScorePublic(String name, int score) {
        addHighScore(name, score);
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getScore() {
        return score;
    }
    
    public void updatePublic() {
        update();
    }
    
    public boolean isGameOver() {
        return isGameOver;
    }
    
    public void setPlaneY(double y) {
        this.planeY = y;
    }
    
    public boolean isShowNewHighScore() {
        return showNewHighScore;
    }
    
    public int getNewHighScoreValue() {
        return newHighScoreValue;
    }
    
    public void setScoreForTest(int score) {
        this.score = score;
    }
    
    public void dumpState() {
        System.out.println("Player: " + playerName);
        System.out.println("Score: " + score);
        System.out.println("High Score: " + highScore);
        System.out.println("Is Working: " + isWorking);
        System.out.println("Is Game Over: " + isGameOver);
        System.out.println("Is Manual Control: " + isManualControl);
        System.out.println("Show New High Score: " + showNewHighScore);
        System.out.println("New High Score Value: " + newHighScoreValue);
        System.out.println("\nHigh Score Table:");
        for (int i = 0; i < highScoreTable.size(); i++) {
            HighScoreEntry entry = highScoreTable.get(i);
            System.out.println("  " + (i+1) + ": " + entry.name + " - " + entry.score);
        }
    }
    
    private boolean checkTriangleCollision(double rectX, double rectY, double rectW, double rectH,
                                           double tx1, double ty1, double tx2, double ty2, double tx3, double ty3) {
        // Check if any corner of the rectangle is inside the triangle
        if (pointInTriangle(rectX, rectY, tx1, ty1, tx2, ty2, tx3, ty3)) return true;
        if (pointInTriangle(rectX + rectW, rectY, tx1, ty1, tx2, ty2, tx3, ty3)) return true;
        if (pointInTriangle(rectX, rectY + rectH, tx1, ty1, tx2, ty2, tx3, ty3)) return true;
        if (pointInTriangle(rectX + rectW, rectY + rectH, tx1, ty1, tx2, ty2, tx3, ty3)) return true;
        
        // Check if any triangle vertex is inside the rectangle
        if (tx1 >= rectX && tx1 <= rectX + rectW && ty1 >= rectY && ty1 <= rectY + rectH) return true;
        if (tx2 >= rectX && tx2 <= rectX + rectW && ty2 >= rectY && ty2 <= rectY + rectH) return true;
        if (tx3 >= rectX && tx3 <= rectX + rectW && ty3 >= rectY && ty3 <= rectY + rectH) return true;
        
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
    
    private void loadHighScore() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), ".pomatotimer_highscore");
            if (file.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(file);
                if (scanner.hasNextInt()) {
                    highScore = scanner.nextInt();
                }
                scanner.close();
            }
        } catch (Exception e) {
            highScore = 0;
        }
    }
    
    private String generateRandomName() {
        String first = FIRST_NAMES[(int)(Math.random() * FIRST_NAMES.length)];
        String last = LAST_NAMES[(int)(Math.random() * LAST_NAMES.length)];
        return first + " " + last;
    }
    
    private void initializeHighScoreTable() {
        highScoreTable.clear();
        int[] scores = {200, 150, 100, 75, 50};
        for (int score : scores) {
            highScoreTable.add(new HighScoreEntry(generateRandomName(), score));
        }
    }
    
    private void addHighScore(String name, int score) {
        highScoreTable.add(new HighScoreEntry(name, score));
        highScoreTable.sort((a, b) -> b.score - a.score);
        if (highScoreTable.size() > 5) {
            highScoreTable.remove(5);
        }
    }
    
    private String buildHighScoreString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < highScoreTable.size(); i++) {
            HighScoreEntry entry = highScoreTable.get(i);
            sb.append((i + 1)).append(": ").append(entry.name).append(" ").append(entry.score);
            if (i < highScoreTable.size() - 1) {
                sb.append("   ");
            }
        }
        return sb.toString();
    }
    
    private void saveHighScore() {
        try {
            java.io.File file = new java.io.File(System.getProperty("user.home"), ".pomatotimer_highscore");
            java.io.PrintWriter writer = new java.io.PrintWriter(file);
            writer.println(highScore);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
