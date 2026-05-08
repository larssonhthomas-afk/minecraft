package com.worldtweakancient.logic;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalLootBanListTest {

    @Test
    void defaultsBansAllThreeItems() {
        GlobalLootBanList list = GlobalLootBanList.defaults();
        assertTrue(list.isBanned("minecraft:ancient_debris"));
        assertTrue(list.isBanned("minecraft:netherite_ingot"));
        assertTrue(list.isBanned("minecraft:netherite_scrap"));
    }

    @Test
    void defaultsAllowOtherItems() {
        GlobalLootBanList list = GlobalLootBanList.defaults();
        assertFalse(list.isBanned("minecraft:diamond"));
        assertFalse(list.isBanned("minecraft:gold_ingot"));
        assertFalse(list.isBanned("minecraft:gold_block"));
        assertFalse(list.isBanned("minecraft:crossbow"));
    }

    @Test
    void nullItemIdReturnsFalse() {
        assertFalse(GlobalLootBanList.defaults().isBanned(null));
    }

    @Test
    void unknownItemIdReturnsFalse() {
        assertFalse(GlobalLootBanList.defaults().isBanned("minecraft:unobtanium"));
    }

    @Test
    void customListBansOnlySpecified() {
        Set<String> custom = new HashSet<>();
        custom.add("minecraft:diamond");
        GlobalLootBanList list = new GlobalLootBanList(custom);
        assertTrue(list.isBanned("minecraft:diamond"));
        assertFalse(list.isBanned("minecraft:ancient_debris"));
    }

    @Test
    void emptyListBansNothing() {
        GlobalLootBanList list = new GlobalLootBanList(new HashSet<>());
        assertFalse(list.isBanned("minecraft:ancient_debris"));
        assertFalse(list.isBanned("minecraft:netherite_ingot"));
        assertFalse(list.isBanned("minecraft:netherite_scrap"));
    }

    @Test
    void nullSetThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GlobalLootBanList(null));
    }

    @Test
    void blankIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add("minecraft:diamond");
        bad.add("");
        assertThrows(IllegalArgumentException.class, () -> new GlobalLootBanList(bad));
    }

    @Test
    void nullIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add(null);
        assertThrows(IllegalArgumentException.class, () -> new GlobalLootBanList(bad));
    }

    @Test
    void bannedSetIsImmutable() {
        GlobalLootBanList list = GlobalLootBanList.defaults();
        Set<String> banned = list.banned();
        assertThrows(UnsupportedOperationException.class, () -> banned.add("minecraft:hello"));
    }

    @Test
    void constantsAreCorrect() {
        assertEquals("minecraft:ancient_debris", GlobalLootBanList.ANCIENT_DEBRIS);
        assertEquals("minecraft:netherite_ingot", GlobalLootBanList.NETHERITE_INGOT);
        assertEquals("minecraft:netherite_scrap", GlobalLootBanList.NETHERITE_SCRAP);
    }

    @Test
    void mutatingInputSetDoesNotMutateList() {
        Set<String> input = new HashSet<>();
        input.add("minecraft:diamond");
        GlobalLootBanList list = new GlobalLootBanList(input);
        input.add("minecraft:emerald");
        assertFalse(list.isBanned("minecraft:emerald"));
    }
}
