package com.lifesteal.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistens-lager för varje spelares nuvarande max-HP.
 * Filen ligger som {@code lifesteal.json} i världens root.
 * Format: flatt JSON-objekt med UUID-sträng som nyckel och max-HP som float-värde.
 */
public final class HeartDataStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final java.lang.reflect.Type MAP_TYPE =
            new TypeToken<Map<String, Float>>() {}.getType();

    private final Path path;
    private final float defaultMax;
    private final Map<UUID, Float> maxes;

    private HeartDataStore(Path path, float defaultMax, Map<UUID, Float> maxes) {
        this.path = path;
        this.defaultMax = defaultMax;
        this.maxes = maxes;
    }

    public static HeartDataStore loadOrCreate(Path path, float defaultMax) {
        Map<UUID, Float> parsed = new HashMap<>();
        if (Files.isRegularFile(path)) {
            try {
                String content = Files.readString(path);
                Map<String, Float> raw = GSON.fromJson(content, MAP_TYPE);
                if (raw != null) {
                    for (Map.Entry<String, Float> e : raw.entrySet()) {
                        try {
                            parsed.put(UUID.fromString(e.getKey()), e.getValue());
                        } catch (IllegalArgumentException ignored) {
                            // hoppa över ogiltig UUID-nyckel
                        }
                    }
                }
            } catch (IOException | RuntimeException ex) {
                throw new IllegalStateException("Kunde inte läsa " + path + ": " + ex.getMessage(), ex);
            }
        }
        return new HeartDataStore(path, defaultMax, parsed);
    }

    public synchronized float getMaxHealth(UUID id) {
        return maxes.computeIfAbsent(id, k -> defaultMax);
    }

    public synchronized void setMaxHealth(UUID id, float value) {
        maxes.put(id, value);
    }

    public synchronized boolean has(UUID id) {
        return maxes.containsKey(id);
    }

    public synchronized int size() {
        return maxes.size();
    }

    public synchronized void save() throws IOException {
        Map<String, Float> raw = new HashMap<>(maxes.size());
        for (Map.Entry<UUID, Float> e : maxes.entrySet()) {
            raw.put(e.getKey().toString(), e.getValue());
        }
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = path.resolveSibling(path.getFileName().toString() + ".tmp");
        Files.writeString(tmp, GSON.toJson(raw));
        Files.move(tmp, path,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }

    public synchronized void saveQuiet() {
        try {
            save();
        } catch (IOException ex) {
            throw new IllegalStateException("Kunde inte spara " + path + ": " + ex.getMessage(), ex);
        }
    }

    public Path getPath() {
        return path;
    }
}
