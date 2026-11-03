package com.thefryup.pomodoropilot;

/**
 * Vertical flight state of a plane plus the physics for both flight modes.
 *
 * Conventions: y grows downward (screen space), velY > 0 means falling,
 * pitch > 0 means nose UP (degrees). Units are pixels / frames.
 *
 * FLY mode is deliberately arcade: the stick drives pitch through angular
 * inertia, the flight path lags behind the nose (floatiness), climbing bleeds
 * airspeed and diving builds it, and lift fades when slow so gravity wins.
 */
public class FlightBody {
    // --- Fly mode tuning ---
    public static final double MAX_PITCH_DEG = 85.0;
    /** Pitch acceleration at full stick (deg/frame^2). */
    static final double PITCH_ACCEL = 1.60;
    /** Angular drag on pitch rate - caps rate at ~PITCH_ACCEL/PITCH_DAMPING deg/frame. */
    static final double PITCH_DAMPING = 0.10;
    /** Steady-state top pitch rate at full stick (deg/frame). */
    public static final double MAX_PITCH_RATE = PITCH_ACCEL / PITCH_DAMPING;
    /** Weak self-levelling tendency when the stick is centred. */
    static final double PITCH_STABILITY = 0.004;
    static final double CRUISE_SPEED = 5.0;
    static final double MIN_SPEED = 3.0;
    static final double MAX_SPEED = 7.0;
    /** Airspeed lost per frame per unit sin(pitch) when climbing (gained when diving). */
    static final double SPEED_GRAVITY = 0.03;
    /** How quickly the engine pulls airspeed back to cruise. */
    static final double THRUST_RESPONSE = 0.02;
    /** How quickly the flight path follows the nose (lower = floatier). */
    static final double PATH_RESPONSE = 0.15;
    /** Gravity that lift cancels out at cruise speed. */
    static final double FLY_GRAVITY = 0.10;

    // --- Bouncy mode tuning (original values) ---
    static final double GRAVITY = 0.5;
    static final double JUMP_STRENGTH = -4.5;

    public static final double CEILING_Y = 10;

    public double y = 100;
    public double velY = 0;
    public double pitch = 0;
    public double pitchRate = 0;
    public double airspeed = CRUISE_SPEED;

    public void resetFlight(double startY) {
        y = startY;
        velY = 0;
        pitch = 0;
        pitchRate = 0;
        airspeed = CRUISE_SPEED;
    }

    /**
     * Advance one frame of pitch-controlled flight.
     * @param control stick input in [-1, 1]; positive pulls the nose up.
     */
    public void stepFly(double control) {
        control = Math.max(-1.0, Math.min(1.0, control));

        // Pitch has inertia: the stick accelerates the pitch rate rather than setting the angle.
        pitchRate += control * PITCH_ACCEL - pitchRate * PITCH_DAMPING - pitch * PITCH_STABILITY;
        pitch += pitchRate;
        if (pitch > MAX_PITCH_DEG) {
            pitch = MAX_PITCH_DEG;
            if (pitchRate > 0) pitchRate = 0;
        } else if (pitch < -MAX_PITCH_DEG) {
            pitch = -MAX_PITCH_DEG;
            if (pitchRate < 0) pitchRate = 0;
        }

        double sinPitch = Math.sin(Math.toRadians(pitch));

        // Energy: climbing trades speed for height, diving the reverse; the engine nudges back to cruise.
        airspeed += -sinPitch * SPEED_GRAVITY + (CRUISE_SPEED - airspeed) * THRUST_RESPONSE;
        airspeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, airspeed));

        // Lift fades below cruise speed, letting gravity pull the plane down (a gentle mush/stall).
        double speedRatio = airspeed / CRUISE_SPEED;
        double lift = Math.min(1.0, speedRatio * speedRatio);

        // The flight path chases the nose direction with lag - this is the floatiness/inertia.
        double pathVelY = -airspeed * sinPitch;
        velY += (pathVelY - velY) * PATH_RESPONSE + FLY_GRAVITY * (1.0 - lift);
        y += velY;

        clampToCeiling();
    }

    /** Advance one frame of classic hop physics. */
    public void stepBouncy(boolean jump) {
        if (jump) {
            velY = JUMP_STRENGTH;
        }
        velY += GRAVITY;
        y += velY;
        clampToCeiling();
    }

    private void clampToCeiling() {
        if (y < CEILING_Y) {
            y = CEILING_Y;
            velY = 0;
        }
    }
}
