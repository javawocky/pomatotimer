package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FlightBodyTest {

    private FlightBody levelAt(double y) {
        FlightBody b = new FlightBody();
        b.resetFlight(y);
        return b;
    }

    @Test
    public void testPitchNeverExceeds85Degrees() {
        FlightBody up = levelAt(200);
        FlightBody down = levelAt(20);
        for (int i = 0; i < 300; i++) {
            up.stepFly(1.0);
            down.stepFly(-1.0);
            assertTrue(up.pitch <= 85.0 + 1e-9, "Nose-up pitch exceeded limit: " + up.pitch);
            assertTrue(down.pitch >= -85.0 - 1e-9, "Nose-down pitch exceeded limit: " + down.pitch);
        }
        assertEquals(85.0, FlightBody.MAX_PITCH_DEG, 1e-9, "Steep, but never fully vertical");
        assertEquals(85.0, up.pitch, 1e-9, "Sustained full stick should reach the 85 degree limit");
        assertEquals(-85.0, down.pitch, 1e-9);
    }

    @Test
    public void testStickInputIsClamped() {
        FlightBody a = levelAt(120);
        FlightBody b = levelAt(120);
        a.stepFly(1.0);
        b.stepFly(50.0);
        assertEquals(a.pitch, b.pitch, 1e-12, "Control beyond +/-1 should be clamped");
    }

    @Test
    public void testPitchRespondsGraduallyNotInstantly() {
        FlightBody b = levelAt(150);
        b.stepFly(1.0);
        assertTrue(b.pitch > 0 && b.pitch < FlightBody.PITCH_ACCEL + 1e-9,
            "One frame of full stick should apply only one step of pitch acceleration, got " + b.pitch);

        int framesTo30 = 1;
        while (b.pitch < 30.0 && framesTo30 < 200) {
            b.stepFly(1.0);
            framesTo30++;
        }
        int framesToLimit = framesTo30;
        while (b.pitch < FlightBody.MAX_PITCH_DEG && framesToLimit < 400) {
            b.stepFly(1.0);
            framesToLimit++;
        }
        assertTrue(framesTo30 >= 4, "Nose should ease up to 30 degrees over several frames, not snap (took " + framesTo30 + ")");
        assertTrue(framesToLimit >= 8 && framesToLimit <= 40,
            "Reaching full pitch should take a noticeable but agile time, took " + framesToLimit + " frames");
    }

    @Test
    public void testPitchHasInertiaAfterStickReleased() {
        FlightBody b = levelAt(150);
        for (int i = 0; i < 8; i++) b.stepFly(1.0);
        double pitchAtRelease = b.pitch;
        b.stepFly(0.0);
        assertTrue(b.pitch > pitchAtRelease, "Nose should keep rotating briefly after the stick is centred");
    }

    @Test
    public void testNoseUpClimbsAndNoseDownDescends() {
        FlightBody climber = levelAt(230);
        FlightBody diver = levelAt(50);
        for (int i = 0; i < 40; i++) {
            climber.stepFly(1.0);
            diver.stepFly(-1.0);
        }
        assertTrue(climber.y < 230 - 40, "Nose-up flight should climb, y=" + climber.y);
        assertTrue(climber.y > FlightBody.CEILING_Y, "Test needs headroom below the ceiling");
        assertTrue(climber.velY < 0, "Climbing plane should have upward velocity");
        assertTrue(diver.y > 50 + 40, "Nose-down flight should descend, y=" + diver.y);
        assertTrue(diver.velY > 0, "Diving plane should have downward velocity");
    }

    @Test
    public void testFlightPathLagsBehindNose() {
        // Dive, then pull up hard: the plane should keep sinking for a while (momentum/floatiness)
        FlightBody b = levelAt(40);
        for (int i = 0; i < 50; i++) b.stepFly(-1.0);
        double yAtPullUp = b.y;
        int framesStillSinking = 0;
        for (int i = 0; i < 60; i++) {
            double before = b.y;
            b.stepFly(1.0);
            if (b.y > before) framesStillSinking++;
        }
        assertTrue(framesStillSinking >= 10, "Plane should carry its descent briefly, only sank for " + framesStillSinking);
        assertTrue(b.y < yAtPullUp + 80, "Plane should eventually recover from the dive");
    }

    @Test
    public void testClimbingBleedsAirspeedAndDivingBuildsIt() {
        FlightBody climber = levelAt(200);
        FlightBody diver = levelAt(20);
        for (int i = 0; i < 60; i++) {
            climber.stepFly(1.0);
            diver.stepFly(-1.0);
        }
        assertTrue(climber.airspeed < FlightBody.CRUISE_SPEED, "Climb should cost airspeed: " + climber.airspeed);
        assertTrue(diver.airspeed > FlightBody.CRUISE_SPEED, "Dive should gain airspeed: " + diver.airspeed);
    }

    @Test
    public void testTrimmedLevelFlightHoldsAltitude() {
        FlightBody b = levelAt(120);
        for (int i = 0; i < 120; i++) b.stepFly(0.0);
        assertEquals(120, b.y, 1.0, "Level, centred-stick flight at cruise should hold altitude");
        assertEquals(0.0, b.pitch, 1e-9);
    }

    @Test
    public void testSteepDiveIsMuchFasterThanShallowDive() {
        FlightBody steep = levelAt(20);
        FlightBody shallow = levelAt(20);
        for (int i = 0; i < 40; i++) {
            steep.stepFly(-1.0);
            // Hold roughly 30 degrees nose down
            shallow.stepFly(shallow.pitch > -30 ? -1.0 : (shallow.pitchRate < 0 ? 1.0 : 0.0));
        }
        assertTrue(steep.velY > shallow.velY * 1.3,
            "A near-vertical dive should descend faster: steep=" + steep.velY + " shallow=" + shallow.velY);
    }

    @Test
    public void testCeilingClamp() {
        FlightBody b = levelAt(15);
        for (int i = 0; i < 100; i++) b.stepFly(1.0);
        assertEquals(FlightBody.CEILING_Y, b.y, 1e-9);
        assertTrue(b.velY >= 0, "Ceiling should stop upward motion");
    }

    @Test
    public void testBouncyPhysicsUnchanged() {
        FlightBody b = levelAt(100);
        b.stepBouncy(true);
        assertEquals(-4.0, b.velY, 1e-9, "Jump -4.5 plus one frame of 0.5 gravity");
        assertEquals(96.0, b.y, 1e-9);
        b.stepBouncy(false);
        assertEquals(-3.5, b.velY, 1e-9);
        assertEquals(0.0, b.pitch, 1e-9, "Bouncy mode never pitches");
    }
}
