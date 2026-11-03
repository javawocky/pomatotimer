package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TrainingCourseTest {

    @AfterEach
    public void restoreDefaults() {
        TrainingCourse.COURSE_LIFETIME = 0;
    }

    /** Record n obstacles spawned 30 frames apart starting at frame 100; gapY = 50 + i. */
    private TrainingCourse recordCourse(int n) {
        TrainingCourse c = new TrainingCourse();
        c.beginFresh();
        for (int i = 0; i < n; i++) c.record(50 + i, 100 + i * 30);
        return c;
    }

    private void passUpTo(TrainingCourse c, int lastIndexPassed) {
        GamePanel.Obstacle o = new GamePanel.Obstacle(0, 0);
        o.courseIndex = lastIndexPassed;
        c.markPassed(o);
    }

    /** Drain the replay, returning (frame, gapY, index) for each obstacle in order. */
    private List<int[]> replay(TrainingCourse c, int startFrame) {
        List<int[]> out = new ArrayList<>();
        for (int f = startFrame; c.isReplaying() && f < startFrame + 10000; f++) {
            GamePanel.Obstacle o;
            while ((o = c.nextReplay(f)) != null) out.add(new int[]{f, o.gapY, o.courseIndex});
        }
        return out;
    }

    @Test
    public void testRewindsFiveObstaclesBeforeTheCrash() {
        TrainingCourse c = recordCourse(20);
        passUpTo(c, 11);                 // passed 0..11, crashed into #12
        assertEquals(12, c.getFrontier());
        int start = c.beginRewound();
        assertEquals(7, c.getStartIndex(), "Restart 5 obstacles before the one that ended the run");
        assertEquals(100 + 7 * 30 - TrainingCourse.SETTLE_FRAMES, start, "Settle gap before the first obstacle");
    }

    @Test
    public void testReplayReproducesTheSameObstaclesAndSpacing() {
        TrainingCourse c = recordCourse(20);
        passUpTo(c, 11);
        int start = c.beginRewound();
        List<int[]> out = replay(c, start);
        assertEquals(13, out.size(), "Replays obstacles 7..19");
        for (int k = 0; k < out.size(); k++) {
            int idx = 7 + k;
            assertEquals(idx, out.get(k)[2]);
            assertEquals(50 + idx, out.get(k)[1], "Same gap height");
            assertEquals(100 + idx * 30, out.get(k)[0], "Same spawn frame, so same spacing and difficulty");
        }
        assertEquals(TrainingCourse.SETTLE_FRAMES, out.get(0)[0] - start, "Obstacle-free settle time first");
        assertFalse(c.isReplaying(), "After the recording, normal spawning takes over");
    }

    @Test
    public void testEarlyCrashRewindsToTheBeginning() {
        TrainingCourse c = recordCourse(10);
        passUpTo(c, 2);
        assertEquals(0, c.beginRewound());
        assertEquals(0, c.getStartIndex());
        assertEquals(10, replay(c, 0).size(), "Whole course replayed from the start");
    }

    @Test
    public void testNewObstaclesAreRecordedAfterTheReplay() {
        TrainingCourse c = recordCourse(10);
        passUpTo(c, 8);
        c.beginRewound();
        replay(c, 0);
        GamePanel.Obstacle extra = c.record(99, 500);
        assertEquals(10, extra.courseIndex);
        assertEquals(11, c.getRecordedCount());
    }

    @Test
    public void testStuckForMoreThan100RewindsStartsAFreshCourse() {
        TrainingCourse c = recordCourse(30);
        passUpTo(c, 19);                       // frontier 20
        c.beginRewound();                      // new best frontier: stuck count stays 0
        assertEquals(0, c.getStuckRewinds());
        for (int i = 1; i <= TrainingCourse.MAX_STUCK_REWINDS; i++) {
            passUpTo(c, 19);                   // never gets past #20
            c.beginRewound();
            assertEquals(i, c.getStuckRewinds());
            assertEquals(30, c.getRecordedCount(), "Still on the same course");
        }
        passUpTo(c, 19);
        assertEquals(0, c.beginRewound(), "Rewind 101 gives up: back to the beginning");
        assertEquals(0, c.getRecordedCount(), "A different journey - fresh course");
        assertEquals(0, c.getStuckRewinds(), "Stuck timer reset");
    }

    @Test
    public void testProgressResetsTheStuckTimer() {
        TrainingCourse c = recordCourse(40);
        passUpTo(c, 19);
        c.beginRewound();
        for (int i = 0; i < 50; i++) { passUpTo(c, 19); c.beginRewound(); }
        assertEquals(50, c.getStuckRewinds());
        passUpTo(c, 24);                       // finally gets further
        c.beginRewound();
        assertEquals(0, c.getStuckRewinds());
        assertEquals(20, c.getStartIndex());
    }

    @Test
    public void testOptionalCourseLifetime() {
        TrainingCourse.COURSE_LIFETIME = 3;
        TrainingCourse c = recordCourse(20);
        passUpTo(c, 10); c.beginRewound();
        passUpTo(c, 12); c.beginRewound();
        assertEquals(20, c.getRecordedCount());
        passUpTo(c, 14); c.beginRewound();
        assertEquals(0, c.getRecordedCount(), "Lifetime used up - fresh course");
    }

    @Test
    public void testSuperTrainingHeadlessRunsAndRewinds() {
        AISimulation sim = new AISimulation(FlightMode.FLY, 21, 3000, true);
        sim.runGenerations(15);
        assertEquals(16, sim.getEvolution().getGeneration());
        assertTrue(sim.getCourse().getRecordedCount() > 0);
    }
}
