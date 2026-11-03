package com.thefryup.pomodoropilot;

/**
 * Plane-vs-mountain collision using the separating axis theorem, so the
 * plane's hitbox can rotate with its pitch.
 */
public final class Collision {
    static final int PIPE_WIDTH = 50;
    static final int PIPE_GAP = 80;
    static final int SCREEN_HEIGHT = 240;

    private Collision() {}

    /**
     * @param spriteX   left edge of the (unrotated) sprite box
     * @param spriteY   top edge of the (unrotated) sprite box
     * @param spriteSize sprite box size; the hitbox is centred inside it
     * @param pitchDeg  pitch in degrees, positive = nose up
     */
    public static boolean planeHitsObstacle(double spriteX, double spriteY, int spriteSize,
                                            int hitW, int hitH, double pitchDeg, GamePanel.Obstacle obs) {
        double[][] box = rotatedBox(spriteX + spriteSize / 2.0, spriteY + spriteSize / 2.0, hitW, hitH, pitchDeg);

        int topPeakY = obs.gapY - PIPE_GAP / 2;
        int bottomPeakY = obs.gapY + PIPE_GAP / 2;
        double peakX = obs.x + PIPE_WIDTH / 2.0;

        if (topPeakY > 0) {
            double[][] top = {{peakX, topPeakY}, {obs.x, 0}, {obs.x + PIPE_WIDTH, 0}};
            if (polygonsOverlap(box, top)) return true;
        }
        if (bottomPeakY < SCREEN_HEIGHT) {
            double[][] bottom = {{peakX, bottomPeakY}, {obs.x, SCREEN_HEIGHT}, {obs.x + PIPE_WIDTH, SCREEN_HEIGHT}};
            if (polygonsOverlap(box, bottom)) return true;
        }
        return false;
    }

    /** Corners of a w x h box centred on (cx, cy), rotated so that positive pitch lifts the nose. */
    static double[][] rotatedBox(double cx, double cy, double w, double h, double pitchDeg) {
        // Screen y points down, so nose-up is a negative screen rotation.
        double a = Math.toRadians(-pitchDeg);
        double cos = Math.cos(a), sin = Math.sin(a);
        double hw = w / 2.0, hh = h / 2.0;
        double[][] local = {{-hw, -hh}, {hw, -hh}, {hw, hh}, {-hw, hh}};
        double[][] out = new double[4][2];
        for (int i = 0; i < 4; i++) {
            out[i][0] = cx + local[i][0] * cos - local[i][1] * sin;
            out[i][1] = cy + local[i][0] * sin + local[i][1] * cos;
        }
        return out;
    }

    /** Convex polygon overlap test (SAT). Touching edges count as overlap. */
    static boolean polygonsOverlap(double[][] a, double[][] b) {
        return !hasSeparatingAxis(a, b) && !hasSeparatingAxis(b, a);
    }

    private static boolean hasSeparatingAxis(double[][] edgesFrom, double[][] other) {
        int n = edgesFrom.length;
        for (int i = 0; i < n; i++) {
            double[] p1 = edgesFrom[i];
            double[] p2 = edgesFrom[(i + 1) % n];
            double axisX = -(p2[1] - p1[1]);
            double axisY = p2[0] - p1[0];

            double minA = Double.MAX_VALUE, maxA = -Double.MAX_VALUE;
            for (double[] p : edgesFrom) {
                double proj = p[0] * axisX + p[1] * axisY;
                minA = Math.min(minA, proj);
                maxA = Math.max(maxA, proj);
            }
            double minB = Double.MAX_VALUE, maxB = -Double.MAX_VALUE;
            for (double[] p : other) {
                double proj = p[0] * axisX + p[1] * axisY;
                minB = Math.min(minB, proj);
                maxB = Math.max(maxB, proj);
            }
            if (maxA < minB || maxB < minA) return true;
        }
        return false;
    }
}
