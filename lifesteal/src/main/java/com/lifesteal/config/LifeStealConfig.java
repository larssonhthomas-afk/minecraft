package com.lifesteal.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Konfig-värden som laddas från {@code config/lifesteal.json}.
 * Pure Java; ingen Minecraft-import så klassen kan enhetstestas fristående.
 */
public record LifeStealConfig(float minHealth, float maxHealth, float heartsToSteal) {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static LifeStealConfig defaults() {
        return new LifeStealConfig(4.0f, 40.0f, 1.0f);
    }

    /**
     * Läser config från filen. Skrivs ut med defaults om filen inte finns.
     * Om filen finns men är korrupt, kastas {@link IOException} — anroparen
     * får själv besluta om fallback (vi föredrar att inte tyst skriva över
     * användarens fil även om den är trasig).
     */
    public static LifeStealConfig loadOrCreate(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            LifeStealConfig defaults = defaults();
            defaults.write(path);
            return defaults;
        }
        String content = Files.readString(path);
        LifeStealConfig parsed;
        try {
            parsed = GSON.fromJson(content, LifeStealConfig.class);
        } catch (JsonSyntaxException ex) {
            throw new IOException("Ogiltig JSON i " + path + ": " + ex.getMessage(), ex);
        }
        if (parsed == null) {
            throw new IOException("Tom config-fil: " + path);
        }
        return parsed;
    }

    public void write(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, GSON.toJson(this));
    }
}
