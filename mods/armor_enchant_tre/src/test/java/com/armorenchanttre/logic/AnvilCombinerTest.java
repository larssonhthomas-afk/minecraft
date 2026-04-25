package com.armorenchanttre.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnvilCombinerTest {

    private static ItemView armor(String itemId) {
        return new ItemView(itemId, null, false, null);
    }

    private static ItemView armor(String itemId, String existingMarker) {
        return new ItemView(itemId, null, false, existingMarker);
    }

    private static ItemView enchantedBook(EnchantmentType type) {
        return new ItemView(RecipeValidator.BOOK_ITEM_ID, type.bookName(), false, type.id());
    }

    @Test
    void combine_immunityBookOnBoots_isAllowed() {
        EnchantmentType result = AnvilCombiner.combine(
                armor("minecraft:diamond_boots"),
                enchantedBook(EnchantmentType.IMMUNITY));
        assertSame(EnchantmentType.IMMUNITY, result);
    }

    @Test
    void combine_enduranceBookOnLeggings_isAllowed() {
        EnchantmentType result = AnvilCombiner.combine(
                armor("minecraft:netherite_leggings"),
                enchantedBook(EnchantmentType.ENDURANCE));
        assertSame(EnchantmentType.ENDURANCE, result);
    }

    @Test
    void combine_extinguishBookOnLeggings_isAllowed() {
        EnchantmentType result = AnvilCombiner.combine(
                armor("minecraft:iron_leggings"),
                enchantedBook(EnchantmentType.EXTINGUISH));
        assertSame(EnchantmentType.EXTINGUISH, result);
    }

    @Test
    void combine_immunityBookOnHelmet_isRejected() {
        assertNull(AnvilCombiner.combine(
                armor("minecraft:diamond_helmet"),
                enchantedBook(EnchantmentType.IMMUNITY)));
    }

    @Test
    void combine_enduranceBookOnBoots_isRejected() {
        assertNull(AnvilCombiner.combine(
                armor("minecraft:diamond_boots"),
                enchantedBook(EnchantmentType.ENDURANCE)));
    }

    @Test
    void combine_extinguishBookOnChestplate_isRejected() {
        assertNull(AnvilCombiner.combine(
                armor("minecraft:diamond_chestplate"),
                enchantedBook(EnchantmentType.EXTINGUISH)));
    }

    @Test
    void combine_unenchantedBook_isRejected() {
        ItemView plainBook = new ItemView(RecipeValidator.BOOK_ITEM_ID, "Immunity", false, null);
        assertNull(AnvilCombiner.combine(
                armor("minecraft:diamond_boots"),
                plainBook),
                "Bok utan vår enchant-marker får inte appliceras via städ.");
    }

    @Test
    void combine_armorAlreadyHasSameEnchant_isRejected() {
        ItemView alreadyEnchanted = armor("minecraft:diamond_boots", "immunity");
        assertNull(AnvilCombiner.combine(alreadyEnchanted, enchantedBook(EnchantmentType.IMMUNITY)));
    }

    @Test
    void combine_emptyInputs_returnNull() {
        assertNull(AnvilCombiner.combine(ItemView.empty(), enchantedBook(EnchantmentType.IMMUNITY)));
        assertNull(AnvilCombiner.combine(armor("minecraft:diamond_boots"), ItemView.empty()));
    }

    @Test
    void combine_nullInputs_returnNull() {
        assertNull(AnvilCombiner.combine(null, enchantedBook(EnchantmentType.IMMUNITY)));
        assertNull(AnvilCombiner.combine(armor("minecraft:diamond_boots"), null));
    }

    @Test
    void combine_nonArmorLeftSlot_isRejected() {
        assertNull(AnvilCombiner.combine(
                new ItemView("minecraft:stick", null, false, null),
                enchantedBook(EnchantmentType.IMMUNITY)));
    }

    @Test
    void xpCost_isZero() {
        assertEquals(0, AnvilCombiner.xpCost());
    }
}
