package com.worldtweakancient.logic;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EnchantmentBanListTest {

    @Test
    void defaultsBansBreach() {
        EnchantmentBanList list = EnchantmentBanList.defaults();
        assertTrue(list.isBanned("minecraft:breach"));
    }

    @Test
    void defaultsAllowsOtherEnchantments() {
        EnchantmentBanList list = EnchantmentBanList.defaults();
        assertFalse(list.isBanned("minecraft:sharpness"));
        assertFalse(list.isBanned("minecraft:protection"));
        assertFalse(list.isBanned("minecraft:mending"));
    }

    @Test
    void nullEnchantmentIdReturnsFalse() {
        assertFalse(EnchantmentBanList.defaults().isBanned(null));
    }

    @Test
    void unknownEnchantmentIdReturnsFalse() {
        assertFalse(EnchantmentBanList.defaults().isBanned("minecraft:nonexistent"));
    }

    @Test
    void customListBansOnlySpecified() {
        Set<String> custom = new HashSet<>();
        custom.add("minecraft:sharpness");
        EnchantmentBanList list = new EnchantmentBanList(custom);
        assertTrue(list.isBanned("minecraft:sharpness"));
        assertFalse(list.isBanned("minecraft:breach"));
    }

    @Test
    void emptyListBansNothing() {
        EnchantmentBanList list = new EnchantmentBanList(new HashSet<>());
        assertFalse(list.isBanned("minecraft:breach"));
    }

    @Test
    void nullSetThrows() {
        assertThrows(IllegalArgumentException.class, () -> new EnchantmentBanList(null));
    }

    @Test
    void blankIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add("minecraft:breach");
        bad.add("");
        assertThrows(IllegalArgumentException.class, () -> new EnchantmentBanList(bad));
    }

    @Test
    void nullIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add(null);
        assertThrows(IllegalArgumentException.class, () -> new EnchantmentBanList(bad));
    }

    @Test
    void bannedSetIsImmutable() {
        EnchantmentBanList list = EnchantmentBanList.defaults();
        Set<String> banned = list.banned();
        assertThrows(UnsupportedOperationException.class, () -> banned.add("minecraft:hello"));
    }

    @Test
    void constantIsCorrect() {
        assertEquals("minecraft:breach", EnchantmentBanList.BREACH);
    }

    @Test
    void mutatingInputSetDoesNotMutateList() {
        Set<String> input = new HashSet<>();
        input.add("minecraft:breach");
        EnchantmentBanList list = new EnchantmentBanList(input);
        input.add("minecraft:sharpness");
        assertFalse(list.isBanned("minecraft:sharpness"));
    }
}
