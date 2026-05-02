package com.admintooloperatorer.logic;

import java.util.Map;
import java.util.UUID;

/**
 * Pure logic for /rank set: finds an existing rank holder and computes the transfer.
 */
public final class RankSetLogic {

    public record RankSetResult(UUID previousHolder, UUID newHolder, int tier) {}

    private RankSetLogic() {}

    /**
     * Parses a rank string like "R1", "r5", or "3" into a tier integer.
     * Returns -1 if parsing fails.
     */
    public static int parseTier(String rankArg) {
        if (rankArg == null) return -1;
        String s = rankArg.trim();
        if (s.toUpperCase().startsWith("R")) s = s.substring(1);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Determines who (if anyone) currently holds {@code tier} and must be displaced.
     * If target already holds the rank, no displacement occurs.
     */
    public static RankSetResult compute(Map<UUID, Integer> currentTiers, UUID target, int tier) {
        UUID previous = null;
        for (Map.Entry<UUID, Integer> entry : currentTiers.entrySet()) {
            if (entry.getValue() == tier && !entry.getKey().equals(target)) {
                previous = entry.getKey();
                break;
            }
        }
        return new RankSetResult(previous, target, tier);
    }
}
