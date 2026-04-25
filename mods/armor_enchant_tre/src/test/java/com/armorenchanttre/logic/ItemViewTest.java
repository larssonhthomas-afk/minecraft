package com.armorenchanttre.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemViewTest {

    @Test
    void empty_isAir_andIsEmpty() {
        ItemView v = ItemView.empty();
        assertEquals("minecraft:air", v.itemId());
        assertTrue(v.isEmpty());
        assertNull(v.customName());
        assertFalse(v.hasPlayerProfile());
        assertNull(v.enchantMarkerId());
    }

    @Test
    void of_withId_setsOnlyId() {
        ItemView v = ItemView.of("minecraft:diamond_boots");
        assertEquals("minecraft:diamond_boots", v.itemId());
        assertFalse(v.isEmpty());
        assertNull(v.customName());
    }

    @Test
    void of_withIdAndName_setsBoth() {
        ItemView v = ItemView.of("minecraft:book", "Immunity");
        assertEquals("minecraft:book", v.itemId());
        assertEquals("Immunity", v.customName());
    }

    @Test
    void constructor_rejectsNullItemId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemView(null, null, false, null));
    }

    @Test
    void recordEquality_holds() {
        ItemView a = new ItemView("minecraft:book", "Immunity", false, "immunity");
        ItemView b = new ItemView("minecraft:book", "Immunity", false, "immunity");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
