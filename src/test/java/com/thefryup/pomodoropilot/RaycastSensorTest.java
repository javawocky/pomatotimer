package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class RaycastSensorTest {

    @Test
    public void testZeroPitchMatchesLegacyFan() {
        ArrayList<GamePanel.Obstacle> obstacles = new ArrayList<>();
        obstacles.add(new GamePanel.Obstacle(150, 100));
        RaycastSensor.RayResult[] legacy = RaycastSensor.castRays(92, 112, obstacles);
        RaycastSensor.RayResult[] pitched = RaycastSensor.castRays(92, 112, 0.0, obstacles);
        for (int i = 0; i < 9; i++) {
            assertEquals(legacy[i].distance, pitched[i].distance, 1e-12);
            assertEquals(legacy[i].endY, pitched[i].endY, 1e-12);
        }
    }

    @Test
    public void testRaysRotateWithPitch() {
        ArrayList<GamePanel.Obstacle> none = new ArrayList<>();
        double x = 92, y = 120;

        RaycastSensor.RayResult[] level = RaycastSensor.castRays(x, y, 0.0, none);
        RaycastSensor.RayResult[] noseUp = RaycastSensor.castRays(x, y, 20.0, none);
        RaycastSensor.RayResult[] noseDown = RaycastSensor.castRays(x, y, -20.0, none);

        assertEquals(y, level[0].endY, 1e-6, "Level forward ray is horizontal");
        double upAngle = Math.toDegrees(Math.atan2(y - noseUp[0].endY, noseUp[0].endX - x));
        double downAngle = Math.toDegrees(Math.atan2(y - noseDown[0].endY, noseDown[0].endX - x));
        assertEquals(20.0, upAngle, 0.5, "Forward ray should point 20 degrees up with the nose");
        assertEquals(-20.0, downAngle, 0.5, "Forward ray should point 20 degrees down with the nose");

        // Belly ray stays perpendicular to the fuselage: rotating the plane nose-up swings it forward
        assertTrue(noseUp[RaycastSensor.FLOOR_RAY].endX > x, "Belly ray should swing forward when nose is up");
        assertTrue(noseDown[RaycastSensor.FLOOR_RAY].endX < x, "Belly ray should swing aft when nose is down");
    }

    @Test
    public void testPitchedBellyRayStillSeesTheGround() {
        ArrayList<GamePanel.Obstacle> none = new ArrayList<>();
        RaycastSensor.RayResult[] rays = RaycastSensor.castRays(92, 215, -30.0, none);
        assertTrue(rays[RaycastSensor.FLOOR_RAY].hit, "Belly ray should detect the floor while pitched");
        assertTrue(rays[RaycastSensor.FLOOR_RAY].distance < 1.0);
    }

    @Test
    public void testPitchedForwardRayHitsObstacleItPointsAt() {
        ArrayList<GamePanel.Obstacle> obstacles = new ArrayList<>();
        // Top mountain peak at (175, 80). Level, the forward ray threads the gap; nose-up it climbs into the peak.
        obstacles.add(new GamePanel.Obstacle(150, 120));
        RaycastSensor.RayResult[] level = RaycastSensor.castRays(92, 110, 0.0, obstacles);
        RaycastSensor.RayResult[] climbing = RaycastSensor.castRays(92, 110, 30.0, obstacles);
        assertFalse(level[0].hit, "Level forward ray should pass through the gap");
        assertTrue(climbing[0].hit, "Nose-up forward ray should hit the top mountain");
        assertTrue(climbing[0].distance < level[0].distance,
            "Pitching toward the mountain should shorten the forward ray");
    }

    @Test
    public void testSteepDiveSeesTheGroundAheadInFlyMode() {
        ArrayList<GamePanel.Obstacle> none = new ArrayList<>();
        // At 85 degrees nose-down the belly ray points nearly backwards; the forward rays face the floor
        RaycastSensor.RayResult[] legacyBounds = RaycastSensor.castRays(92, 180, -85.0, none, false);
        RaycastSensor.RayResult[] flyBounds = RaycastSensor.castRays(92, 180, -85.0, none, true);
        assertFalse(legacyBounds[0].hit, "Without all-ray bounds the forward ray is blind to the floor");
        assertTrue(flyBounds[0].hit, "Fly-mode forward ray should see the floor it is diving at");
        assertTrue(flyBounds[0].distance < 0.5);
    }

    @Test
    public void testSensorHelperUsesAllRayBoundsOnlyInFlyMode() {
        ArrayList<GamePanel.Obstacle> none = new ArrayList<>();
        FlightBody body = new FlightBody();
        body.resetFlight(180);
        body.pitch = -85.0;
        RaycastSensor.RayResult[] fly = AISimulation.castSensors(92, 192, body, FlightMode.FLY, none);
        RaycastSensor.RayResult[] bouncy = AISimulation.castSensors(92, 192, body, FlightMode.BOUNCY, none);
        assertTrue(fly[0].hit, "Fly sensors rotate with pitch and see the floor");
        assertFalse(bouncy[0].hit, "Bouncy sensors ignore pitch and keep the original fan");
    }

    @Test
    public void testFlyFanSpans85UpTo85DownEvenly() {
        double[] a = RaycastSensor.FLY_RAY_ANGLES.clone();
        java.util.Arrays.sort(a);
        assertEquals(-85.0, a[0], 1e-9);
        assertEquals(85.0, a[a.length - 1], 1e-9);
        for (int i = 1; i < a.length; i++) {
            assertEquals(21.25, a[i] - a[i - 1], 1e-9, "Fly rays should be evenly spaced");
        }
        // Level plane: every ray is full length, so the outermost rays reach far above and below
        RaycastSensor.RayResult[] rays = RaycastSensor.castFlyRays(92, 120, 0.0, new ArrayList<>());
        assertTrue(rays[6].hit, "Steep downward ray should reach the floor from mid-screen");
        assertEquals(240.0, rays[6].endY, 1.0);
        assertTrue(rays[RaycastSensor.FLOOR_RAY].hit, "Down ray should reach the floor from mid-screen");
        assertTrue(rays[RaycastSensor.CEILING_RAY].hit, "Up ray should reach the ceiling from mid-screen");
    }
}
