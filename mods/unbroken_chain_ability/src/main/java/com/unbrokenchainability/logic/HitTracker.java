package com.unbrokenchainability.logic;

import java.util.HashMap;
import java.util.Map;

public final class HitTracker {

    public static final int MIN_HITS_FOR_BONUS = 3;
    public static final int MAX_BONUS_LEVELS = 10;
    public static final long TIMEOUT_MS = 7000L;

    private record HitState(String targetId, int hits, long lastHitMs) {}

    private final Map<String, HitState> states = new HashMap<>();

    public record HitResult(int totalHits, int bonusLevel, boolean soundShouldPlay) {}

    public HitResult recordHit(String attackerId, String targetId, long nowMs) {
        HitState prev = states.get(attackerId);

        int newHits;
        if (prev == null
                || !targetId.equals(prev.targetId())
                || (nowMs - prev.lastHitMs()) > TIMEOUT_MS) {
            newHits = 1;
        } else {
            newHits = prev.hits() + 1;
        }

        states.put(attackerId, new HitState(targetId, newHits, nowMs));

        int bonusLevel = 0;
        if (newHits > MIN_HITS_FOR_BONUS) {
            bonusLevel = Math.min(newHits - MIN_HITS_FOR_BONUS, MAX_BONUS_LEVELS);
        }

        return new HitResult(newHits, bonusLevel, newHits > MIN_HITS_FOR_BONUS);
    }

    public void resetChain(String attackerId) {
        states.remove(attackerId);
    }

    public int getBonusLevel(String attackerId) {
        HitState state = states.get(attackerId);
        if (state == null) return 0;
        int hits = state.hits();
        if (hits <= MIN_HITS_FOR_BONUS) return 0;
        return Math.min(hits - MIN_HITS_FOR_BONUS, MAX_BONUS_LEVELS);
    }

    public int getConsecutiveHits(String attackerId) {
        HitState state = states.get(attackerId);
        return state == null ? 0 : state.hits();
    }
}
