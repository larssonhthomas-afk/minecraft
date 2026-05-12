package com.glowingplayerheadom.logic;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlowingSessionTest {
    private final UUID activatorId = UUID.randomUUID();
    private final long now = System.currentTimeMillis();

    @Test
    void sessionNotExpiredImmediately() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertFalse(session.isExpired(now));
        assertFalse(session.isExpired(now + GlowingSession.DURATION_MS - 1));
    }

    @Test
    void sessionExpiredAtDuration() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertTrue(session.isExpired(now + GlowingSession.DURATION_MS));
        assertTrue(session.isExpired(now + GlowingSession.DURATION_MS + 5000));
    }

    @Test
    void remainingMsEqualsFullDurationAtCreation() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertEquals(GlowingSession.DURATION_MS, session.remainingMs(now));
    }

    @Test
    void remainingMsDecreasesOverTime() {
        GlowingSession session = new GlowingSession(activatorId, now);
        long later = now + 30_000L;
        assertEquals(GlowingSession.DURATION_MS - 30_000L, session.remainingMs(later));
    }

    @Test
    void withinRadiusTrueForSamePoint() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertTrue(session.isWithinRadius(0, 0, 0, 0, 0, 0));
    }

    @Test
    void withinRadiusTrueJustInside() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertTrue(session.isWithinRadius(0, 0, 0, 49.9, 0, 0));
        assertTrue(session.isWithinRadius(0, 0, 0, 0, 49.9, 0));
        assertTrue(session.isWithinRadius(0, 0, 0, 0, 0, 49.9));
    }

    @Test
    void withinRadiusTrueOnBoundary() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertTrue(session.isWithinRadius(0, 0, 0, 50, 0, 0));
    }

    @Test
    void withinRadiusFalseJustOutside() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertFalse(session.isWithinRadius(0, 0, 0, 50.1, 0, 0));
        assertFalse(session.isWithinRadius(0, 0, 0, 0, 50.1, 0));
    }

    @Test
    void withinRadiusFalseWellOutside() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertFalse(session.isWithinRadius(0, 0, 0, 100, 0, 0));
        assertFalse(session.isWithinRadius(0, 0, 0, 0, 100, 100));
    }

    @Test
    void affectedPlayerIdsEmptyAtCreation() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertTrue(session.affectedPlayerIds.isEmpty());
    }

    @Test
    void expirySetToNowPlusDuration() {
        GlowingSession session = new GlowingSession(activatorId, now);
        assertEquals(now + GlowingSession.DURATION_MS, session.expiryMs);
    }
}
