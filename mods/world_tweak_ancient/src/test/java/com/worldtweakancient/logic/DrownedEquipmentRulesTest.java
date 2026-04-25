package com.worldtweakancient.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrownedEquipmentRulesTest {

    @Test
    void defaultsDisallowTrident() {
        DrownedEquipmentRules rules = DrownedEquipmentRules.defaults();
        assertFalse(rules.isTridentAllowed());
    }

    @Test
    void defaultsSkipMainHandAssignment() {
        DrownedEquipmentRules rules = DrownedEquipmentRules.defaults();
        assertTrue(rules.shouldSkipMainHandAssignment());
    }

    @Test
    void tridentAllowedKeepsMainHandAssignment() {
        DrownedEquipmentRules rules = new DrownedEquipmentRules(true);
        assertTrue(rules.isTridentAllowed());
        assertFalse(rules.shouldSkipMainHandAssignment());
    }

    @Test
    void tridentDisallowedSkipsMainHandAssignment() {
        DrownedEquipmentRules rules = new DrownedEquipmentRules(false);
        assertFalse(rules.isTridentAllowed());
        assertTrue(rules.shouldSkipMainHandAssignment());
    }

    @Test
    void constantIsCorrect() {
        assertEquals("minecraft:trident", DrownedEquipmentRules.TRIDENT);
    }
}
