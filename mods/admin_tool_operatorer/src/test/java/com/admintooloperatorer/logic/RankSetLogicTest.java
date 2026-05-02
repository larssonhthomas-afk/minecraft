package com.admintooloperatorer.logic;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankSetLogicTest {

    @Test
    void compute_noExistingHolder() {
        UUID target = UUID.randomUUID();
        Map<UUID, Integer> tiers = Map.of(UUID.randomUUID(), 2, UUID.randomUUID(), 3);
        RankSetLogic.RankSetResult result = RankSetLogic.compute(tiers, target, 1);
        assertNull(result.previousHolder());
        assertEquals(target, result.newHolder());
        assertEquals(1, result.tier());
    }

    @Test
    void compute_rankAlreadyHeldByOther() {
        UUID existing = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        Map<UUID, Integer> tiers = Map.of(existing, 1, UUID.randomUUID(), 3);
        RankSetLogic.RankSetResult result = RankSetLogic.compute(tiers, target, 1);
        assertEquals(existing, result.previousHolder());
        assertEquals(target, result.newHolder());
        assertEquals(1, result.tier());
    }

    @Test
    void compute_assigningToCurrentHolder_noPreviousHolder() {
        UUID target = UUID.randomUUID();
        Map<UUID, Integer> tiers = Map.of(target, 1);
        RankSetLogic.RankSetResult result = RankSetLogic.compute(tiers, target, 1);
        assertNull(result.previousHolder());
        assertEquals(target, result.newHolder());
    }

    @Test
    void compute_emptyTierMap() {
        UUID target = UUID.randomUUID();
        RankSetLogic.RankSetResult result = RankSetLogic.compute(Map.of(), target, 5);
        assertNull(result.previousHolder());
        assertEquals(target, result.newHolder());
        assertEquals(5, result.tier());
    }

    @Test
    void parseTier_acceptsRPrefix() {
        assertEquals(1, RankSetLogic.parseTier("R1"));
        assertEquals(9, RankSetLogic.parseTier("R9"));
        assertEquals(5, RankSetLogic.parseTier("r5"));
        assertEquals(3, RankSetLogic.parseTier("R3"));
    }

    @Test
    void parseTier_acceptsNumberOnly() {
        assertEquals(1, RankSetLogic.parseTier("1"));
        assertEquals(9, RankSetLogic.parseTier("9"));
        assertEquals(7, RankSetLogic.parseTier("7"));
    }

    @Test
    void parseTier_invalidInputReturnsNegativeOne() {
        assertEquals(-1, RankSetLogic.parseTier("abc"));
        assertEquals(-1, RankSetLogic.parseTier(null));
        assertEquals(-1, RankSetLogic.parseTier("R"));
        assertEquals(-1, RankSetLogic.parseTier(""));
        assertEquals(-1, RankSetLogic.parseTier("R0x"));
    }

    @Test
    void parseTier_leadingAndTrailingWhitespace() {
        assertEquals(2, RankSetLogic.parseTier("  R2  "));
        assertEquals(4, RankSetLogic.parseTier("  4  "));
    }
}
