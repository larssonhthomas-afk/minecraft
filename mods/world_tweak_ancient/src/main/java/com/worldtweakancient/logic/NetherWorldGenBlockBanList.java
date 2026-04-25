package com.worldtweakancient.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bestämmer vilka block-ID:n som ska blockeras under världsgenerering i Nether.
 * Används främst för att hindra Gold Blocks från att placeras i Bastions.
 * Ren logik utan Minecraft-importer.
 */
public final class NetherWorldGenBlockBanList {

    public static final String GOLD_BLOCK = "minecraft:gold_block";
    public static final String NETHER_DIMENSION_ID = "minecraft:the_nether";

    private final Set<String> banned;

    public NetherWorldGenBlockBanList(Set<String> banned) {
        if (banned == null) {
            throw new IllegalArgumentException("banned får inte vara null");
        }
        for (String id : banned) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("banned får inte innehålla null/blanka ID:n");
            }
        }
        this.banned = Collections.unmodifiableSet(new HashSet<>(banned));
    }

    public static NetherWorldGenBlockBanList defaults() {
        Set<String> set = new HashSet<>();
        set.add(GOLD_BLOCK);
        return new NetherWorldGenBlockBanList(set);
    }

    /**
     * @return true om blocket ska blockeras (dvs hoppas över) i Netherns världsgenerering
     */
    public boolean shouldBlockInNether(String dimensionId, String blockId) {
        if (!NETHER_DIMENSION_ID.equals(dimensionId)) return false;
        if (blockId == null) return false;
        return banned.contains(blockId);
    }

    public Set<String> banned() {
        return banned;
    }
}
