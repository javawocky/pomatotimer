package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that the neuro-evolution actually learns to fly, using the headless
 * {@link AISimulation} which runs the same world rules as the game's AI mode.
 * Everything is seeded, so results are deterministic.
 */
public class AILearningViabilityTest {
    private static final int GENERATIONS = 40;
    private static final int FRAME_CAP = 3000;
    private static final long[] SEEDS = {11, 42};

    private static double mean(List<? extends Number> values, int from, int to) {
        double sum = 0;
        for (int i = from; i < to; i++) sum += values.get(i).doubleValue();
        return sum / (to - from);
    }

    private void assertLearns(FlightMode mode) {
        for (long seed : SEEDS) {
            AISimulation sim = new AISimulation(mode, seed, FRAME_CAP);
            sim.runGenerations(GENERATIONS);

            List<Double> fitness = sim.getBestFitnessHistory();
            List<Integer> scores = sim.getBestScoreHistory();
            assertEquals(GENERATIONS, fitness.size());

            double early = mean(fitness, 0, 5);
            double late = mean(fitness, GENERATIONS - 10, GENERATIONS);
            int bestScore = scores.stream().mapToInt(Integer::intValue).max().orElse(0);
            String summary = mode + " seed " + seed + ": early=" + early + " late=" + late + " bestScore=" + bestScore;

            assertTrue(late >= early * 2.0, "Best survival should at least double with training. " + summary);
            assertTrue(late > 700, "Trained pilots should survive well past the opening obstacles. " + summary);
            assertTrue(bestScore >= 100, "Some pilot should clear 10+ obstacles. " + summary);
            assertEquals(GENERATIONS + 1, sim.getEvolution().getGeneration());
        }
    }

    @Test
    public void testFlyModeAIImprovesOverGenerations() {
        assertLearns(FlightMode.FLY);
    }

    @Test
    public void testBouncyModeAIImprovesOverGenerations() {
        assertLearns(FlightMode.BOUNCY);
    }

    @Test
    public void testFlyModeAIPlanesRespectPitchLimits() {
        AISimulation sim = new AISimulation(FlightMode.FLY, 3, 600);
        // Watch the population during a generation via the shared step
        ArrayList<GamePanel.Obstacle> obstacles = new ArrayList<>();
        obstacles.add(new GamePanel.Obstacle(200, 80));
        List<AIPlane> population = sim.getEvolution().getPopulation();
        boolean anyPitched = false;
        for (int frame = 0; frame < 120; frame++) {
            AISimulation.stepPopulation(population, obstacles, FlightMode.FLY);
            for (AIPlane p : population) {
                assertTrue(Math.abs(p.pitch) <= FlightBody.MAX_PITCH_DEG + 1e-9, "Pitch out of range: " + p.pitch);
                if (Math.abs(p.pitch) > 1.0) anyPitched = true;
            }
        }
        assertTrue(anyPitched, "Random brains should be steering the pitch, not hopping");
    }

    @Test
    public void testFlyPhysicsIsFlyableByAHandWrittenAutopilot() {
        // Independent of learning: a simple PD controller must be able to thread gaps,
        // otherwise the physics would be too sluggish for any AI to succeed.
        Random rand = new Random(5);
        int totalPassed = 0;
        int runs = 10;
        for (int run = 0; run < runs; run++) {
            FlightBody body = new FlightBody();
            ArrayList<GamePanel.Obstacle> obstacles = new ArrayList<>();
            int frame = 0, spawnTimer = 0, passed = 0;
            boolean dead = false;
            while (!dead && frame < 4000) {
                frame++;
                spawnTimer++;
                if (spawnTimer >= AISimulation.spawnInterval(frame)) {
                    obstacles.add(new GamePanel.Obstacle(370, AISimulation.randomGapY(rand)));
                    spawnTimer = 0;
                }
                for (GamePanel.Obstacle o : obstacles) {
                    o.x -= AISimulation.OBSTACLE_SPEED;
                    if (!o.scored && o.x + Collision.PIPE_WIDTH < AISimulation.PLANE_X) {
                        o.scored = true;
                        passed++;
                    }
                }
                obstacles.removeIf(o -> o.x < -100);
                GamePanel.Obstacle next = null;
                for (GamePanel.Obstacle o : obstacles) {
                    if (o.x + Collision.PIPE_WIDTH > AISimulation.PLANE_X) { next = o; break; }
                }
                double target = next == null ? 100 : next.gapY - 12;
                body.stepFly(GamePanel.autopilotControl(body, body.y, body.velY, target));
                dead = body.y + AISimulation.PLANE_SIZE > 240;
                for (GamePanel.Obstacle o : obstacles) {
                    dead |= Collision.planeHitsObstacle(AISimulation.PLANE_X, body.y, AISimulation.PLANE_SIZE,
                        FlightMode.FLY.hitboxWidth, FlightMode.FLY.hitboxHeight, body.pitch, o);
                }
            }
            totalPassed += passed;
        }
        double avg = totalPassed / (double) runs;
        assertTrue(avg >= 8, "Autopilot should clear several obstacles on average, got " + avg);
    }
}
