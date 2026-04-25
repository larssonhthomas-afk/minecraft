package com.armorenchanttre.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeValidatorTest {

    private static ItemView book(String name) {
        return new ItemView(RecipeValidator.BOOK_ITEM_ID, name, false, null);
    }

    private static ItemView key(String name) {
        return new ItemView(RecipeValidator.KEY_ITEM_ID, name, false, null);
    }

    private static ItemView playerHead(boolean withProfile) {
        return new ItemView(RecipeValidator.PLAYER_HEAD_ITEM_ID, null, withProfile, null);
    }

    @Test
    void validate_immunityRecipe_returnsImmunity() {
        EnchantmentType result = RecipeValidator.validate(
                book("Immunity"),
                key("Dragon Key"),
                playerHead(true));
        assertSame(EnchantmentType.IMMUNITY, result);
    }

    @Test
    void validate_enduranceRecipe_returnsEndurance() {
        EnchantmentType result = RecipeValidator.validate(
                book("Endurance"),
                key("Warden Key"),
                playerHead(true));
        assertSame(EnchantmentType.ENDURANCE, result);
    }

    @Test
    void validate_extinguishRecipe_returnsExtinguish() {
        EnchantmentType result = RecipeValidator.validate(
                book("Extinguish"),
                key("Wither Key"),
                playerHead(true));
        assertSame(EnchantmentType.EXTINGUISH, result);
    }

    @Test
    void validate_mismatchedBookAndKey_returnsNull() {
        assertNull(RecipeValidator.validate(
                book("Immunity"),
                key("Warden Key"),
                playerHead(true)));
        assertNull(RecipeValidator.validate(
                book("Endurance"),
                key("Wither Key"),
                playerHead(true)));
    }

    @Test
    void validate_playerHeadWithoutProfile_isRejected() {
        assertNull(RecipeValidator.validate(
                book("Immunity"),
                key("Dragon Key"),
                playerHead(false)),
                "Vanlig skull utan profil får inte godkännas — endast Drop Mod-huvuden.");
    }

    @Test
    void validate_keyWithoutCustomName_isRejected() {
        ItemView vanillaFireworkStar = new ItemView(RecipeValidator.KEY_ITEM_ID, null, false, null);
        assertNull(RecipeValidator.validate(
                book("Immunity"),
                vanillaFireworkStar,
                playerHead(true)),
                "Vanlig firework_star utan rätt CUSTOM_NAME får inte godkännas.");
    }

    @Test
    void validate_keyWithWrongCustomName_isRejected() {
        ItemView fakeKey = new ItemView(RecipeValidator.KEY_ITEM_ID, "Fake Key", false, null);
        assertNull(RecipeValidator.validate(
                book("Immunity"),
                fakeKey,
                playerHead(true)));
    }

    @Test
    void validate_bookWithWrongName_isRejected() {
        assertNull(RecipeValidator.validate(
                book("immunity"),
                key("Dragon Key"),
                playerHead(true)),
                "Case-sensitive: 'immunity' != 'Immunity'");
    }

    @Test
    void validate_bookWithoutName_isRejected() {
        assertNull(RecipeValidator.validate(
                book(null),
                key("Dragon Key"),
                playerHead(true)));
    }

    @Test
    void validate_bookAlreadyEnchanted_isRejected() {
        ItemView enchantedBook = new ItemView(RecipeValidator.BOOK_ITEM_ID, "Immunity", false, "immunity");
        assertNull(RecipeValidator.validate(
                enchantedBook,
                key("Dragon Key"),
                playerHead(true)),
                "En bok som redan har vår marker får inte enchantas igen.");
    }

    @Test
    void validate_wrongItemTypes_areRejected() {
        ItemView paperPretendingToBeBook = new ItemView("minecraft:paper", "Immunity", false, null);
        assertNull(RecipeValidator.validate(
                paperPretendingToBeBook,
                key("Dragon Key"),
                playerHead(true)));

        ItemView skullInsteadOfHead = new ItemView("minecraft:wither_skeleton_skull", null, true, null);
        assertNull(RecipeValidator.validate(
                book("Immunity"),
                key("Dragon Key"),
                skullInsteadOfHead));
    }

    @Test
    void validate_nullInputs_returnsNull() {
        assertNull(RecipeValidator.validate(null, key("Dragon Key"), playerHead(true)));
        assertNull(RecipeValidator.validate(book("Immunity"), null, playerHead(true)));
        assertNull(RecipeValidator.validate(book("Immunity"), key("Dragon Key"), null));
    }

    @Test
    void readBookType_recognisesNamedBook() {
        assertSame(EnchantmentType.IMMUNITY, RecipeValidator.readBookType(book("Immunity")));
        assertSame(EnchantmentType.ENDURANCE, RecipeValidator.readBookType(book("Endurance")));
        assertSame(EnchantmentType.EXTINGUISH, RecipeValidator.readBookType(book("Extinguish")));
    }

    @Test
    void readKeyType_recognisesNamedKey() {
        assertSame(EnchantmentType.IMMUNITY, RecipeValidator.readKeyType(key("Dragon Key")));
        assertSame(EnchantmentType.ENDURANCE, RecipeValidator.readKeyType(key("Warden Key")));
        assertSame(EnchantmentType.EXTINGUISH, RecipeValidator.readKeyType(key("Wither Key")));
    }

    @Test
    void isValidPlayerHead_requiresProfile() {
        assertTrue(RecipeValidator.isValidPlayerHead(playerHead(true)));
        assertFalse(RecipeValidator.isValidPlayerHead(playerHead(false)));
        assertFalse(RecipeValidator.isValidPlayerHead(new ItemView("minecraft:player_head", "X", false, null)));
    }

    @Test
    void validateGrid_unenchantedImmunityBookPlusKeyPlusHead_returnsImmunity() {
        EnchantmentType result = RecipeValidator.validateGrid(List.of(
                book("Immunity"),
                key("Dragon Key"),
                playerHead(true),
                ItemView.empty(), ItemView.empty(), ItemView.empty(),
                ItemView.empty(), ItemView.empty(), ItemView.empty()));
        assertSame(EnchantmentType.IMMUNITY, result);
    }

    @Test
    void validateGrid_unenchantedEnduranceRecipe_returnsEndurance() {
        EnchantmentType result = RecipeValidator.validateGrid(List.of(
                book("Endurance"),
                key("Warden Key"),
                playerHead(true)));
        assertSame(EnchantmentType.ENDURANCE, result);
    }

    @Test
    void validateGrid_unenchantedExtinguishRecipe_returnsExtinguish() {
        EnchantmentType result = RecipeValidator.validateGrid(List.of(
                book("Extinguish"),
                key("Wither Key"),
                playerHead(true)));
        assertSame(EnchantmentType.EXTINGUISH, result);
    }

    @Test
    void validateGrid_alreadyEnchantedBook_returnsNull() {
        ItemView enchantedBook = new ItemView(RecipeValidator.BOOK_ITEM_ID, "Immunity", false, "immunity");
        EnchantmentType result = RecipeValidator.validateGrid(List.of(
                enchantedBook,
                key("Dragon Key"),
                playerHead(true)));
        assertNull(result, "En redan-enchantad bok i griden ska blockera crafting.");
    }

    @Test
    void validateGrid_extraJunkItem_returnsNull() {
        assertNull(RecipeValidator.validateGrid(List.of(
                book("Immunity"),
                key("Dragon Key"),
                playerHead(true),
                new ItemView("minecraft:stick", null, false, null))));
    }

    @Test
    void validateGrid_duplicateBooks_returnsNull() {
        assertNull(RecipeValidator.validateGrid(List.of(
                book("Immunity"),
                book("Immunity"),
                key("Dragon Key"),
                playerHead(true))));
    }

    @Test
    void validateGrid_missingHead_returnsNull() {
        assertNull(RecipeValidator.validateGrid(List.of(
                book("Immunity"),
                key("Dragon Key"))));
    }

    @Test
    void validateGrid_emptyGrid_returnsNull() {
        assertNull(RecipeValidator.validateGrid(List.of(
                ItemView.empty(), ItemView.empty(), ItemView.empty())));
    }

    @Test
    void validateGrid_nullStacks_returnsNull() {
        assertNull(RecipeValidator.validateGrid(null));
    }
}
