package com.heavenlynr.logic;

import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class HeavenlyDataStore {

    private final Path path;
    private final Set<UUID> holders = new HashSet<>();
    private final Map<UUID, Long> cooldownExpiry = new HashMap<>();
    private final Map<UUID, String> playerNames = new HashMap<>();

    private HeavenlyDataStore(Path path) {
        this.path = path;
    }

    public static HeavenlyDataStore loadOrCreate(Path path) throws IOException {
        HeavenlyDataStore store = new HeavenlyDataStore(path);
        if (Files.exists(path)) {
            store.load();
        } else {
            store.save();
        }
        return store;
    }

    private void load() throws IOException {
        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            if (root.has("holders")) {
                for (JsonElement e : root.getAsJsonArray("holders")) {
                    holders.add(UUID.fromString(e.getAsString()));
                }
            }
            if (root.has("cooldowns")) {
                for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("cooldowns").entrySet()) {
                    cooldownExpiry.put(UUID.fromString(e.getKey()), e.getValue().getAsLong());
                }
            }
            if (root.has("names")) {
                for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("names").entrySet()) {
                    playerNames.put(UUID.fromString(e.getKey()), e.getValue().getAsString());
                }
            }
        }
    }

    public synchronized boolean hasAbility(UUID uuid) {
        return holders.contains(uuid);
    }

    public synchronized void grantAbility(UUID uuid) {
        holders.add(uuid);
    }

    public synchronized void revokeAbility(UUID uuid) {
        holders.remove(uuid);
        cooldownExpiry.remove(uuid);
    }

    public synchronized Set<UUID> getAllHolders() {
        return Collections.unmodifiableSet(new HashSet<>(holders));
    }

    public synchronized void triggerCooldown(UUID uuid) {
        cooldownExpiry.put(uuid, System.currentTimeMillis() + HeavenlyLogic.COOLDOWN_MS);
    }

    public synchronized long getCooldownExpiry(UUID uuid) {
        return cooldownExpiry.getOrDefault(uuid, 0L);
    }

    public synchronized boolean isOnCooldown(UUID uuid) {
        long expiry = getCooldownExpiry(uuid);
        return expiry > 0 && System.currentTimeMillis() < expiry;
    }

    public synchronized long remainingCooldownMs(UUID uuid) {
        long expiry = getCooldownExpiry(uuid);
        if (expiry == 0) return 0;
        return Math.max(0, expiry - System.currentTimeMillis());
    }

    public synchronized void setPlayerName(UUID uuid, String name) {
        playerNames.put(uuid, name);
    }

    public synchronized String getPlayerName(UUID uuid) {
        return playerNames.getOrDefault(uuid, uuid.toString());
    }

    public synchronized void save() throws IOException {
        JsonObject root = new JsonObject();

        JsonArray holdersArr = new JsonArray();
        for (UUID u : holders) holdersArr.add(u.toString());
        root.add("holders", holdersArr);

        JsonObject cooldowns = new JsonObject();
        for (Map.Entry<UUID, Long> e : cooldownExpiry.entrySet()) {
            cooldowns.addProperty(e.getKey().toString(), e.getValue());
        }
        root.add("cooldowns", cooldowns);

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
