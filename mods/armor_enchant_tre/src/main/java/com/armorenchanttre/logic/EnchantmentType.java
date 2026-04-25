package com.armorenchanttre.logic;

/**
 * De tre custom enchantments som modden tillhandahåller.
 * Varje enchantment har en bok-namn-prefix som krävs på den oenchantade boken,
 * en bossnyckel-typ som krävs som ingrediens, samt vilken rustningstyp den får appliceras på.
 */
public enum EnchantmentType {
    IMMUNITY("immunity", "Immunity", "Dragon Key", ArmorSlot.FEET),
    ENDURANCE("endurance", "Endurance", "Warden Key", ArmorSlot.LEGS),
    EXTINGUISH("extinguish", "Extinguish", "Wither Key", ArmorSlot.LEGS);

    private final String id;
    private final String bookName;
    private final String keyName;
    private final ArmorSlot armorSlot;

    EnchantmentType(String id, String bookName, String keyName, ArmorSlot armorSlot) {
        this.id = id;
        this.bookName = bookName;
        this.keyName = keyName;
        this.armorSlot = armorSlot;
    }

    public String id() {
        return id;
    }

    public String bookName() {
        return bookName;
    }

    public String keyName() {
        return keyName;
    }

    public ArmorSlot armorSlot() {
        return armorSlot;
    }

    public static EnchantmentType fromId(String id) {
        if (id == null) return null;
        for (EnchantmentType t : values()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    public static EnchantmentType fromBookName(String bookName) {
        if (bookName == null) return null;
        for (EnchantmentType t : values()) {
            if (t.bookName.equals(bookName)) return t;
        }
        return null;
    }

    public enum ArmorSlot {
        HEAD, CHEST, LEGS, FEET;

        public static ArmorSlot fromItemId(String itemId) {
            if (itemId == null) return null;
            if (itemId.endsWith("_helmet")) return HEAD;
            if (itemId.endsWith("_chestplate")) return CHEST;
            if (itemId.endsWith("_leggings")) return LEGS;
            if (itemId.endsWith("_boots")) return FEET;
            return null;
        }
    }
}
