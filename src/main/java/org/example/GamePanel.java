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
    private BufferedImage[] planeFrames;
    private BufferedImage[] numberImages = new BufferedImage[10];
    private BufferedImage[] letterImages = new BufferedImage[26];
    private int scrollX = 0;
    private int groundScrollX = 0;
    private double planeY = 100;
    private double planeVelY = 0;
    private double targetY = 100;
    private int frameCounter = 0;
    private int jumpTimer = 0;
    private int platformX = -100;
    private boolean platformSpawned = false;
    private String currentPlaneColor = "Blue";
    private String currentTerrain = "";
    private int cachedGroundY = 0;
    private int cachedLandingY = 0;
    private boolean renderBackground = true;
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private boolean isWorking = true;
    private boolean isLanding = false;
    private boolean isGameOver = false;
    private int gameOverTimer = 0;
    private int landingProgress = 0;
    private String timeText = "00:00";
    private int score = 0;
    private int highScore = 0;
    private int planeWidth = 24;
    private int planeHeight = 24;
    private static final int PIPE_WIDTH = 50;
    private static final int PIPE_GAP = 80;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_STRENGTH = -4;

    public GamePanel() {
        setPreferredSize(new Dimension(320, 240));
        setBackground(Color.BLACK);
        
        System.setProperty("sun.java2d.opengl", "true");
        
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
        if (isGameOver) {
            gameOverTimer++;
            if (gameOverTimer > 90) {
                if (score > highScore) {
                    highScore = score;
                }
                isGameOver = false;
                gameOverTimer = 0;
                startWork();
            }
            return;
        }
        
        if (isWorking && !isLanding) {
            scrollX -= 1.5;
            if (scrollX < -background.getWidth()) {
                scrollX = 0;
            }

            groundScrollX -= 2.6;
            if (groundScrollX < -ground.getWidth()) {
                groundScrollX = 0;
            }

            frameCounter++;
            
            if (!platformSpawned) {
                int spawnInterval = Math.max(24, 120 - (frameCounter / 150));
                
                if (frameCounter % spawnInterval == 0) {
                    int gapY = 60 + (int)(Math.random() * 100);
                    obstacles.add(new Obstacle(320, gapY));
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

            planeVelY += GRAVITY;
            planeY += planeVelY;

            double lerpFactor = 0.05;
            if (planeY > targetY) {
                planeY += (targetY - planeY) * lerpFactor;
            }

            if (planeY < 10) {
                planeY = 10;
                planeVelY = 0;
            }
            if (planeY > 200) {
                planeY = 200;
                planeVelY = 0;
            }

            // Collision detection
            for (Obstacle obs : obstacles) {
                if (planeX + planeWidth > obs.x && planeX < obs.x + PIPE_WIDTH) {
                    int topPipeHeight = obs.gapY - PIPE_GAP/2;
                    int bottomPipeY = obs.gapY + PIPE_GAP/2;
                    if (planeY < topPipeHeight || planeY + planeHeight > bottomPipeY) {
                        isGameOver = true;
                        gameOverTimer = 0;
                    }
                }
            }
        } else if (isLanding) {
            landingProgress++;
            if (planeY < cachedLandingY) {
                planeY += 2;
                if (planeY > cachedLandingY) planeY = cachedLandingY;
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
            g2d.drawImage(background, (int)scrollX, 0, null);
            g2d.drawImage(background, (int)scrollX + background.getWidth(), 0, null);
            g2d.drawImage(background, (int)scrollX + background.getWidth() * 2, 0, null);

            g2d.drawImage(ground, (int)groundScrollX, cachedGroundY, null);
            g2d.drawImage(ground, (int)groundScrollX + ground.getWidth(), cachedGroundY, null);
            g2d.drawImage(ground, (int)groundScrollX + ground.getWidth() * 2, cachedGroundY, null);
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

        drawText(g2d, timeText, 15, 25);
        
        String scoreText = String.format("%05d", score);
        int scoreWidth = getTextWidth(scoreText);
        drawText(g2d, scoreText, 320 - scoreWidth - 15, 25);
        
        String highScoreText = "HI " + String.format("%05d", highScore);
        int hsWidth = getTextWidth(highScoreText);
        drawText(g2d, highScoreText, (320 - hsWidth) / 2, 25);

        if (isGameOver) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, 320, 240);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString("Game Over!", 70, 120);
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
        }
    }

    public void startWork() {
        isWorking = true;
        isLanding = false;
        scrollX = 0;
        groundScrollX = 0;
        planeY = 100;
        planeVelY = 0;
        targetY = 100;
        jumpTimer = 0;
        score = 0;
        platformX = -100;
        platformSpawned = false;
        obstacles.clear();
        frameCounter = 0;
        
        String[] colors = {"Blue", "Green", "Red", "Yellow"};
        String[] terrains = {"Grass", "Ice", "Snow"};
        currentPlaneColor = colors[(int)(Math.random() * colors.length)];
        currentTerrain = terrains[(int)(Math.random() * terrains.length)];
        loadImages();
        planeFrames = new BufferedImage[]{planeBlue1, planeBlue2, planeBlue3};
    }

    public void startBreak() {
        isWorking = false;
        isLanding = true;
        landingProgress = 0;
        planeVelY = 0;
        scrollX = 0;
        groundScrollX = 0;
    }
    
    private void drawText(Graphics2D g2d, String text, int x, int y) {
        int currentX = x;
        for (char c : text.toCharArray()) {
            if (c >= '0' && c <= '9') {
                BufferedImage img = numberImages[c - '0'];
                g2d.drawImage(img, currentX, y - 18, null);
                currentX += img.getWidth();
            } else if (c >= 'A' && c <= 'Z') {
                BufferedImage img = letterImages[c - 'A'];
                g2d.drawImage(img, currentX, y - 18, null);
                currentX += img.getWidth();
            } else if (c >= 'a' && c <= 'z') {
                BufferedImage img = letterImages[c - 'a'];
                g2d.drawImage(img, currentX, y - 18, null);
                currentX += img.getWidth();
            } else if (c == ' ' || c == ':') {
                currentX += 8;
            }
        }
    }
    
    private int getTextWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            if (c >= '0' && c <= '9') {
                width += numberImages[c - '0'].getWidth();
            } else if (c >= 'A' && c <= 'Z') {
                width += letterImages[c - 'A'].getWidth();
            } else if (c >= 'a' && c <= 'z') {
                width += letterImages[c - 'a'].getWidth();
            } else if (c == ' ' || c == ':') {
                width += 8;
            }
        }
        return width;
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
}
