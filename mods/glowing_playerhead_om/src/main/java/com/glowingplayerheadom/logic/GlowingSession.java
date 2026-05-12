package com.glowingplayerheadom.logic;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlowingSession {
    public static final double RADIUS = 50.0;
    public static final long DURATION_MS = 10L * 60 * 1000;

    public final UUID activatorId;
    public final long expiryMs;
    public final Set<UUID> affectedPlayerIds = new HashSet<>();

    public GlowingSession(UUID activatorId, long nowMs) {
        this.activatorId = activatorId;
        this.expiryMs = nowMs + DURATION_MS;
    }

    public boolean isExpired(long nowMs) {
        return nowMs >= expiryMs;
    }

    public long remainingMs(long nowMs) {
        return expiryMs - nowMs;
    }

    public boolean isWithinRadius(double ax, double ay, double az,
                                   double bx, double by, double bz) {
        double dx = ax - bx, dy = ay - by, dz = az - bz;
        return dx * dx + dy * dy + dz * dz <= RADIUS * RADIUS;
    }
}
