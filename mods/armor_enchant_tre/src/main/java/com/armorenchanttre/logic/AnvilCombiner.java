package com.armorenchanttre.logic;

/**
 * Bestämmer om en kombination av (rustning, enchantad bok) i städet är giltig
 * och vilken enchantment som då ska appliceras på rustningen.
 *
 * Regler:
 *   - Vänster slot måste vara en rustningsdel av rätt slag (boots/leggings)
 *   - Höger slot måste vara en bok som markerats med vår egna enchantment-marker
 *   - Rustningens slot måste matcha enchantmentens armorSlot
 *   - Rustningen får inte redan ha samma enchantment
 */
public final class AnvilCombiner {

    private AnvilCombiner() {}

    public static EnchantmentType combine(ItemView armor, ItemView book) {
        if (armor == null || book == null) return null;
        if (armor.isEmpty() || book.isEmpty()) return null;

        EnchantmentType bookType = EnchantmentType.fromId(book.enchantMarkerId());
        if (bookType == null) return null;

        if (!RecipeValidator.BOOK_ITEM_ID.equals(book.itemId())) return null;

        EnchantmentType.ArmorSlot armorSlot = EnchantmentType.ArmorSlot.fromItemId(armor.itemId());
        if (armorSlot == null) return null;
        if (armorSlot != bookType.armorSlot()) return null;

        if (bookType.id().equals(armor.enchantMarkerId())) return null;

        return bookType;
    }

    public static int xpCost() {
        // Vanilla AnvilScreenHandler tillåter bara uttag av resultatet om levelCost > 0
        // (se Slot.canTakeItems i AnvilScreenHandler). Vi sätter därför 1 istället för 0.
        return 1;
    }
}
