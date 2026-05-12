package com.glowingplayerheadom.logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlowingLogic {
    private final Map<UUID, GlowingSession> activeSessions = new HashMap<>();

    public GlowingSession createSession(UUID activatorId, long nowMs) {
        activeSessions.remove(activatorId);
        GlowingSession session = new GlowingSession(activatorId, nowMs);
        activeSessions.put(activatorId, session);
        return session;
    }

    public void removeSession(UUID activatorId) {
        activeSessions.remove(activatorId);
    }

    public GlowingSession getSession(UUID activatorId) {
        return activeSessions.get(activatorId);
    }

    public Map<UUID, GlowingSession> getActiveSessions() {
        return Collections.unmodifiableMap(activeSessions);
    }
}
