package com.thefryup.pomodoropilot;

/**
 * How planes are controlled.
 * FLY    - pitch-controlled flight with inertia (default).
 * BOUNCY - classic Flappy Bird style hops.
 */
public enum FlightMode {
    FLY("Fly", 24, 20),
    BOUNCY("Bouncy", 24, 24);

    private final String label;
    /** Collision box size (centred on the sprite, rotated with pitch in FLY mode). */
    public final int hitboxWidth;
    public final int hitboxHeight;

    FlightMode(String label, int hitboxWidth, int hitboxHeight) {
        this.label = label;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
    }

    public String label() {
        return label;
    }

    public FlightMode toggle() {
        return this == FLY ? BOUNCY : FLY;
    }
}
