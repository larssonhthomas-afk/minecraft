package com.combattweakpearl.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CooldownLogicTest {

    @Test
    void pearlCooldownIs15Seconds() {
        assertEquals(300, CooldownLogic.PEARL_COOLDOWN_TICKS);
    }

    @Test
    void windChargeCooldownIs15Seconds() {
        assertEquals(300, CooldownLogic.WIND_CHARGE_COOLDOWN_TICKS);
    }

    @Test
    void bothCooldownsAreEqual() {
        assertEquals(CooldownLogic.PEARL_COOLDOWN_TICKS, CooldownLogic.WIND_CHARGE_COOLDOWN_TICKS);
    }
}
