package com.combatenchantcustom.logic;

import java.util.HashMap;
import java.util.Map;

public final class HitTracker {

    public static final int MIN_HITS_FOR_BONUS = 3;
    public static final int MAX_BONUS_LEVELS = 10;

    private final Map<String, Integer> consecutiveHits = new HashMap<>();

    public record HitResult(int totalHits, int bonusLevel, boolean soundShouldPlay) {}

    public HitResult recordHit(String attackerId) {
        int hits = consecutiveHits.getOrDefault(attackerId, 0) + 1;
        consecutiveHits.put(attackerId, hits);

        int bonusLevel = 0;
        if (hits > MIN_HITS_FOR_BONUS) {
            bonusLevel = Math.min(hits - MIN_HITS_FOR_BONUS, MAX_BONUS_LEVELS);
        }

        return new HitResult(hits, bonusLevel, hits > MIN_HITS_FOR_BONUS);
    }

    public void resetChain(String attackerId) {
        consecutiveHits.remove(attackerId);
    }

    public int getBonusLevel(String attackerId) {
        int hits = consecutiveHits.getOrDefault(attackerId, 0);
        if (hits <= MIN_HITS_FOR_BONUS) return 0;
        return Math.min(hits - MIN_HITS_FOR_BONUS, MAX_BONUS_LEVELS);
    }

    public int getConsecutiveHits(String attackerId) {
        return consecutiveHits.getOrDefault(attackerId, 0);
    }
}
