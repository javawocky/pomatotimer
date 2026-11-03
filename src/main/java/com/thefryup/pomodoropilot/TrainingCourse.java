package com.thefryup.pomodoropilot;

import java.util.ArrayList;
import java.util.List;

/**
 * Remembers the obstacle course so super training mode can replay it.
 *
 * Every spawned obstacle is recorded with its gap height and the frame it spawned on.
 * In super training, when a generation ends the next one restarts a few obstacles before
 * the furthest point reached, replaying the exact same obstacles with the exact same
 * spacing (difficulty follows the frame counter, so that is restored too). Once the
 * recording runs out, normal random spawning carries on and keeps recording.
 */
public class TrainingCourse {
    /** How many obstacles before the crash point the next generation restarts. */
    public static final int REWIND_OBSTACLES = 5;
    /** Obstacle-free frames before the first replayed obstacle spawns, so planes can settle. */
    public static final int SETTLE_FRAMES = 60;
    /**
     * Rewinds allowed without getting past the current hardest obstacle. Beyond this the
     * obstacle is treated as impassable for now: back to the beginning on a fresh course.
     */
    public static final int MAX_STUCK_REWINDS = 100;
    /**
     * Optional cap on generations per recorded course (0 = off). Off by default; in testing,
     * replaying one course for long lets pilots memorise it, and a lifetime of 3 helped.
     */
    public static int COURSE_LIFETIME = 0;
    private int generationsOnCourse = 0;
    private int stuckRewinds = 0;
    private int bestFrontier = 0;

    private record Entry(int gapY, int spawnFrame) {}

    private final List<Entry> history = new ArrayList<>();
    private int replayIndex = 0;
    private int startIndex = 0;
    private int furthestPassed = -1;

    /** Start a brand-new random course (normal training, or a new work session). */
    public void beginFresh() {
        history.clear();
        replayIndex = 0;
        startIndex = 0;
        furthestPassed = -1;
        generationsOnCourse = 0;
        stuckRewinds = 0;
        bestFrontier = 0;
    }

    /**
     * Restart REWIND_OBSTACLES before the furthest obstacle reached, on the same course.
     * Starts a fresh course instead if the pilots have been stuck behind the same obstacle
     * for more than MAX_STUCK_REWINDS rewinds (or COURSE_LIFETIME is set and used up).
     * @return the frame counter value the run should start from
     */
    public int beginRewound() {
        int frontier = getFrontier();
        if (frontier > bestFrontier) {
            bestFrontier = frontier;   // new ground - the stuck timer starts over
            stuckRewinds = 0;
        } else {
            stuckRewinds++;
        }
        generationsOnCourse++;
        if (stuckRewinds > MAX_STUCK_REWINDS || (COURSE_LIFETIME > 0 && generationsOnCourse >= COURSE_LIFETIME)) {
            beginFresh();
            return 0;
        }
        startIndex = Math.max(0, frontier - REWIND_OBSTACLES);
        replayIndex = startIndex;
        furthestPassed = startIndex - 1;
        if (startIndex == 0 || startIndex >= history.size()) {
            return 0;
        }
        return Math.max(0, history.get(startIndex).spawnFrame() - SETTLE_FRAMES);
    }

    /** True while there are recorded obstacles still to replay. */
    public boolean isReplaying() {
        return replayIndex < history.size();
    }

    /** Next recorded obstacle if it is due on this frame, else null. */
    public GamePanel.Obstacle nextReplay(int frameCounter) {
        if (!isReplaying() || frameCounter < history.get(replayIndex).spawnFrame()) {
            return null;
        }
        Entry e = history.get(replayIndex);
        GamePanel.Obstacle obs = new GamePanel.Obstacle(320 + Collision.PIPE_WIDTH, e.gapY());
        obs.courseIndex = replayIndex;
        replayIndex++;
        return obs;
    }

    /** Record a freshly generated obstacle (call only when not replaying). */
    public GamePanel.Obstacle record(int gapY, int frameCounter) {
        history.add(new Entry(gapY, frameCounter));
        replayIndex = history.size();
        GamePanel.Obstacle obs = new GamePanel.Obstacle(320 + Collision.PIPE_WIDTH, gapY);
        obs.courseIndex = history.size() - 1;
        return obs;
    }

    /** Call when an obstacle scrolls past the planes. */
    public void markPassed(GamePanel.Obstacle obs) {
        if (obs.courseIndex >= 0) {
            furthestPassed = Math.max(furthestPassed, obs.courseIndex);
        }
    }

    /** Index of the first obstacle not yet passed in this run (the one that ended it). */
    public int getFrontier() {
        return Math.max(startIndex, furthestPassed + 1);
    }

    /** Course index the current run started from (0 = the beginning). */
    public int getStartIndex() {
        return startIndex;
    }

    public int getRecordedCount() {
        return history.size();
    }

    /** Rewinds so far without getting past the hardest obstacle reached on this course. */
    public int getStuckRewinds() {
        return stuckRewinds;
    }
}
