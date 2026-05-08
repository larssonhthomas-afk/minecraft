package com.combattweakpearl.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaceDamageLogicTest {

    @Test
    void reducesBy40Percent() {
        assertEquals(6.0f, MaceDamageLogic.applyNerfedDamage(10.0f), 0.001f);
    }

    @Test
    void cappedAt14HP() {
        assertEquals(14.0f, MaceDamageLogic.applyNerfedDamage(30.0f), 0.001f);
    }

    @Test
    void largeDamageAlwaysCapped() {
        assertEquals(14.0f, MaceDamageLogic.applyNerfedDamage(100.0f), 0.001f);
    }

    @Test
    void normalDamageNotCapped() {
        float result = MaceDamageLogic.applyNerfedDamage(7.0f);
        assertTrue(result < MaceDamageLogic.MAX_DAMAGE);
        assertEquals(4.2f, result, 0.001f);
    }

    @Test
    void exactCapBoundary() {
        // 14.0 / 0.6 = 23.333... — below triggers nerf only, above also caps
        float below = MaceDamageLogic.applyNerfedDamage(23.0f);
        float above = MaceDamageLogic.applyNerfedDamage(24.0f);
        assertTrue(below < MaceDamageLogic.MAX_DAMAGE);
        assertEquals(MaceDamageLogic.MAX_DAMAGE, above, 0.001f);
    }

    @Test
    void zeroDamageStaysZero() {
        assertEquals(0.0f, MaceDamageLogic.applyNerfedDamage(0.0f), 0.001f);
    }

    @Test
    void maxDamageConstantIs14() {
        assertEquals(14.0f, MaceDamageLogic.MAX_DAMAGE, 0.001f);
    }
}
