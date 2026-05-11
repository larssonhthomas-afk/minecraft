package com.unbrokenchainability.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HitTrackerTest {

    private HitTracker tracker;
    private static final String ATTACKER = "attacker-uuid";
    private static final String TARGET_A = "target-a-uuid";
    private static final String TARGET_B = "target-b-uuid";

    @BeforeEach
    void setUp() {
        tracker = new HitTracker();
    }

    @Test
    void firstHitNoBonus() {
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        assertEquals(1, r.totalHits());
        assertEquals(0, r.bonusLevel());
        assertFalse(r.soundShouldPlay());
    }

    @Test
    void thirdHitStillNoBonus() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_A, 3000L);
        assertEquals(3, r.totalHits());
        assertEquals(0, r.bonusLevel());
        assertFalse(r.soundShouldPlay());
    }

    @Test
    void fourthHitStartsBonus() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        tracker.recordHit(ATTACKER, TARGET_A, 3000L);
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_A, 4000L);
        assertEquals(4, r.totalHits());
        assertEquals(1, r.bonusLevel());
        assertTrue(r.soundShouldPlay());
    }

    @Test
    void bonusLevelCapsAtTen() {
        for (int i = 0; i < 20; i++) {
            tracker.recordHit(ATTACKER, TARGET_A, 1000L + i * 100L);
        }
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_A, 3100L);
        assertEquals(10, r.bonusLevel());
    }

    @Test
    void targetChangeClearsChain() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        tracker.recordHit(ATTACKER, TARGET_A, 3000L);
        // Switch to different target
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_B, 4000L);
        assertEquals(1, r.totalHits());
        assertEquals(0, r.bonusLevel());
    }

    @Test
    void timeoutClearsChain() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        tracker.recordHit(ATTACKER, TARGET_A, 3000L);
        // 7001ms later = timeout
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_A, 3000L + HitTracker.TIMEOUT_MS + 1L);
        assertEquals(1, r.totalHits());
        assertEquals(0, r.bonusLevel());
    }

    @Test
    void justUnderTimeoutContinues() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        tracker.recordHit(ATTACKER, TARGET_A, 3000L);
        // 7000ms exactly should still count (boundary: >7000 breaks, <=7000 continues)
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_A, 3000L + HitTracker.TIMEOUT_MS);
        assertEquals(4, r.totalHits());
        assertEquals(1, r.bonusLevel());
    }

    @Test
    void resetClearsChain() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        tracker.recordHit(ATTACKER, TARGET_A, 3000L);
        tracker.recordHit(ATTACKER, TARGET_A, 4000L);
        tracker.resetChain(ATTACKER);
        HitTracker.HitResult r = tracker.recordHit(ATTACKER, TARGET_A, 5000L);
        assertEquals(1, r.totalHits());
        assertEquals(0, r.bonusLevel());
    }

    @Test
    void differentAttackersAreIndependent() {
        for (int i = 0; i < 5; i++) tracker.recordHit(ATTACKER, TARGET_A, 1000L + i * 100L);
        HitTracker.HitResult r = tracker.recordHit("other-attacker", TARGET_A, 1000L);
        assertEquals(1, r.totalHits());
        assertEquals(0, r.bonusLevel());
    }

    @Test
    void getBonusLevelReturnsZeroBeforeThreshold() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        assertEquals(0, tracker.getBonusLevel(ATTACKER));
    }

    @Test
    void getBonusLevelAfterThreshold() {
        for (int i = 0; i < 4; i++) tracker.recordHit(ATTACKER, TARGET_A, 1000L + i * 100L);
        assertEquals(1, tracker.getBonusLevel(ATTACKER));
    }

    @Test
    void resetNonExistentNoException() {
        assertDoesNotThrow(() -> tracker.resetChain("unknown"));
    }

    @Test
    void getBonusLevelUnknownPlayerReturnsZero() {
        assertEquals(0, tracker.getBonusLevel("nobody"));
    }

    @Test
    void getConsecutiveHitsTracksCorrectly() {
        tracker.recordHit(ATTACKER, TARGET_A, 1000L);
        tracker.recordHit(ATTACKER, TARGET_A, 2000L);
        assertEquals(2, tracker.getConsecutiveHits(ATTACKER));
    }

    @Test
    void getConsecutiveHitsUnknownReturnsZero() {
        assertEquals(0, tracker.getConsecutiveHits("nobody"));
    }
}
