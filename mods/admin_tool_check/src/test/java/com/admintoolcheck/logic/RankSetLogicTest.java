package com.admintoolcheck.logic;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankSetLogicTest {

    @Test
    void parseTier_withRPrefix() {
        assertEquals(1, RankSetLogic.parseTier("R1"));
        assertEquals(5, RankSetLogic.parseTier("R5"));
        assertEquals(9, RankSetLogic.parseTier("R9"));
    }

    @Test
    void parseTier_lowercase() {
        assertEquals(3, RankSetLogic.parseTier("r3"));
    }

    @Test
    void parseTier_numberOnly() {
        assertEquals(7, RankSetLogic.parseTier("7"));
    }

    @Test
    void parseTier_null_returnsMinusOne() {
        assertEquals(-1, RankSetLogic.parseTier(null));
    }

    @Test
    void parseTier_emptyString_returnsMinusOne() {
        assertEquals(-1, RankSetLogic.parseTier(""));
    }

    @Test
    void parseTier_nonNumericAfterR_returnsMinusOne() {
        assertEquals(-1, RankSetLogic.parseTier("Rabc"));
    }

    @Test
    void parseTier_whitespace_returnsMinusOne() {
        assertEquals(-1, RankSetLogic.parseTier("  "));
    }

    @Test
    void compute_noExistingHolders_noPreviousHolder() {
        UUID target = UUID.randomUUID();
        RankSetLogic.RankSetResult result = RankSetLogic.compute(new HashMap<>(), target, 3);
        assertNull(result.previousHolder());
        assertEquals(target, result.newHolder());
        assertEquals(3, result.tier());
    }

    @Test
    void compute_otherPlayerHoldsTier_displacesHolder() {
        UUID target = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Map<UUID, Integer> tiers = new HashMap<>();
        tiers.put(other, 3);
        RankSetLogic.RankSetResult result = RankSetLogic.compute(tiers, target, 3);
        assertEquals(other, result.previousHolder());
        assertEquals(target, result.newHolder());
        assertEquals(3, result.tier());
    }

    @Test
    void compute_targetAlreadyHoldsTier_noPreviousHolder() {
        UUID target = UUID.randomUUID();
        Map<UUID, Integer> tiers = new HashMap<>();
        tiers.put(target, 5);
        RankSetLogic.RankSetResult result = RankSetLogic.compute(tiers, target, 5);
        assertNull(result.previousHolder());
        assertEquals(target, result.newHolder());
        assertEquals(5, result.tier());
    }

    @Test
    void compute_multiplePlayersOnlyDisplacesMatchingTier() {
        UUID target = UUID.randomUUID();
        UUID holder2 = UUID.randomUUID();
        UUID holder4 = UUID.randomUUID();
        Map<UUID, Integer> tiers = new HashMap<>();
        tiers.put(holder2, 2);
        tiers.put(holder4, 4);
        RankSetLogic.RankSetResult result = RankSetLogic.compute(tiers, target, 4);
        assertEquals(holder4, result.previousHolder());
        assertEquals(target, result.newHolder());
    }

    @Test
    void compute_tierNotHeldByAnyone_noPreviousHolder() {
        UUID target = UUID.randomUUID();
        Map<UUID, Integer> tiers = new HashMap<>();
        tiers.put(UUID.randomUUID(), 1);
        tiers.put(UUID.randomUUID(), 2);
        RankSetLogic.RankSetResult result = RankSetLogic.compute(tiers, target, 9);
        assertNull(result.previousHolder());
        assertEquals(target, result.newHolder());
        assertEquals(9, result.tier());
    }

    @Test
    void parseTier_leadingTrailingSpaces_parsed() {
        assertEquals(2, RankSetLogic.parseTier("  R2  "));
    }
}
