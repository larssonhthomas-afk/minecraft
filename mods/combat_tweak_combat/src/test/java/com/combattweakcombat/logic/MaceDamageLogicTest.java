package com.combattweakcombat.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaceDamageLogicTest {

    @Test
    void damageReducedByFortyPercent() {
        float result = MaceDamageLogic.applyNerfedDamage(10.0f);
        assertEquals(6.0f, result, 0.001f);
    }

    @Test
    void damageCapAppliedAt14() {
        float result = MaceDamageLogic.applyNerfedDamage(30.0f);
        assertEquals(14.0f, result, 0.001f);
    }

    @Test
    void damageAtExactCapBoundary() {
        // 14 / 0.6 ≈ 23.333; anything above should be capped
        float result = MaceDamageLogic.applyNerfedDamage(23.4f);
        assertEquals(14.0f, result, 0.001f);
    }

    @Test
    void lowDamageNotCapped() {
        float result = MaceDamageLogic.applyNerfedDamage(5.0f);
        assertEquals(3.0f, result, 0.001f);
    }

    @Test
    void zeroDamageRemainsZero() {
        assertEquals(0.0f, MaceDamageLogic.applyNerfedDamage(0.0f), 0.001f);
    }

    @Test
    void maxDamageConstantIs14() {
        assertEquals(14.0f, MaceDamageLogic.MAX_DAMAGE, 0.001f);
    }
}
