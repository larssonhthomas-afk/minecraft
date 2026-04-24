package com.rankedsmprank.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RankDataStoreTest {

    @TempDir
    Path dir;

    @Test
    void createsFileIfMissing() throws IOException {
        Path p = dir.resolve("ranks.json");
        assertFalse(p.toFile().exists());
        RankDataStore store = RankDataStore.loadOrCreate(p);
        assertTrue(p.toFile().exists());
        assertNotNull(store);
    }

    @Test
    void unknownPlayerReturnsMinusOne() throws IOException {
        RankDataStore store = RankDataStore.loadOrCreate(dir.resolve("r.json"));
        assertEquals(-1, store.getTier(UUID.randomUUID()));
    }

    @Test
    void setAndGetTier() throws IOException {
        RankDataStore store = RankDataStore.loadOrCreate(dir.resolve("r.json"));
        UUID uuid = UUID.randomUUID();
        store.setTier(uuid, 3);
        assertEquals(3, store.getTier(uuid));
    }

    @Test
    void persistsAcrossReload() throws IOException {
        Path p = dir.resolve("r.json");
        UUID uuid = UUID.randomUUID();

        RankDataStore store = RankDataStore.loadOrCreate(p);
        store.setTier(uuid, 5);
        store.save();

        RankDataStore reloaded = RankDataStore.loadOrCreate(p);
        assertEquals(5, reloaded.getTier(uuid));
    }

    @Test
    void playerNameDefaultsToUuidString() throws IOException {
        RankDataStore store = RankDataStore.loadOrCreate(dir.resolve("r.json"));
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), store.getPlayerName(uuid));
    }

    @Test
    void playerNamePersists() throws IOException {
        Path p = dir.resolve("r.json");
        UUID uuid = UUID.randomUUID();

        RankDataStore store = RankDataStore.loadOrCreate(p);
        store.setPlayerName(uuid, "TestPlayer");
        store.save();

        RankDataStore reloaded = RankDataStore.loadOrCreate(p);
        assertEquals("TestPlayer", reloaded.getPlayerName(uuid));
    }

    @Test
    void getAllTiersReturnsSnapshot() throws IOException {
        RankDataStore store = RankDataStore.loadOrCreate(dir.resolve("r.json"));
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        store.setTier(a, 1);
        store.setTier(b, 2);

        var all = store.getAllTiers();
        assertEquals(2, all.size());
        assertEquals(1, all.get(a));
        assertEquals(2, all.get(b));
    }

    @Test
    void clearAllTiersRemovesAll() throws IOException {
        RankDataStore store = RankDataStore.loadOrCreate(dir.resolve("r.json"));
        UUID a = UUID.randomUUID();
        store.setTier(a, 4);
        store.clearAllTiers();
        assertEquals(-1, store.getTier(a));
        assertTrue(store.getAllTiers().isEmpty());
    }

    @Test
    void multipleSaveLoadCycles() throws IOException {
        Path p = dir.resolve("r.json");
        UUID uuid = UUID.randomUUID();

        for (int cycle = 0; cycle < 3; cycle++) {
            RankDataStore store = RankDataStore.loadOrCreate(p);
            store.setTier(uuid, cycle + 1);
            store.save();
        }

        RankDataStore final_ = RankDataStore.loadOrCreate(p);
        assertEquals(3, final_.getTier(uuid));
    }

    @Test
    void legacyCleanModeFieldIsIgnoredOnLoad() throws IOException {
        // Write a JSON file that contains the old "cleanMode" field
        Path p = dir.resolve("r.json");
        java.nio.file.Files.writeString(p,
                "{\"tiers\":{},\"cleanMode\":{\"" + UUID.randomUUID() + "\":true},\"names\":{}}");
        // Should load without error and ignore cleanMode
        RankDataStore store = RankDataStore.loadOrCreate(p);
        assertNotNull(store);
    }
}
