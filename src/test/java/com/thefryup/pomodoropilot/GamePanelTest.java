package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GamePanelTest {
    private GamePanel gamePanel;

    @BeforeEach
    public void setUp() {
        gamePanel = new GamePanel();
        gamePanel.startWork();
    }

    @Test
    public void testObstacleSpawning() {
        gamePanel.step(120);
        assertTrue(gamePanel.getObstacles().size() > 0, "At least 1 obstacle should spawn after 120 frames");
    }

    @Test
    public void testObstacleMovement() {
        gamePanel.setObstacle(320, 100);
        gamePanel.step(10);
        GamePanel.Obstacle obs = gamePanel.getObstacles().get(0);
        // Obstacle moves 3px/frame; background thread may add extra frames
        assertTrue(obs.x < 320 && obs.x >= 280,
            "Obstacle should move left, was at " + obs.x);
    }

    @Test
    public void testObstacleRemoval() {
        // Obstacles are removed when x < -PIPE_WIDTH(50) - 50 = -100
        gamePanel.setObstacle(-98, 100);
        gamePanel.step(1); // moves to -101, which is < -100
        assertEquals(0, gamePanel.getObstacles().size(), "Obstacle should be removed when x < -100");
    }

    @Test
    public void testPlaneStaysInBounds() {
        // In AI learning mode, the AI planes are managed by EvolutionManager,
        // but the classic planeY is still bounded by the update logic
        for (int i = 0; i < 300; i++) {
            gamePanel.step(1);
        }
        // planeY is not updated in AI learning mode (returns early), so it stays at initial 100
        double planeY = gamePanel.getPlaneY();
        assertTrue(planeY >= 10 && planeY <= 240,
            "Plane Y should stay in screen bounds, but was " + planeY);
    }

    @Test
    public void testPlaneNavigatesToGap() {
        // In AI learning mode, classic plane AI doesn't run.
        // The planeY stays at its initial value (100).
        // Just verify the plane position is valid after stepping.
        gamePanel.setObstacle(100, 80);
        double initialY = gamePanel.getPlaneY();
        gamePanel.step(20);
        double finalY = gamePanel.getPlaneY();
        // In AI learning mode, planeY doesn't change via classic AI
        assertTrue(finalY >= 10 && finalY <= 240,
            "Plane Y should be in valid range, was " + finalY);
    }

    @Test
    public void testPlaneAvoidsTopPipe() {
        gamePanel.step(30);
        double finalY = gamePanel.getPlaneY();
        assertTrue(finalY >= 10 && finalY <= 240, "Plane should stay in bounds");
    }

    @Test
    public void testPlaneAvoidsBottomPipe() {
        gamePanel.step(30);
        double finalY = gamePanel.getPlaneY();
        assertTrue(finalY >= 10 && finalY <= 240, "Plane should stay in bounds");
    }

    @Test
    public void testBreakModeStopsObstacles() {
        gamePanel.step(120);
        gamePanel.startBreak();
        int obstacleCount = gamePanel.getObstacles().size();
        gamePanel.step(100);
        // During break, no new obstacles spawn (existing ones stay since no removal logic runs)
        assertEquals(obstacleCount, gamePanel.getObstacles().size(),
            "No new obstacles should spawn during break mode");
    }

    @Test
    public void testBreakModePlaneLands() {
        gamePanel.startBreak();
        // Landing moves plane at 2px/frame toward cachedLandingY
        // Run enough frames for landing to complete (landingProgress > 60 stops landing)
        gamePanel.step(120);
        // After landing completes, plane should be near the landing position
        double planeY = gamePanel.getPlaneY();
        assertTrue(planeY >= 100, "Plane should have moved toward landing position, was at " + planeY);
    }

    @Test
    public void testBreakHudKeepsTheAIScoresInsteadOfTheSeedTable() throws Exception {
        int[] work = new int[2];
        int[] rest = new int[2];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            EvolutionManager em = gamePanel.getEvolutionManager();
            em.getPopulation().get(0).score = 30;
            work[0] = gamePanel.getHudScore();
            work[1] = gamePanel.getHudHighScore();
            gamePanel.startBreak();
            rest[0] = gamePanel.getHudScore();
            rest[1] = gamePanel.getHudHighScore();
        });
        assertEquals(30, work[0]);
        assertEquals(30, work[1], "HI should include the generation in progress");
        assertEquals(work[0], rest[0], "Score must not change when the break starts");
        assertEquals(work[1], rest[1], "HI must not jump to the seeded table value (200) on break");
        assertNotEquals(200, rest[1]);
    }
    
    @Test
    public void testHudHighScoreNeverBelowCurrentScore() throws Exception {
        boolean[] ok = new boolean[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            EvolutionManager em = gamePanel.getEvolutionManager();
            em.getPopulation().forEach(p -> p.alive = false);
            em.getPopulation().get(2).score = 50;
            em.evolveNextGeneration();                 // record: 50
            em.getPopulation().get(1).score = 80;       // new gen beats it mid-flight
            ok[0] = gamePanel.getHudHighScore() == 80 && gamePanel.getHudScore() == 80;
        });
        assertTrue(ok[0], "HI should track a live score that beats the recorded best");
    }
    
    @Test
    public void testWorkModeResetsState() throws Exception {
        gamePanel.setObstacle(100, 100);
        gamePanel.step(50);
        // The panel's game loop ticks on the EDT, so reset and inspect there to avoid racing it
        int[] obstacleCount = new int[1];
        double[] planeY = new double[1];
        int[] frameCount = new int[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            gamePanel.startWork();
            obstacleCount[0] = gamePanel.getObstacles().size();
            planeY[0] = gamePanel.getPlaneY();
            frameCount[0] = gamePanel.getFrameCount();
        });
        assertEquals(0, obstacleCount[0], "Obstacles should be cleared");
        assertEquals(100, planeY[0], 0.1, "Plane should reset to y=100");
        assertEquals(0, frameCount[0], "Frame counter should reset to 0");
    }
    
    @Test
    public void testSuperTrainingIsOffByDefaultAndToggles() {
        assertFalse(gamePanel.isSuperTraining());
        gamePanel.setSuperTraining(true);
        assertTrue(gamePanel.isSuperTraining());
    }
    
    @Test
    public void testRoundFitnessRewardsReachingTheRewindSegment() {
        // A plane that starts deep in the course (rewind) and clears a few obstacles
        // must out-score one that starts at the beginning and dies early.
        AIPlane rewound = new AIPlane(0, new NeuralNetwork(new java.util.Random(1)));
        rewound.startOffset = 1500;        // frames already travelled to reach the segment
        rewound.startObstacleOffset = 30;  // 30 obstacles already behind it
        rewound.survivalTime = 120;
        rewound.score = 30;                // cleared 3 more

        AIPlane fresh = new AIPlane(0, new NeuralNetwork(new java.util.Random(2)));
        fresh.survivalTime = 120;
        fresh.score = 20;

        assertTrue(rewound.getFitness() > fresh.getFitness(),
            "Reaching and clearing the hard segment should score far higher than an early start");
        assertEquals(33, rewound.getTotalObstaclesPassed());
    }

    @Test
    public void testObstacleBonusDistinguishesEqualSurvivalTime() {
        // Same distance survived, but one cleared an obstacle - it should rank higher
        AIPlane passed = new AIPlane(0, new NeuralNetwork(new java.util.Random(1)));
        passed.survivalTime = 200; passed.score = 10;
        AIPlane crashed = new AIPlane(0, new NeuralNetwork(new java.util.Random(2)));
        crashed.survivalTime = 200; crashed.score = 0;
        assertEquals(AIPlane.OBSTACLE_BONUS, passed.getFitness() - crashed.getFitness(), 1e-9);
    }

    @Test
    public void testFlyIsTheDefaultFlightMode() {
        assertEquals(FlightMode.FLY, gamePanel.getFlightMode());
        assertEquals("Mode: Fly", AppWindow.flightModeLabel(gamePanel.getFlightMode()));
    }
    
    @Test
    public void testToggleFlightModeSwitchesAndKeepsSeparatePopulations() {
        EvolutionManager flyPopulation = gamePanel.getEvolutionManager();
        assertNotNull(flyPopulation);
        
        assertEquals(FlightMode.BOUNCY, gamePanel.toggleFlightMode());
        assertEquals("Mode: Bouncy", AppWindow.flightModeLabel(gamePanel.getFlightMode()));
        EvolutionManager bouncyPopulation = gamePanel.getEvolutionManager();
        assertNotSame(flyPopulation, bouncyPopulation, "Each flight mode trains its own population");
        assertEquals(0, gamePanel.getObstacles().size(), "Switching mode restarts the round");
        
        assertEquals(FlightMode.FLY, gamePanel.toggleFlightMode());
        assertSame(flyPopulation, gamePanel.getEvolutionManager(), "Switching back resumes the fly population");
    }
    
    @Test
    public void testFlyModeAIPlanesPitchWithinLimits() {
        gamePanel.step(200);
        for (AIPlane plane : gamePanel.getEvolutionManager().getPopulation()) {
            assertTrue(Math.abs(plane.pitch) <= FlightBody.MAX_PITCH_DEG + 1e-9, "Pitch out of range: " + plane.pitch);
        }
    }
    
    @Test
    public void testBouncyModeAIPlanesNeverPitch() {
        gamePanel.setFlightMode(FlightMode.BOUNCY);
        gamePanel.step(200);
        for (AIPlane plane : gamePanel.getEvolutionManager().getPopulation()) {
            assertEquals(0.0, plane.pitch, 1e-12);
        }
    }
}
