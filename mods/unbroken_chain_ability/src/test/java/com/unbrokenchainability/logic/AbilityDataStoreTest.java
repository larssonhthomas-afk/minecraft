package com.unbrokenchainability.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbilityDataStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void freshStoreHasNoHolders() throws IOException {
        AbilityDataStore store = AbilityDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        assertFalse(store.hasAbility(id));
    }

    @Test
    void grantAndCheckAbility() throws IOException {
        AbilityDataStore store = AbilityDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        store.grantAbility(id);
        assertTrue(store.hasAbility(id));
    }

    @Test
    void revokeRemovesAbility() throws IOException {
        AbilityDataStore store = AbilityDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        store.grantAbility(id);
        store.revokeAbility(id);
        assertFalse(store.hasAbility(id));
    }

    @Test
    void persistsAcrossReload() throws IOException {
        Path file = tempDir.resolve("test.json");
        UUID id = UUID.randomUUID();

        AbilityDataStore store = AbilityDataStore.loadOrCreate(file);
        store.grantAbility(id);
        store.setPlayerName(id, "Steve");
        store.save();

        AbilityDataStore reloaded = AbilityDataStore.loadOrCreate(file);
        assertTrue(reloaded.hasAbility(id));
        assertEquals("Steve", reloaded.getPlayerName(id));
    }

    @Test
    void playerNameDefaultsToUuidString() throws IOException {
        AbilityDataStore store = AbilityDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id = UUID.randomUUID();
        assertEquals(id.toString(), store.getPlayerName(id));
    }

    @Test
    void getAllHoldersReturnsSnapshot() throws IOException {
        AbilityDataStore store = AbilityDataStore.loadOrCreate(tempDir.resolve("test.json"));
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        store.grantAbility(id1);
        store.grantAbility(id2);
        assertEquals(2, store.getAllHolders().size());
        assertTrue(store.getAllHolders().contains(id1));
        assertTrue(store.getAllHolders().contains(id2));
    }

    @Test
    void revokeNonExistentNoException() throws IOException {
        AbilityDataStore store = AbilityDataStore.loadOrCreate(tempDir.resolve("test.json"));
        assertDoesNotThrow(() -> store.revokeAbility(UUID.randomUUID()));
    }
}
