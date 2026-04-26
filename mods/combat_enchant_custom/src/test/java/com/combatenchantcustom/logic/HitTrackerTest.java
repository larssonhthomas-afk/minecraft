package com.combatenchantcustom.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HitTrackerTest {

    private HitTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new HitTracker();
    }

    @Test
    void firstHitNoBonus() {
        HitTracker.HitResult result = tracker.recordHit("player1");
        assertEquals(1, result.totalHits());
        assertEquals(0, result.bonusLevel());
        assertFalse(result.soundShouldPlay());
    }

    @Test
    void secondHitNoBonus() {
        tracker.recordHit("player1");
        HitTracker.HitResult result = tracker.recordHit("player1");
        assertEquals(2, result.totalHits());
        assertEquals(0, result.bonusLevel());
        assertFalse(result.soundShouldPlay());
    }

    @Test
    void thirdHitStillNoBonus() {
        tracker.recordHit("player1");
        tracker.recordHit("player1");
        HitTracker.HitResult result = tracker.recordHit("player1");
        assertEquals(3, result.totalHits());
        assertEquals(0, result.bonusLevel());
        assertFalse(result.soundShouldPlay());
    }

    @Test
    void fourthHitStartsBonus() {
        tracker.recordHit("player1");
        tracker.recordHit("player1");
        tracker.recordHit("player1");
        HitTracker.HitResult result = tracker.recordHit("player1");
        assertEquals(4, result.totalHits());
        assertEquals(1, result.bonusLevel());
        assertTrue(result.soundShouldPlay());
    }

    @Test
    void bonusLevelIncrementsEachHit() {
        for (int i = 0; i < 7; i++) tracker.recordHit("player1");
        HitTracker.HitResult result = tracker.recordHit("player1");
        assertEquals(8, result.totalHits());
        assertEquals(5, result.bonusLevel());
        assertTrue(result.soundShouldPlay());
    }

    @Test
    void bonusLevelCapsAtTen() {
        for (int i = 0; i < 20; i++) tracker.recordHit("player1");
        HitTracker.HitResult result = tracker.recordHit("player1");
        assertEquals(10, result.bonusLevel());
    }

    @Test
    void bonusLevelStaysAtTenBeyondMax() {
        for (int i = 0; i < 30; i++) tracker.recordHit("player1");
        assertEquals(10, tracker.getBonusLevel("player1"));
    }

    @Test
    void resetClearsChain() {
        tracker.recordHit("player1");
        tracker.recordHit("player1");
        tracker.recordHit("player1");
        tracker.recordHit("player1");
        tracker.resetChain("player1");
        HitTracker.HitResult result = tracker.recordHit("player1");
        assertEquals(1, result.totalHits());
        assertEquals(0, result.bonusLevel());
        assertFalse(result.soundShouldPlay());
    }

    @Test
    void differentAttackersAreIndependent() {
        for (int i = 0; i < 5; i++) tracker.recordHit("player1");
        HitTracker.HitResult r2 = tracker.recordHit("player2");
        assertEquals(1, r2.totalHits());
        assertEquals(0, r2.bonusLevel());
    }

    @Test
    void getBonusLevelBeforeMinHits() {
        tracker.recordHit("player1");
        tracker.recordHit("player1");
        assertEquals(0, tracker.getBonusLevel("player1"));
    }

    @Test
    void getBonusLevelAfterMinHits() {
        for (int i = 0; i < 4; i++) tracker.recordHit("player1");
        assertEquals(1, tracker.getBonusLevel("player1"));
    }

    @Test
    void resetNonExistentPlayerNoException() {
        assertDoesNotThrow(() -> tracker.resetChain("unknown-player"));
    }

    @Test
    void getBonusLevelUnknownPlayer() {
        assertEquals(0, tracker.getBonusLevel("nonexistent"));
    }

    @Test
    void getConsecutiveHitsReturnsZeroForUnknown() {
        assertEquals(0, tracker.getConsecutiveHits("nobody"));
    }

    @Test
    void getConsecutiveHitsTracksCorrectly() {
        tracker.recordHit("p1");
        tracker.recordHit("p1");
        assertEquals(2, tracker.getConsecutiveHits("p1"));
    }
}
