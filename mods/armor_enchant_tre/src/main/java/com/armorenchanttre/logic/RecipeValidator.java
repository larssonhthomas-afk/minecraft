package com.armorenchanttre.logic;

import java.util.List;

/**
 * Validerar crafting-recipes för att skapa en enchantad bok.
 * Alla tre ingredienser måste matcha exakt en EnchantmentType:
 *   - oenchantad bok med customName som matchar bookName
 *   - firework_star med customName som matchar nyckelns displayName (Drop Mod-nyckel)
 *   - player_head med en spelarprofil (Drop Mod-huvud), inte en vanlig skull
 *
 * Returnerar EnchantmentType om kombinationen är giltig, annars null.
 */
public final class RecipeValidator {

    public static final String BOOK_ITEM_ID = "minecraft:book";
    public static final String KEY_ITEM_ID = "minecraft:firework_star";
    public static final String PLAYER_HEAD_ITEM_ID = "minecraft:player_head";

    private RecipeValidator() {}

    public static EnchantmentType validate(ItemView book, ItemView key, ItemView head) {
        if (book == null || key == null || head == null) return null;

        EnchantmentType byBook = readBookType(book);
        if (byBook == null) return null;

        EnchantmentType byKey = readKeyType(key);
        if (byKey == null) return null;

        if (byBook != byKey) return null;

        if (!isValidPlayerHead(head)) return null;

        return byBook;
    }

    /**
     * Sorterar in stackarna i bok-/nyckel-/huvud-slot baserat på itemId och kör validate().
     * En redan enchantad bok hamnar i bok-sloten och avvisas sedan av validate()
     * (eftersom den har en marker), så crafting blockeras tyst i det fallet.
     */
    public static EnchantmentType validateGrid(List<ItemView> stacks) {
        if (stacks == null) return null;
        ItemView book = null;
        ItemView key = null;
        ItemView head = null;
        for (ItemView v : stacks) {
            if (v == null || v.isEmpty()) continue;
            if (BOOK_ITEM_ID.equals(v.itemId())) {
                if (book != null) return null;
                book = v;
            } else if (KEY_ITEM_ID.equals(v.itemId())) {
                if (key != null) return null;
                key = v;
            } else if (PLAYER_HEAD_ITEM_ID.equals(v.itemId())) {
                if (head != null) return null;
                head = v;
            } else {
                return null;
            }
        }
        if (book == null || key == null || head == null) return null;
        return validate(book, key, head);
    }

    public static EnchantmentType readBookType(ItemView book) {
        if (book == null || !BOOK_ITEM_ID.equals(book.itemId())) return null;
        if (book.enchantMarkerId() != null) return null;
        return EnchantmentType.fromBookName(book.customName());
    }

    public static EnchantmentType readKeyType(ItemView key) {
        if (key == null || !KEY_ITEM_ID.equals(key.itemId())) return null;
        String name = key.customName();
        if (name == null) return null;
        for (EnchantmentType t : EnchantmentType.values()) {
            if (t.keyName().equals(name)) return t;
        }
        return null;
    }

    public static boolean isValidPlayerHead(ItemView head) {
        if (head == null) return false;
        if (!PLAYER_HEAD_ITEM_ID.equals(head.itemId())) return false;
        return head.hasPlayerProfile();
    }
}
