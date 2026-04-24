package com.rankedsmprank.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RankManagerTest {

    private RankManager manager;
    private final UUID playerA = UUID.randomUUID();
    private final UUID playerB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manager = new RankManager();
    }

    // --- processPvPKill ---

    @Test
    void noSwapWhenKillerHasBetterRank() {
        // Killer tier 1 (best) kills victim tier 5 → no swap (killer already better)
        var result = manager.processPvPKill(playerA, 1, playerB, 5);
        assertFalse(result.swapOccurred());
    }

    @Test
    void noSwapWhenSameTier() {
        var result = manager.processPvPKill(playerA, 3, playerB, 3);
        assertFalse(result.swapOccurred());
    }

    @Test
    void swapOccursWhenKillerHasWorseTier() {
        // Killer tier 9 (worst) kills victim tier 1 (best) → they swap
        var result = manager.processPvPKill(playerA, 9, playerB, 1);
        assertTrue(result.swapOccurred());
        assertEquals(playerA, result.killer());
        assertEquals(9, result.killerOldTier());
        assertEquals(1, result.killerNewTier());
        assertEquals(playerB, result.victim());
        assertEquals(1, result.victimOldTier());
        assertEquals(9, result.victimNewTier());
    }

    @Test
    void swapOccursWhenKillerOneRankBehind() {
        var result = manager.processPvPKill(playerA, 5, playerB, 4);
        assertTrue(result.swapOccurred());
        assertEquals(4, result.killerNewTier());
        assertEquals(5, result.victimNewTier());
    }

    @Test
    void noSwapWithInvalidKillerTier() {
        var result = manager.processPvPKill(playerA, 0, playerB, 3);
        assertFalse(result.swapOccurred());

        var result2 = manager.processPvPKill(playerA, -1, playerB, 3);
        assertFalse(result2.swapOccurred());
    }

    @Test
    void noSwapWithInvalidVictimTier() {
        var result = manager.processPvPKill(playerA, 5, playerB, 10);
        assertFalse(result.swapOccurred());
    }

    @Test
    void noSwapWhenVictimHasNoRank() {
        // victimTier -1 = unranked
        var result = manager.processPvPKill(playerA, 5, playerB, -1);
        assertFalse(result.swapOccurred());
    }

    // --- assignRanks ---

    @Test
    void emptyListProducesEmptyAssignment() {
        var result = manager.assignRanks(List.of(), new Random(0));
        assertTrue(result.assignments().isEmpty());
    }

    @Test
    void singlePlayerGetsRank1() {
        UUID p = UUID.randomUUID();
        var result = manager.assignRanks(List.of(p), new Random(0));
        assertEquals(1, result.assignments().size());
        assertEquals(1, result.assignments().get(p));
    }

    @Test
    void allPlayersGetUniqueRanks() {
        List<UUID> players = new ArrayList<>();
        for (int i = 0; i < 9; i++) players.add(UUID.randomUUID());

        var result = manager.assignRanks(players, new Random(42));
        assertEquals(9, result.assignments().size());
        Set<Integer> tiers = new HashSet<>(result.assignments().values());
        assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9), tiers);
    }

    @Test
    void fewerPlayersThanRanksAssignsOnlyLowestTiers() {
        List<UUID> players = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        var result = manager.assignRanks(players, new Random(7));
        assertEquals(3, result.assignments().size());
        for (int tier : result.assignments().values()) {
            assertTrue(tier >= 1 && tier <= 3, "Expected tier 1-3 but got " + tier);
        }
    }

    @Test
    void moreThan9PlayersOnlyAssigns9Ranks() {
        List<UUID> players = new ArrayList<>();
        for (int i = 0; i < 15; i++) players.add(UUID.randomUUID());

        var result = manager.assignRanks(players, new Random(1));
        assertEquals(9, result.assignments().size());
        Set<Integer> tiers = new HashSet<>(result.assignments().values());
        assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9), tiers);
    }

    @Test
    void assignmentsAreRandom() {
        List<UUID> players = new ArrayList<>();
        for (int i = 0; i < 9; i++) players.add(UUID.randomUUID());

        var r1 = manager.assignRanks(players, new Random(1));
        var r2 = manager.assignRanks(players, new Random(999));

        boolean anyDifference = false;
        for (UUID p : players) {
            Integer t1 = r1.assignments().get(p);
            Integer t2 = r2.assignments().get(p);
            if (t1 != null && t2 != null && !t1.equals(t2)) {
                anyDifference = true;
                break;
            }
        }
        assertTrue(anyDifference, "Different seeds should produce different assignments");
    }
}
