package com.glowingplayerheadom.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlowingLogicTest {
    private GlowingLogic logic;
    private final UUID activatorId = UUID.randomUUID();
    private final long now = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        logic = new GlowingLogic();
    }

    @Test
    void createSessionAddsToActiveSessions() {
        GlowingSession session = logic.createSession(activatorId, now);
        assertNotNull(session);
        assertEquals(session, logic.getSession(activatorId));
        assertEquals(1, logic.getActiveSessions().size());
    }

    @Test
    void getSessionReturnsNullForUnknown() {
        assertNull(logic.getSession(UUID.randomUUID()));
    }

    @Test
    void removeSessionClearsEntry() {
        logic.createSession(activatorId, now);
        logic.removeSession(activatorId);
        assertNull(logic.getSession(activatorId));
        assertTrue(logic.getActiveSessions().isEmpty());
    }

    @Test
    void removeSessionOnNonExistentIsNoop() {
        assertDoesNotThrow(() -> logic.removeSession(UUID.randomUUID()));
    }

    @Test
    void createSessionReplacesExistingSession() {
        GlowingSession first = logic.createSession(activatorId, now);
        GlowingSession second = logic.createSession(activatorId, now + 1000);
        assertNotSame(first, second);
        assertEquals(second, logic.getSession(activatorId));
        assertEquals(1, logic.getActiveSessions().size());
    }

    @Test
    void multipleActivatorsHaveIndependentSessions() {
        UUID other = UUID.randomUUID();
        logic.createSession(activatorId, now);
        logic.createSession(other, now);
        assertEquals(2, logic.getActiveSessions().size());
        assertNotNull(logic.getSession(activatorId));
        assertNotNull(logic.getSession(other));
    }

    @Test
    void removeOneSessionLeavesOtherIntact() {
        UUID other = UUID.randomUUID();
        logic.createSession(activatorId, now);
        logic.createSession(other, now);
        logic.removeSession(activatorId);
        assertNull(logic.getSession(activatorId));
        assertNotNull(logic.getSession(other));
        assertEquals(1, logic.getActiveSessions().size());
    }

    @Test
    void getActiveSessionsIsUnmodifiable() {
        logic.createSession(activatorId, now);
        assertThrows(UnsupportedOperationException.class,
                () -> logic.getActiveSessions().put(UUID.randomUUID(), null));
    }

    @Test
    void sessionExpirySetCorrectly() {
        GlowingSession session = logic.createSession(activatorId, now);
        assertEquals(now + GlowingSession.DURATION_MS, session.expiryMs);
    }

    @Test
    void activeSessionsEmptyInitially() {
        assertTrue(logic.getActiveSessions().isEmpty());
    }
}
