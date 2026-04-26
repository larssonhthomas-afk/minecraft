package com.combatenchantcustom.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnvilValidatorTest {

    private static final ItemView VALID_BOOK =
            new ItemView("minecraft:enchanted_book", "Unbroken_chain", false, "unbroken_chain");

    @Test
    void diamondSwordPlusBookIsValid() {
        ItemView sword = new ItemView("minecraft:diamond_sword", null, false, null);
        assertTrue(AnvilValidator.canEnchant(sword, VALID_BOOK));
    }

    @Test
    void allSwordVariantsAccepted() {
        for (String id : new String[]{
                "minecraft:wooden_sword", "minecraft:stone_sword",
                "minecraft:iron_sword", "minecraft:golden_sword",
                "minecraft:diamond_sword", "minecraft:netherite_sword"}) {
            assertTrue(AnvilValidator.canEnchant(new ItemView(id, null, false, null), VALID_BOOK),
                    "Expected " + id + " to be valid");
        }
    }

    @Test
    void axeRejected() {
        ItemView axe = new ItemView("minecraft:diamond_axe", null, false, null);
        assertFalse(AnvilValidator.canEnchant(axe, VALID_BOOK));
    }

    @Test
    void bowRejected() {
        ItemView bow = new ItemView("minecraft:bow", null, false, null);
        assertFalse(AnvilValidator.canEnchant(bow, VALID_BOOK));
    }

    @Test
    void bookWithWrongNameRejected() {
        ItemView sword = new ItemView("minecraft:diamond_sword", null, false, null);
        ItemView wrongBook = new ItemView("minecraft:enchanted_book", "SomethingElse", false, "unbroken_chain");
        assertFalse(AnvilValidator.canEnchant(sword, wrongBook));
    }

    @Test
    void bookWithoutMarkerRejected() {
        ItemView sword = new ItemView("minecraft:diamond_sword", null, false, null);
        ItemView noMarkerBook = new ItemView("minecraft:enchanted_book", "Unbroken_chain", false, null);
        assertFalse(AnvilValidator.canEnchant(sword, noMarkerBook));
    }

    @Test
    void regularBookRejected() {
        ItemView sword = new ItemView("minecraft:diamond_sword", null, false, null);
        ItemView plainBook = new ItemView("minecraft:book", "Unbroken_chain", false, "unbroken_chain");
        assertFalse(AnvilValidator.canEnchant(sword, plainBook));
    }

    @Test
    void nullWeaponRejected() {
        assertFalse(AnvilValidator.canEnchant(null, VALID_BOOK));
    }

    @Test
    void nullBookRejected() {
        ItemView sword = new ItemView("minecraft:diamond_sword", null, false, null);
        assertFalse(AnvilValidator.canEnchant(sword, null));
    }

    @Test
    void bothNullRejected() {
        assertFalse(AnvilValidator.canEnchant(null, null));
    }

    @Test
    void emptyWeaponRejected() {
        assertFalse(AnvilValidator.canEnchant(ItemView.empty(), VALID_BOOK));
    }

    @Test
    void emptyBookRejected() {
        ItemView sword = new ItemView("minecraft:diamond_sword", null, false, null);
        assertFalse(AnvilValidator.canEnchant(sword, ItemView.empty()));
    }
}
