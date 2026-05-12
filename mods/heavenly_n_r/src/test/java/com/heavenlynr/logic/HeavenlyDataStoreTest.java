package com.heavenlynr.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HeavenlyDataStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void freshStoreHasNoAbility() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        assertFalse(store.hasAbility(UUID.randomUUID()));
    }

    @Test
    void grantAndCheckAbility() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        store.grantAbility(id);
        assertTrue(store.hasAbility(id));
    }

    @Test
    void revokeRemovesAbility() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        store.grantAbility(id);
        store.revokeAbility(id);
        assertFalse(store.hasAbility(id));
    }

    @Test
    void revokeAlsoClearsCooldown() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        store.grantAbility(id);
        store.triggerCooldown(id);
        assertTrue(store.isOnCooldown(id));
        store.revokeAbility(id);
        assertFalse(store.isOnCooldown(id));
    }

    @Test
    void freshStoreHasNoCooldown() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        assertFalse(store.isOnCooldown(id));
        assertEquals(0, store.remainingCooldownMs(id));
    }

    @Test
    void triggerCooldown_setsActiveCooldown() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        store.triggerCooldown(id);
        assertTrue(store.isOnCooldown(id));
        assertTrue(store.remainingCooldownMs(id) > 0);
    }

    @Test
    void triggerCooldown_remainingIsApproxTwentyMinutes() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        store.triggerCooldown(id);
        long remaining = store.remainingCooldownMs(id);
        // Allow 1-second tolerance
        assertTrue(remaining > HeavenlyLogic.COOLDOWN_MS - 1000);
        assertTrue(remaining <= HeavenlyLogic.COOLDOWN_MS);
    }

    @Test
    void persistsHoldersAcrossReload() throws IOException {
        Path file = tempDir.resolve("test.json");
        UUID id = UUID.randomUUID();

        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(file);
        store.grantAbility(id);
        store.setPlayerName(id, "Steve");
        store.save();

        HeavenlyDataStore reloaded = HeavenlyDataStore.loadOrCreate(file);
        assertTrue(reloaded.hasAbility(id));
        assertEquals("Steve", reloaded.getPlayerName(id));
    }

    @Test
    void persistsCooldownAcrossReload() throws IOException {
        Path file = tempDir.resolve("test.json");
        UUID id = UUID.randomUUID();

        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(file);
        store.grantAbility(id);
        store.triggerCooldown(id);
        long expiry = store.getCooldownExpiry(id);
        store.save();

        HeavenlyDataStore reloaded = HeavenlyDataStore.loadOrCreate(file);
        assertEquals(expiry, reloaded.getCooldownExpiry(id));
        assertTrue(reloaded.isOnCooldown(id));
    }

    @Test
    void playerNameDefaultsToUuidString() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        assertEquals(id.toString(), store.getPlayerName(id));
    }

    @Test
    void getAllHoldersReturnsSnapshot() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        store.grantAbility(id1);
        store.grantAbility(id2);
        assertEquals(2, store.getAllHolders().size());
        assertTrue(store.getAllHolders().contains(id1));
        assertTrue(store.getAllHolders().contains(id2));
    }

    @Test
    void revokeNonExistentDoesNotThrow() throws IOException {
        HeavenlyDataStore store = HeavenlyDataStore.loadOrCreate(tempDir.resolve("test.json"));
        assertDoesNotThrow(() -> store.revokeAbility(UUID.randomUUID()));
    }
}
