package com.heavenlynr.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeavenlyLogicTest {

    @Test
    void wouldSave_withAbilityAndNoCooldown_returnsTrue() {
        assertTrue(HeavenlyLogic.wouldSave(true, false));
    }

    @Test
    void wouldSave_withAbilityAndCooldownActive_returnsFalse() {
        assertFalse(HeavenlyLogic.wouldSave(true, true));
    }

    @Test
    void wouldSave_withoutAbility_returnsFalse() {
        assertFalse(HeavenlyLogic.wouldSave(false, false));
    }

    @Test
    void wouldSave_withoutAbilityAndWithCooldown_returnsFalse() {
        assertFalse(HeavenlyLogic.wouldSave(false, true));
    }

    @Test
    void formatCooldown_fullCooldown_formatsCorrectly() {
        assertEquals("20:00", HeavenlyLogic.formatCooldown(20L * 60 * 1000));
    }

    @Test
    void formatCooldown_oneMinute_formatsCorrectly() {
        assertEquals("1:00", HeavenlyLogic.formatCooldown(60_000));
    }

    @Test
    void formatCooldown_thirtySeconds_formatsCorrectly() {
        assertEquals("0:30", HeavenlyLogic.formatCooldown(30_000));
    }

    @Test
    void formatCooldown_oneSecond_formatsCorrectly() {
        assertEquals("0:01", HeavenlyLogic.formatCooldown(1_000));
    }

    @Test
    void formatCooldown_partialSecond_roundsUp() {
        // 500 ms → rounds up to 1 second
        assertEquals("0:01", HeavenlyLogic.formatCooldown(500));
    }

    @Test
    void formatCooldown_zero_returnsZero() {
        assertEquals("0:00", HeavenlyLogic.formatCooldown(0));
    }

    @Test
    void formatCooldown_negative_returnsZero() {
        assertEquals("0:00", HeavenlyLogic.formatCooldown(-1000));
    }

    @Test
    void formatCooldown_tenMinutesFiftyNineSeconds_formatsCorrectly() {
        assertEquals("10:59", HeavenlyLogic.formatCooldown(10L * 60 * 1000 + 59 * 1000));
    }

    @Test
    void cooldownConstant_isTwentyMinutes() {
        assertEquals(20L * 60 * 1000, HeavenlyLogic.COOLDOWN_MS);
    }

    @Test
    void helmetDamageFraction_isFortyPercent() {
        assertEquals(0.40f, HeavenlyLogic.HELMET_DAMAGE_FRACTION, 0.0001f);
    }

    @Test
    void bookDisplayName_isHeavenly() {
        assertEquals("Heavenly", HeavenlyLogic.BOOK_DISPLAY_NAME);
    }
}
