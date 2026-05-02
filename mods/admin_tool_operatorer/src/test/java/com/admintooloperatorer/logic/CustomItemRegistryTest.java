package com.admintooloperatorer.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomItemRegistryTest {

    @Test
    void allItemIds_containsAllBaseItems() {
        List<String> ids = CustomItemRegistry.allItemIds();
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("neutral_player_head")));
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("Warden_key")));
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("Whiter_key")));
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("Dragon_key")));
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("Immunity")));
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("Extinguish")));
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("endurance")));
    }

    @Test
    void allItemIds_containsUnbrokenChainBook() {
        List<String> ids = CustomItemRegistry.allItemIds();
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("Unbroken_chain")));
    }

    @Test
    void isValid_caseInsensitive() {
        assertTrue(CustomItemRegistry.isValid("Immunity"));
        assertTrue(CustomItemRegistry.isValid("immunity"));
        assertTrue(CustomItemRegistry.isValid("IMMUNITY"));
        assertTrue(CustomItemRegistry.isValid("Warden_key"));
        assertTrue(CustomItemRegistry.isValid("WARDEN_KEY"));
    }

    @Test
    void isValid_nullReturnsFalse() {
        assertFalse(CustomItemRegistry.isValid(null));
    }

    @Test
    void isValid_unknownItemReturnsFalse() {
        assertFalse(CustomItemRegistry.isValid("unknown_item_xyz"));
        assertFalse(CustomItemRegistry.isValid(""));
    }

    @Test
    void registerEnchantBook_appearsInAllItems() {
        CustomItemRegistry.registerEnchantBook("Test_enchant_xyz");
        List<String> ids = CustomItemRegistry.allItemIds();
        assertTrue(ids.stream().anyMatch(s -> s.equalsIgnoreCase("Test_enchant_xyz")));
        assertTrue(CustomItemRegistry.isValid("Test_enchant_xyz"));
    }

    @Test
    void registerEnchantBook_noDuplicates() {
        int before = CustomItemRegistry.allItemIds().size();
        CustomItemRegistry.registerEnchantBook("Unbroken_chain");
        int after = CustomItemRegistry.allItemIds().size();
        assertEquals(before, after);
    }

    @Test
    void allItemIds_returnsUnmodifiableList() {
        List<String> ids = CustomItemRegistry.allItemIds();
        assertThrows(UnsupportedOperationException.class, () -> ids.add("hack"));
    }
}
