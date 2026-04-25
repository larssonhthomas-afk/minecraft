package com.worldtweakancient.logic;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BastionLootBanListTest {

    @Test
    void defaultsBansAncientDebrisAndNetheriteIngot() {
        BastionLootBanList list = BastionLootBanList.defaults();
        assertTrue(list.isBanned("minecraft:ancient_debris"));
        assertTrue(list.isBanned("minecraft:netherite_ingot"));
    }

    @Test
    void defaultsAllowOtherItems() {
        BastionLootBanList list = BastionLootBanList.defaults();
        assertFalse(list.isBanned("minecraft:diamond"));
        assertFalse(list.isBanned("minecraft:gold_ingot"));
        assertFalse(list.isBanned("minecraft:netherite_scrap"));
        assertFalse(list.isBanned("minecraft:gold_block"));
    }

    @Test
    void nullItemIdReturnsFalse() {
        assertFalse(BastionLootBanList.defaults().isBanned(null));
    }

    @Test
    void unknownItemIdReturnsFalse() {
        assertFalse(BastionLootBanList.defaults().isBanned("minecraft:unobtanium"));
    }

    @Test
    void customListBansOnlySpecified() {
        Set<String> custom = new HashSet<>();
        custom.add("minecraft:diamond");
        BastionLootBanList list = new BastionLootBanList(custom);
        assertTrue(list.isBanned("minecraft:diamond"));
        assertFalse(list.isBanned("minecraft:ancient_debris"));
    }

    @Test
    void emptyListBansNothing() {
        BastionLootBanList list = new BastionLootBanList(new HashSet<>());
        assertFalse(list.isBanned("minecraft:ancient_debris"));
        assertFalse(list.isBanned("minecraft:gold_ingot"));
    }

    @Test
    void nullSetThrows() {
        assertThrows(IllegalArgumentException.class, () -> new BastionLootBanList(null));
    }

    @Test
    void blankIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add("minecraft:diamond");
        bad.add("");
        assertThrows(IllegalArgumentException.class, () -> new BastionLootBanList(bad));
    }

    @Test
    void nullIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add(null);
        assertThrows(IllegalArgumentException.class, () -> new BastionLootBanList(bad));
    }

    @Test
    void bannedSetIsImmutable() {
        BastionLootBanList list = BastionLootBanList.defaults();
        Set<String> banned = list.banned();
        assertThrows(UnsupportedOperationException.class, () -> banned.add("minecraft:hello"));
    }

    @Test
    void constantsAreCorrect() {
        assertEquals("minecraft:ancient_debris", BastionLootBanList.ANCIENT_DEBRIS);
        assertEquals("minecraft:netherite_ingot", BastionLootBanList.NETHERITE_INGOT);
    }

    @Test
    void mutatingInputSetDoesNotMutateList() {
        Set<String> input = new HashSet<>();
        input.add("minecraft:diamond");
        BastionLootBanList list = new BastionLootBanList(input);
        input.add("minecraft:emerald");
        assertFalse(list.isBanned("minecraft:emerald"));
    }
}
