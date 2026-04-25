package com.worldtweakancient.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bestämmer vilka item-ID:n som ska filtreras bort ur Bastion chest loot tables.
 * Ren logik utan Minecraft-importer så den kan enhetstestas.
 */
public final class BastionLootBanList {

    public static final String ANCIENT_DEBRIS = "minecraft:ancient_debris";
    public static final String NETHERITE_INGOT = "minecraft:netherite_ingot";

    private final Set<String> banned;

    public BastionLootBanList(Set<String> banned) {
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

    public static BastionLootBanList defaults() {
        Set<String> set = new HashSet<>();
        set.add(ANCIENT_DEBRIS);
        set.add(NETHERITE_INGOT);
        return new BastionLootBanList(set);
    }

    public boolean isBanned(String itemId) {
        if (itemId == null) return false;
        return banned.contains(itemId);
    }

    public Set<String> banned() {
        return banned;
    }
}
