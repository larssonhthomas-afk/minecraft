package com.dropnr.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BossKeyRegistryTest {

    @Test
    void enderDragon_mapsToDragonKey() {
        assertEquals(Optional.of(BossKeyType.DRAGON),
                BossKeyRegistry.forEntityId("minecraft:ender_dragon"));
    }

    @Test
    void warden_mapsToWardenKey() {
        assertEquals(Optional.of(BossKeyType.WARDEN),
                BossKeyRegistry.forEntityId("minecraft:warden"));
    }

    @Test
    void wither_mapsToWitherKey() {
        assertEquals(Optional.of(BossKeyType.WITHER),
                BossKeyRegistry.forEntityId("minecraft:wither"));
    }

    @Test
    void zombie_mapsToNothing() {
        assertEquals(Optional.empty(),
                BossKeyRegistry.forEntityId("minecraft:zombie"));
    }

    @Test
    void nullId_mapsToNothing() {
        assertEquals(Optional.empty(),
                BossKeyRegistry.forEntityId(null));
    }

    @Test
    void emptyId_mapsToNothing() {
        assertEquals(Optional.empty(),
                BossKeyRegistry.forEntityId(""));
    }

    @Test
    void constants_matchMinecraftIds() {
        assertEquals("minecraft:ender_dragon", BossKeyRegistry.ENDER_DRAGON_ID);
        assertEquals("minecraft:warden", BossKeyRegistry.WARDEN_ID);
        assertEquals("minecraft:wither", BossKeyRegistry.WITHER_ID);
    }

    @Test
    void caseSensitive_upperCaseDoesNotMatch() {
        assertEquals(Optional.empty(),
                BossKeyRegistry.forEntityId("Minecraft:Ender_Dragon"));
    }
}
