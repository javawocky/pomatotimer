package com.thefryup.pomodoropilot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI-learning world rules shared by {@link GamePanel} and the headless trainer.
 *
 * The static helpers are what GamePanel calls each frame; an instance of this
 * class is a self-contained, renderless copy of the AI-mode game loop that can
 * run generations as fast as the CPU allows (used by tests to check that
 * evolution actually improves the pilots).
 */
public class AISimulation {
    public static final int PLANE_X = 80;
    public static final int PLANE_SIZE = 24;
    public static final int OBSTACLE_SPEED = 3;
    static final int PIPE_WIDTH = Collision.PIPE_WIDTH;

    // ---------------------------------------------------------------------
    // Shared rules
    // ---------------------------------------------------------------------

    /** Frames between spawns: starts at 120, reaches 24 after ~30s. */
    public static int spawnInterval(int frameCounter) {
        return Math.max(24, 120 - (frameCounter / 19));
    }

    /** Obstacles allowed ahead of the plane: 2, plus one every 2.3 seconds. */
    public static int maxObstacles(int secondsElapsed) {
        return 2 + (int) (secondsElapsed / 2.3);
    }

    public static int randomGapY(Random rand) {
        return 50 + rand.nextInt(120);
    }

    /**
     * Sense, think, move and collide every live plane for one frame.
     */
    public static void stepPopulation(List<AIPlane> population, ArrayList<GamePanel.Obstacle> obstacles, FlightMode mode) {
        GamePanel.Obstacle next = null;
        GamePanel.Obstacle second = null;
        for (GamePanel.Obstacle obs : obstacles) {
            if (obs.x + PIPE_WIDTH > PLANE_X) {
                if (next == null) {
                    next = obs;
                } else {
                    second = obs;
                    break;
                }
            }
        }

        double targetY = 120, targetDistance = 0, target2Y = 120, target2Distance = 0;
        if (next != null) {
            targetY = next.gapY;
            targetDistance = Math.hypot(next.x + PIPE_WIDTH / 2 - PLANE_X, targetY - 120);
        }
        if (second != null) {
            target2Y = second.gapY;
            target2Distance = Math.hypot(second.x + PIPE_WIDTH / 2 - PLANE_X, target2Y - 120);
        }

        for (AIPlane plane : population) {
            if (!plane.alive) continue;
            RaycastSensor.RayResult[] rays = castSensors(
                PLANE_X + PLANE_SIZE / 2, plane.y + PLANE_SIZE / 2, plane, mode, obstacles);
            plane.update(rays, PLANE_SIZE, targetY, targetDistance, target2Y, target2Distance, mode);
            for (GamePanel.Obstacle obs : obstacles) {
                plane.checkCollision(obs, PLANE_X, PLANE_SIZE, mode);
            }
        }
    }

    /** Rays follow the nose in FLY mode; bouncy planes have no pitch. */
    public static double sensorPitch(FlightBody plane, FlightMode mode) {
        return mode == FlightMode.FLY ? plane.pitch : 0.0;
    }
    
    /** The plane's sensor fan for the given mode (centre point in screen coordinates). */
    public static RaycastSensor.RayResult[] castSensors(double centerX, double centerY, FlightBody plane, FlightMode mode,
                                                        ArrayList<GamePanel.Obstacle> obstacles) {
        if (mode == FlightMode.FLY) {
            return RaycastSensor.castFlyRays(centerX, centerY, plane.pitch, obstacles);
        }
        return RaycastSensor.castRays(centerX, centerY, 0.0, obstacles, false);
    }

    // ---------------------------------------------------------------------
    // Headless trainer
    // ---------------------------------------------------------------------

    private final FlightMode mode;
    private final Random rand;
    private final EvolutionManager evolution;
    private final ArrayList<GamePanel.Obstacle> obstacles = new ArrayList<>();
    private final int maxFramesPerGeneration;
    private final List<Integer> bestScoreHistory = new ArrayList<>();
    private final List<Double> bestFitnessHistory = new ArrayList<>();
    private int frameCounter = 0;
    private int spawnTimer = 0;
    private int globalFrameCounter = 0;
    private final boolean superTraining;
    private final TrainingCourse course = new TrainingCourse();
    private long framesSimulated = 0;

    public AISimulation(FlightMode mode, long seed, int maxFramesPerGeneration) {
        this(mode, seed, maxFramesPerGeneration, false);
    }

    /** @param superTraining replay the course from a few obstacles before each crash point */
    public AISimulation(FlightMode mode, long seed, int maxFramesPerGeneration, boolean superTraining) {
        this.superTraining = superTraining;
        this.mode = mode;
        this.rand = new Random(seed);
        this.evolution = new EvolutionManager(new Random(seed * 31 + 7));
        this.maxFramesPerGeneration = maxFramesPerGeneration;
    }

    public EvolutionManager getEvolution() {
        return evolution;
    }

    public TrainingCourse getCourse() {
        return course;
    }
    
    double lastGenFitnessSpread, lastGenMaxObstacles, lastGenStartObstacle;

    /** Total frames simulated so far - a measure of training time spent. */
    public long getFramesSimulated() {
        return framesSimulated;
    }

    /** Best survival time (frames) of each completed generation, uncapped by the 30-entry UI history. */
    public List<Double> getBestFitnessHistory() {
        return bestFitnessHistory;
    }

    /** Best score (10 per obstacle passed) of each completed generation. */
    public List<Integer> getBestScoreHistory() {
        return bestScoreHistory;
    }

    public void runGenerations(int count) {
        for (int i = 0; i < count; i++) {
            runGeneration();
        }
    }

    /** Runs one generation to completion (all planes dead, or the frame cap hit) and evolves. */
    public void runGeneration() {
        obstacles.clear();
        frameCounter = superTraining ? course.beginRewound() : 0;
        if (!superTraining) course.beginFresh();
        if (superTraining) {
            for (AIPlane plane : evolution.getPopulation()) {
                plane.startOffset = frameCounter;
                plane.startObstacleOffset = course.getStartIndex();
            }
        }
        spawnTimer = 0;
        int startFrame = frameCounter;
        while (!evolution.allDead() && frameCounter - startFrame < maxFramesPerGeneration) {
            stepFrame();
        }
        for (AIPlane plane : evolution.getPopulation()) {
            plane.alive = false; // frame cap reached - end the generation
        }
        bestFitnessHistory.add(evolution.getBestFitnessThisGen());
        bestScoreHistory.add(evolution.getBestScoreThisGen());
        lastGenFitnessSpread = evolution.getBestFitnessThisGen()
            - evolution.getPopulation().stream().mapToDouble(AIPlane::getFitness).min().orElse(0);
        lastGenMaxObstacles = evolution.getPopulation().stream().mapToInt(AIPlane::getTotalObstaclesPassed).max().orElse(0);
        lastGenStartObstacle = course.getStartIndex();
        evolution.evolveNextGeneration();
    }

    /** Mirrors the AI-learning branch of GamePanel.update(). */
    private void stepFrame() {
        globalFrameCounter++;
        frameCounter++;
        spawnTimer++;

        framesSimulated++;
        long ahead = obstacles.stream().filter(o -> o.x + PIPE_WIDTH > PLANE_X).count();
        if (course.isReplaying()) {
            GamePanel.Obstacle replayed = course.nextReplay(frameCounter);
            if (replayed != null) {
                obstacles.add(replayed);
                spawnTimer = 0;
            }
        } else if (spawnTimer >= spawnInterval(frameCounter) && ahead < maxObstacles(globalFrameCounter / 60)) {
            obstacles.add(course.record(randomGapY(rand), frameCounter));
            spawnTimer = 0;
        }

        for (GamePanel.Obstacle obs : obstacles) {
            obs.x -= OBSTACLE_SPEED;
            if (!obs.scored && obs.x + PIPE_WIDTH < PLANE_X) {
                obs.scored = true;
                course.markPassed(obs);
                for (AIPlane plane : evolution.getPopulation()) {
                    if (plane.alive) plane.score += 10;
                }
            }
        }
        obstacles.removeIf(obs -> obs.x < -PIPE_WIDTH - 50);

        stepPopulation(evolution.getPopulation(), obstacles, mode);
    }
}
