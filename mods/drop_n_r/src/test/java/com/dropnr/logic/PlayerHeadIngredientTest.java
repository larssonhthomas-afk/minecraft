package com.dropnr.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerHeadIngredientTest {

    @Test
    void playerHead_isPlayerHead() {
        assertTrue(PlayerHeadIngredient.isPlayerHead("minecraft:player_head"));
    }

    @Test
    void zombieHead_isNotPlayerHead() {
        assertFalse(PlayerHeadIngredient.isPlayerHead("minecraft:zombie_head"));
    }

    @Test
    void skeletonSkull_isNotPlayerHead() {
        assertFalse(PlayerHeadIngredient.isPlayerHead("minecraft:skeleton_skull"));
    }

    @Test
    void creeperHead_isNotPlayerHead() {
        assertFalse(PlayerHeadIngredient.isPlayerHead("minecraft:creeper_head"));
    }

    @Test
    void emptyString_isNotPlayerHead() {
        assertFalse(PlayerHeadIngredient.isPlayerHead(""));
    }

    @Test
    void nullId_isNotPlayerHead() {
        assertFalse(PlayerHeadIngredient.isPlayerHead(null));
    }

    @Test
    void constant_exposesExpectedId() {
        assertEquals("minecraft:player_head", PlayerHeadIngredient.PLAYER_HEAD_ID);
    }
}
