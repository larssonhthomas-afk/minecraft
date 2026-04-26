package com.combatenchantcustom.logic;

public final class AnvilValidator {

    private AnvilValidator() {}

    public static boolean canEnchant(ItemView weapon, ItemView book) {
        if (weapon == null || book == null) return false;
        if (weapon.isEmpty() || book.isEmpty()) return false;
        if (!UnbrokenChainLogic.isSwordItemId(weapon.itemId())) return false;
        if (!"minecraft:enchanted_book".equals(book.itemId())) return false;
        if (!UnbrokenChainLogic.BOOK_NAME.equals(book.customName())) return false;
        return "unbroken_chain".equals(book.enchantMarkerId());
    }
}
