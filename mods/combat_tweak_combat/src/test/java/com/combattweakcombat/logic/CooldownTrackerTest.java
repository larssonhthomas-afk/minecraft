package com.combattweakcombat.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CooldownTrackerTest {

    @Test
    void pearlCooldownIs300Ticks() {
        assertEquals(300, CooldownLogic.PEARL_COOLDOWN_TICKS);
    }

    @Test
    void windChargeCooldownIs300Ticks() {
        assertEquals(300, CooldownLogic.WIND_CHARGE_COOLDOWN_TICKS);
    }

    @Test
    void pearlAndWindChargeCooldownsAreEqual() {
        assertEquals(CooldownLogic.PEARL_COOLDOWN_TICKS, CooldownLogic.WIND_CHARGE_COOLDOWN_TICKS);
    }
}
