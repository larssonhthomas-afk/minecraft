package com.rankedsmprank.persistence;

import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * JSON-backed persistence for rank assignments and player names.
 * Thread-safe via synchronized methods.
 */
public class RankDataStore {

    private final Path path;
    private final Map<UUID, Integer> playerTiers = new HashMap<>();
    private final Map<UUID, String> playerNames = new HashMap<>();

    private RankDataStore(Path path) {
        this.path = path;
    }

    public static RankDataStore loadOrCreate(Path path) throws IOException {
        RankDataStore store = new RankDataStore(path);
        if (Files.exists(path)) {
            store.load();
        } else {
            store.save();
        }
        return store;
    }

    private void load() throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("tiers")) {
                for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("tiers").entrySet()) {
                    playerTiers.put(UUID.fromString(e.getKey()), e.getValue().getAsInt());
                }
            }
            if (root.has("names")) {
                for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("names").entrySet()) {
                    playerNames.put(UUID.fromString(e.getKey()), e.getValue().getAsString());
                }
            }
            // "cleanMode" field is ignored (feature removed)
        }
    }

    public synchronized int getTier(UUID uuid) {
        return playerTiers.getOrDefault(uuid, -1);
    }

    public synchronized void setTier(UUID uuid, int tier) {
        playerTiers.put(uuid, tier);
    }

    public synchronized void clearAllTiers() {
        playerTiers.clear();
    }

    public synchronized String getPlayerName(UUID uuid) {
        return playerNames.getOrDefault(uuid, uuid.toString());
    }

    public synchronized void setPlayerName(UUID uuid, String name) {
        playerNames.put(uuid, name);
    }

    /** Returns an unmodifiable snapshot of all current tier assignments. */
    public synchronized Map<UUID, Integer> getAllTiers() {
        return Collections.unmodifiableMap(new HashMap<>(playerTiers));
    }

    public synchronized void save() throws IOException {
        JsonObject root = new JsonObject();

        JsonObject tiers = new JsonObject();
        for (Map.Entry<UUID, Integer> e : playerTiers.entrySet()) {
            tiers.addProperty(e.getKey().toString(), e.getValue());
        }
        root.add("tiers", tiers);

        JsonObject names = new JsonObject();
        for (Map.Entry<UUID, String> e : playerNames.entrySet()) {
            names.addProperty(e.getKey().toString(), e.getValue());
        }
        root.add("names", names);

        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer w = Files.newBufferedWriter(tmp)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, w);
        }
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
