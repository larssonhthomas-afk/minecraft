package com.worldtweakancient.logic;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntitySpawnBlockListTest {

    @Test
    void defaultsBlockZombifiedPiglin() {
        EntitySpawnBlockList list = EntitySpawnBlockList.defaults();
        assertTrue(list.isSpawnBlocked("minecraft:zombified_piglin"));
    }

    @Test
    void defaultsAllowOtherEntities() {
        EntitySpawnBlockList list = EntitySpawnBlockList.defaults();
        assertFalse(list.isSpawnBlocked("minecraft:zombie"));
        assertFalse(list.isSpawnBlocked("minecraft:piglin"));
        assertFalse(list.isSpawnBlocked("minecraft:hoglin"));
        assertFalse(list.isSpawnBlocked("minecraft:player"));
    }

    @Test
    void nullEntityIdReturnsFalse() {
        assertFalse(EntitySpawnBlockList.defaults().isSpawnBlocked(null));
    }

    @Test
    void customListBlocksOnlySpecified() {
        Set<String> custom = new HashSet<>();
        custom.add("minecraft:creeper");
        EntitySpawnBlockList list = new EntitySpawnBlockList(custom);
        assertTrue(list.isSpawnBlocked("minecraft:creeper"));
        assertFalse(list.isSpawnBlocked("minecraft:zombified_piglin"));
    }

    @Test
    void emptyListBlocksNothing() {
        EntitySpawnBlockList list = new EntitySpawnBlockList(new HashSet<>());
        assertFalse(list.isSpawnBlocked("minecraft:zombified_piglin"));
    }

    @Test
    void nullSetThrows() {
        assertThrows(IllegalArgumentException.class, () -> new EntitySpawnBlockList(null));
    }

    @Test
    void blankIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add("minecraft:zombie");
        bad.add("   ");
        assertThrows(IllegalArgumentException.class, () -> new EntitySpawnBlockList(bad));
    }

    @Test
    void nullIdInSetThrows() {
        Set<String> bad = new HashSet<>();
        bad.add(null);
        assertThrows(IllegalArgumentException.class, () -> new EntitySpawnBlockList(bad));
    }

    @Test
    void blockedSetIsImmutable() {
        EntitySpawnBlockList list = EntitySpawnBlockList.defaults();
        Set<String> blocked = list.blocked();
        assertThrows(UnsupportedOperationException.class, () -> blocked.add("minecraft:hello"));
    }

    @Test
    void constantIsCorrect() {
        assertEquals("minecraft:zombified_piglin", EntitySpawnBlockList.ZOMBIFIED_PIGLIN);
    }

    @Test
    void canBlockMultipleEntities() {
        Set<String> blocked = new HashSet<>();
        blocked.add("minecraft:zombified_piglin");
        blocked.add("minecraft:wither_skeleton");
        EntitySpawnBlockList list = new EntitySpawnBlockList(blocked);
        assertTrue(list.isSpawnBlocked("minecraft:zombified_piglin"));
        assertTrue(list.isSpawnBlocked("minecraft:wither_skeleton"));
        assertFalse(list.isSpawnBlocked("minecraft:zombie"));
    }
}
