package com.lifesteal.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LifeStealConfigTest {

    private static final float EPS = 1e-4f;

    @Test
    void defaults_matchHeartManagerDefaults() {
        LifeStealConfig c = LifeStealConfig.defaults();
        assertEquals(4.0f, c.minHealth(), EPS);
        assertEquals(40.0f, c.maxHealth(), EPS);
        assertEquals(1.0f, c.heartsToSteal(), EPS);
    }

    @Test
    void loadOrCreate_missingFile_writesDefaultsAndReturnsThem(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("lifesteal.json");
        assertFalse(Files.exists(path));

        LifeStealConfig c = LifeStealConfig.loadOrCreate(path);

        assertTrue(Files.exists(path));
        assertEquals(LifeStealConfig.defaults(), c);
    }

    @Test
    void loadOrCreate_existingFile_returnsParsedValues(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("lifesteal.json");
        Files.writeString(path, "{\"minHealth\": 2.0, \"maxHealth\": 30.0, \"heartsToSteal\": 2.0}");

        LifeStealConfig c = LifeStealConfig.loadOrCreate(path);

        assertEquals(2.0f, c.minHealth(), EPS);
        assertEquals(30.0f, c.maxHealth(), EPS);
        assertEquals(2.0f, c.heartsToSteal(), EPS);
    }

    @Test
    void loadOrCreate_corruptFile_throws(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("lifesteal.json");
        Files.writeString(path, "{ this is not json");

        assertThrows(IOException.class, () -> LifeStealConfig.loadOrCreate(path));
    }

    @Test
    void loadOrCreate_emptyFile_throws(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("lifesteal.json");
        Files.writeString(path, "");

        assertThrows(IOException.class, () -> LifeStealConfig.loadOrCreate(path));
    }

    @Test
    void write_thenLoad_roundtrips(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("lifesteal.json");
        LifeStealConfig original = new LifeStealConfig(5.0f, 50.0f, 1.5f);
        original.write(path);

        LifeStealConfig loaded = LifeStealConfig.loadOrCreate(path);

        assertEquals(original, loaded);
    }

    @Test
    void write_createsParentDirectories(@TempDir Path dir) throws IOException {
        Path path = dir.resolve("nested").resolve("deeper").resolve("lifesteal.json");
        LifeStealConfig.defaults().write(path);

        assertTrue(Files.exists(path));
    }
}
