package com.armorenchanttre.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnchantmentTypeTest {

    @Test
    void immunity_isOnFeet_andRequiresDragonKey() {
        assertEquals(EnchantmentType.ArmorSlot.FEET, EnchantmentType.IMMUNITY.armorSlot());
        assertEquals("Dragon Key", EnchantmentType.IMMUNITY.keyName());
        assertEquals("Immunity", EnchantmentType.IMMUNITY.bookName());
        assertEquals("immunity", EnchantmentType.IMMUNITY.id());
    }

    @Test
    void endurance_isOnLegs_andRequiresWardenKey() {
        assertEquals(EnchantmentType.ArmorSlot.LEGS, EnchantmentType.ENDURANCE.armorSlot());
        assertEquals("Warden Key", EnchantmentType.ENDURANCE.keyName());
        assertEquals("Endurance", EnchantmentType.ENDURANCE.bookName());
        assertEquals("endurance", EnchantmentType.ENDURANCE.id());
    }

    @Test
    void extinguish_isOnLegs_andRequiresWitherKey() {
        assertEquals(EnchantmentType.ArmorSlot.LEGS, EnchantmentType.EXTINGUISH.armorSlot());
        assertEquals("Wither Key", EnchantmentType.EXTINGUISH.keyName());
        assertEquals("Extinguish", EnchantmentType.EXTINGUISH.bookName());
        assertEquals("extinguish", EnchantmentType.EXTINGUISH.id());
    }

    @Test
    void fromId_findsByLowercaseIdentifier() {
        assertSame(EnchantmentType.IMMUNITY, EnchantmentType.fromId("immunity"));
        assertSame(EnchantmentType.ENDURANCE, EnchantmentType.fromId("endurance"));
        assertSame(EnchantmentType.EXTINGUISH, EnchantmentType.fromId("extinguish"));
    }

    @Test
    void fromId_returnsNullForUnknownOrNull() {
        assertNull(EnchantmentType.fromId(null));
        assertNull(EnchantmentType.fromId(""));
        assertNull(EnchantmentType.fromId("Immunity"));
        assertNull(EnchantmentType.fromId("flame"));
    }

    @Test
    void fromBookName_findsByDisplayName() {
        assertSame(EnchantmentType.IMMUNITY, EnchantmentType.fromBookName("Immunity"));
        assertSame(EnchantmentType.ENDURANCE, EnchantmentType.fromBookName("Endurance"));
        assertSame(EnchantmentType.EXTINGUISH, EnchantmentType.fromBookName("Extinguish"));
    }

    @Test
    void fromBookName_returnsNullForUnknownOrNull() {
        assertNull(EnchantmentType.fromBookName(null));
        assertNull(EnchantmentType.fromBookName("immunity"));
        assertNull(EnchantmentType.fromBookName("Random Name"));
    }

    @Test
    void armorSlot_fromItemId_recognisesAllVanillaArmor() {
        assertEquals(EnchantmentType.ArmorSlot.HEAD, EnchantmentType.ArmorSlot.fromItemId("minecraft:diamond_helmet"));
        assertEquals(EnchantmentType.ArmorSlot.CHEST, EnchantmentType.ArmorSlot.fromItemId("minecraft:netherite_chestplate"));
        assertEquals(EnchantmentType.ArmorSlot.LEGS, EnchantmentType.ArmorSlot.fromItemId("minecraft:iron_leggings"));
        assertEquals(EnchantmentType.ArmorSlot.FEET, EnchantmentType.ArmorSlot.fromItemId("minecraft:leather_boots"));
    }

    @Test
    void armorSlot_fromItemId_returnsNullForNonArmor() {
        assertNull(EnchantmentType.ArmorSlot.fromItemId(null));
        assertNull(EnchantmentType.ArmorSlot.fromItemId("minecraft:stick"));
        assertNull(EnchantmentType.ArmorSlot.fromItemId("minecraft:elytra"));
    }
}
