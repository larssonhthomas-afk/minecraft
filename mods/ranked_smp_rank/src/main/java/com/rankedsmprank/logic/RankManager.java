package com.rankedsmprank.logic;

import java.util.*;

/**
 * Pure-logic class: rank swap rules and random assignment.
 * No Minecraft imports — all inputs/outputs are plain Java types.
 */
public class RankManager {

    /**
     * Result of evaluating a PvP kill for a possible rank swap.
     * A swap occurs when a player with a WORSE rank (higher tier number)
     * kills a player with a BETTER rank (lower tier number).
     */
    public record SwapResult(
            boolean swapOccurred,
            UUID killer,
            int killerOldTier,
            int killerNewTier,
            UUID victim,
            int victimOldTier,
            int victimNewTier
    ) {
        public static SwapResult noSwap() {
            return new SwapResult(false, null, -1, -1, null, -1, -1);
        }
    }

    /** Result of a /rr random rank assignment. */
    public record AssignResult(Map<UUID, Integer> assignments) {}

    /**
     * Evaluate a PvP kill.
     * Lower tier number = better rank (Rank 1 is best).
     * Swap only if killerTier > victimTier, meaning the killer had a worse rank.
     */
    public SwapResult processPvPKill(UUID killer, int killerTier, UUID victim, int victimTier) {
        if (!RankDefinition.isValidTier(killerTier) || !RankDefinition.isValidTier(victimTier)) {
            return SwapResult.noSwap();
        }
        if (killerTier <= victimTier) {
            return SwapResult.noSwap();
        }
        return new SwapResult(true,
                killer, killerTier, victimTier,
                victim, victimTier, killerTier);
    }

    /**
     * Assign ranks 1..N to up to N online players where N = min(players, 9).
     * Clears all previous assignments; each rank goes to exactly one player.
     */
    public AssignResult assignRanks(List<UUID> onlinePlayers, Random random) {
        if (onlinePlayers.isEmpty()) return new AssignResult(Map.of());

        int count = Math.min(onlinePlayers.size(), 9);
        List<UUID> shuffled = new ArrayList<>(onlinePlayers);
        Collections.shuffle(shuffled, random);

        Map<UUID, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            result.put(shuffled.get(i), i + 1);
        }
        return new AssignResult(Collections.unmodifiableMap(result));
    }
}
