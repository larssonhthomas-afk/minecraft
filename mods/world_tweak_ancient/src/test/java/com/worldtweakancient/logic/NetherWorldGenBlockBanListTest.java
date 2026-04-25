package com.worldtweakancient.logic;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NetherWorldGenBlockBanListTest {

    @Test
    void defaultsBanGoldBlockInNether() {
        NetherWorldGenBlockBanList list = NetherWorldGenBlockBanList.defaults();
        assertTrue(list.shouldBlockInNether("minecraft:the_nether", "minecraft:gold_block"));
    }

    @Test
    void defaultsAllowGoldBlockInOverworld() {
        NetherWorldGenBlockBanList list = NetherWorldGenBlockBanList.defaults();
        assertFalse(list.shouldBlockInNether("minecraft:overworld", "minecraft:gold_block"));
    }

    @Test
    void defaultsAllowGoldBlockInEnd() {
        NetherWorldGenBlockBanList list = NetherWorldGenBlockBanList.defaults();
        assertFalse(list.shouldBlockInNether("minecraft:the_end", "minecraft:gold_block"));
    }

    @Test
    void defaultsAllowOtherBlocksInNether() {
        NetherWorldGenBlockBanList list = NetherWorldGenBlockBanList.defaults();
        assertFalse(list.shouldBlockInNether("minecraft:the_nether", "minecraft:netherrack"));
        assertFalse(list.shouldBlockInNether("minecraft:the_nether", "minecraft:gilded_blackstone"));
        assertFalse(list.shouldBlockInNether("minecraft:the_nether", "minecraft:gold_ore"));
    }

    @Test
    void nullDimensionReturnsFalse() {
        NetherWorldGenBlockBanList list = NetherWorldGenBlockBanList.defaults();
        assertFalse(list.shouldBlockInNether(null, "minecraft:gold_block"));
    }

    @Test
    void nullBlockReturnsFalse() {
        NetherWorldGenBlockBanList list = NetherWorldGenBlockBanList.defaults();
        assertFalse(list.shouldBlockInNether("minecraft:the_nether", null));
    }

    @Test
    void customListBansOnlySpecified() {
        Set<String> custom = new HashSet<>();
        custom.add("minecraft:diamond_block");
        NetherWorldGenBlockBanList list = new NetherWorldGenBlockBanList(custom);
        assertTrue(list.shouldBlockInNether("minecraft:the_nether", "minecraft:diamond_block"));
        assertFalse(list.shouldBlockInNether("minecraft:the_nether", "minecraft:gold_block"));
    }

    @Test
    void emptyListBlocksNothing() {
        NetherWorldGenBlockBanList list = new NetherWorldGenBlockBanList(new HashSet<>());
        assertFalse(list.shouldBlockInNether("minecraft:the_nether", "minecraft:gold_block"));
    }

    @Test
    void nullSetThrows() {
        assertThrows(IllegalArgumentException.class, () -> new NetherWorldGenBlockBanList(null));
    }

    @Test
    void blankIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add("minecraft:gold_block");
        bad.add("");
        assertThrows(IllegalArgumentException.class, () -> new NetherWorldGenBlockBanList(bad));
    }

    @Test
    void bannedSetIsImmutable() {
        NetherWorldGenBlockBanList list = NetherWorldGenBlockBanList.defaults();
        Set<String> banned = list.banned();
        assertThrows(UnsupportedOperationException.class, () -> banned.add("minecraft:hello"));
    }

    @Test
    void constantsAreCorrect() {
        assertEquals("minecraft:gold_block", NetherWorldGenBlockBanList.GOLD_BLOCK);
        assertEquals("minecraft:the_nether", NetherWorldGenBlockBanList.NETHER_DIMENSION_ID);
    }
}
