package com.lifesteal.logic;

import com.lifesteal.logic.HeartManager.TransferResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeartManagerTest {

    private static final float EPS = 1e-4f;

    // -- Konstruktor-validering -------------------------------------------------

    @Test
    void constructor_rejectsNegativeMinHealth() {
        assertThrows(IllegalArgumentException.class,
                () -> new HeartManager(-1.0f, 40.0f, 1.0f));
    }

    @Test
    void constructor_rejectsMaxEqualToMin() {
        assertThrows(IllegalArgumentException.class,
                () -> new HeartManager(10.0f, 10.0f, 1.0f));
    }

    @Test
    void constructor_rejectsMaxBelowMin() {
        assertThrows(IllegalArgumentException.class,
                () -> new HeartManager(10.0f, 5.0f, 1.0f));
    }

    @Test
    void constructor_rejectsZeroHeartsToSteal() {
        assertThrows(IllegalArgumentException.class,
                () -> new HeartManager(4.0f, 40.0f, 0.0f));
    }

    @Test
    void constructor_rejectsNegativeHeartsToSteal() {
        assertThrows(IllegalArgumentException.class,
                () -> new HeartManager(4.0f, 40.0f, -1.0f));
    }

    @Test
    void constructor_rejectsNaN() {
        assertThrows(IllegalArgumentException.class,
                () -> new HeartManager(Float.NaN, 40.0f, 1.0f));
    }

    @Test
    void constructor_rejectsInfinity() {
        assertThrows(IllegalArgumentException.class,
                () -> new HeartManager(4.0f, Float.POSITIVE_INFINITY, 1.0f));
    }

    @Test
    void constructor_acceptsValidDefaults() {
        HeartManager hm = HeartManager.createDefault();
        assertEquals(4.0f, hm.getMinHealth(), EPS);
        assertEquals(40.0f, hm.getMaxHealth(), EPS);
        assertEquals(1.0f, hm.getHeartsToSteal(), EPS);
    }

    // -- Konstanter -------------------------------------------------------------

    @Test
    void healthPerHeart_isTwo() {
        assertEquals(2.0f, HeartManager.HEALTH_PER_HEART, EPS);
    }

    // -- transferHearts: vanlig PvP-kill ---------------------------------------

    @Test
    void transfer_normalKill_movesOneHeart() {
        HeartManager hm = HeartManager.createDefault();
        TransferResult r = hm.transferHearts(20.0f, 20.0f);
        assertEquals(22.0f, r.newKillerMax(), EPS);
        assertEquals(18.0f, r.newVictimMax(), EPS);
        assertEquals(2.0f, r.heartsTransferredHp(), EPS);
        assertEquals(1.0f, r.heartsTransferred(), EPS);
        assertTrue(r.transferOccurred());
        assertFalse(r.victimEliminated());
    }

    // -- transferHearts: offer redan på min -> elimineras ----------------------

    @Test
    void transfer_victimAtMin_dropsBelowAndIsEliminated() {
        HeartManager hm = HeartManager.createDefault();
        TransferResult r = hm.transferHearts(20.0f, 4.0f);
        assertEquals(22.0f, r.newKillerMax(), EPS);
        assertEquals(2.0f, r.newVictimMax(), EPS);
        assertTrue(r.transferOccurred());
        assertTrue(r.victimEliminated(), "Offret ska vara markerat som eliminerat när nya max < minHealth");
    }

    // -- transferHearts: mördaren redan på max -> ingen överföring -------------

    @Test
    void transfer_killerAtMax_noTransfer() {
        HeartManager hm = HeartManager.createDefault();
        TransferResult r = hm.transferHearts(40.0f, 20.0f);
        assertEquals(40.0f, r.newKillerMax(), EPS);
        assertEquals(20.0f, r.newVictimMax(), EPS);
        assertEquals(0.0f, r.heartsTransferredHp(), EPS);
        assertFalse(r.transferOccurred());
        assertFalse(r.victimEliminated());
    }

    // -- transferHearts: partiell (mördaren nästan på max) ---------------------

    @Test
    void transfer_killerNearMax_partialTransfer() {
        HeartManager hm = HeartManager.createDefault();
        TransferResult r = hm.transferHearts(39.0f, 20.0f);
        assertEquals(40.0f, r.newKillerMax(), EPS);
        assertEquals(19.0f, r.newVictimMax(), EPS);
        assertEquals(1.0f, r.heartsTransferredHp(), EPS);
        assertTrue(r.transferOccurred());
    }

    // -- transferHearts: offer på 0 -> ingen överföring ------------------------

    @Test
    void transfer_victimAtZero_noTransfer() {
        HeartManager hm = HeartManager.createDefault();
        TransferResult r = hm.transferHearts(20.0f, 0.0f);
        assertEquals(20.0f, r.newKillerMax(), EPS);
        assertEquals(0.0f, r.newVictimMax(), EPS);
        assertFalse(r.transferOccurred());
        assertTrue(r.victimEliminated());
    }

    // -- isAtMaximumHealth ------------------------------------------------------

    @Test
    void isAtMaximumHealth_trueAtMax() {
        HeartManager hm = HeartManager.createDefault();
        assertTrue(hm.isAtMaximumHealth(40.0f));
    }

    @Test
    void isAtMaximumHealth_falseBelow() {
        HeartManager hm = HeartManager.createDefault();
        assertFalse(hm.isAtMaximumHealth(38.0f));
    }

    // -- isEliminated -----------------------------------------------------------

    @Test
    void isEliminated_trueWhenBelowMin() {
        HeartManager hm = HeartManager.createDefault();
        assertTrue(hm.isEliminated(3.9f));
        assertTrue(hm.isEliminated(0.0f));
    }

    @Test
    void isEliminated_falseAtOrAboveMin() {
        HeartManager hm = HeartManager.createDefault();
        assertFalse(hm.isEliminated(4.0f));
        assertFalse(hm.isEliminated(10.0f));
    }

    // -- Anpassade konfigurationer ---------------------------------------------

    @Test
    void customConfig_twoHeartsPerKill() {
        HeartManager hm = new HeartManager(4.0f, 40.0f, 2.0f);
        TransferResult r = hm.transferHearts(20.0f, 20.0f);
        assertEquals(24.0f, r.newKillerMax(), EPS);
        assertEquals(16.0f, r.newVictimMax(), EPS);
        assertEquals(4.0f, r.heartsTransferredHp(), EPS);
        assertEquals(2.0f, r.heartsTransferred(), EPS);
    }

    @Test
    void customConfig_lowerMax_capsKillerCorrectly() {
        HeartManager hm = new HeartManager(2.0f, 20.0f, 1.0f);
        TransferResult r = hm.transferHearts(19.0f, 10.0f);
        assertEquals(20.0f, r.newKillerMax(), EPS);
        assertEquals(9.0f, r.newVictimMax(), EPS);
        assertEquals(1.0f, r.heartsTransferredHp(), EPS);
    }

    @Test
    void clampToRange_bringsValuesIntoBounds() {
        HeartManager hm = HeartManager.createDefault();
        assertEquals(0.0f, hm.clampToRange(-5.0f), EPS);
        assertEquals(40.0f, hm.clampToRange(100.0f), EPS);
        assertEquals(20.0f, hm.clampToRange(20.0f), EPS);
    }
}
