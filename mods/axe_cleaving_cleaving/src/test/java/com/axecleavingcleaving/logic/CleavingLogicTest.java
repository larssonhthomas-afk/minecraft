package com.axecleavingcleaving.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CleavingLogicTest {

    // isAxeById — valid axes

    @Test
    void woodenAxeIsAxe() {
        assertTrue(CleavingLogic.isAxeById("minecraft:wooden_axe"));
    }

    @Test
    void stoneAxeIsAxe() {
        assertTrue(CleavingLogic.isAxeById("minecraft:stone_axe"));
    }

    @Test
    void ironAxeIsAxe() {
        assertTrue(CleavingLogic.isAxeById("minecraft:iron_axe"));
    }

    @Test
    void goldenAxeIsAxe() {
        assertTrue(CleavingLogic.isAxeById("minecraft:golden_axe"));
    }

    @Test
    void diamondAxeIsAxe() {
        assertTrue(CleavingLogic.isAxeById("minecraft:diamond_axe"));
    }

    @Test
    void netheriteAxeIsAxe() {
        assertTrue(CleavingLogic.isAxeById("minecraft:netherite_axe"));
    }

    // isAxeById — non-axes

    @Test
    void swordIsNotAxe() {
        assertFalse(CleavingLogic.isAxeById("minecraft:diamond_sword"));
    }

    @Test
    void pickaxeIsNotAxe() {
        assertFalse(CleavingLogic.isAxeById("minecraft:iron_pickaxe"));
    }

    @Test
    void nullIsNotAxe() {
        assertFalse(CleavingLogic.isAxeById(null));
    }

    @Test
    void emptyStringIsNotAxe() {
        assertFalse(CleavingLogic.isAxeById(""));
    }

    @Test
    void partialAxeIdIsNotAxe() {
        assertFalse(CleavingLogic.isAxeById("diamond_axe"));
    }

    // getExtraDamage

    @Test
    void extraDamageIsThreeHp() {
        assertEquals(3.0f, CleavingLogic.getExtraDamage(), 0.001f);
    }

    // hasCleaving

    @Test
    void hasCleavingTrueWhenFlagTrue() {
        assertTrue(CleavingLogic.hasCleaving(true));
    }

    @Test
    void hasCleavingFalseWhenFlagFalse() {
        assertFalse(CleavingLogic.hasCleaving(false));
    }

    // isCleavingBook

    @Test
    void isCleavingBookTrueWhenFlagTrue() {
        assertTrue(CleavingLogic.isCleavingBook(true));
    }

    @Test
    void isCleavingBookFalseWhenFlagFalse() {
        assertFalse(CleavingLogic.isCleavingBook(false));
    }

    // constants

    @Test
    void cleavingNbtKeyIsNotEmpty() {
        assertFalse(CleavingLogic.CLEAVING_NBT_KEY.isEmpty());
    }

    @Test
    void cleavingBookNbtKeyIsNotEmpty() {
        assertFalse(CleavingLogic.CLEAVING_BOOK_NBT_KEY.isEmpty());
    }

    @Test
    void nbtKeysAreDistinct() {
        assertNotEquals(CleavingLogic.CLEAVING_NBT_KEY, CleavingLogic.CLEAVING_BOOK_NBT_KEY);
    }
}
