package com.worldtweakancient.logic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bestämmer vilka entitets-ID:n som inte ska få spawnas alls.
 * Ren logik utan Minecraft-importer så den kan enhetstestas.
 */
public final class EntitySpawnBlockList {

    public static final String ZOMBIFIED_PIGLIN = "minecraft:zombified_piglin";

    private final Set<String> blocked;

    public EntitySpawnBlockList(Set<String> blocked) {
        if (blocked == null) {
            throw new IllegalArgumentException("blocked får inte vara null");
        }
        for (String id : blocked) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("blocked får inte innehålla null/blanka ID:n");
            }
        }
        this.blocked = Collections.unmodifiableSet(new HashSet<>(blocked));
    }

    public static EntitySpawnBlockList defaults() {
        Set<String> set = new HashSet<>();
        set.add(ZOMBIFIED_PIGLIN);
        return new EntitySpawnBlockList(set);
    }

    public boolean isSpawnBlocked(String entityTypeId) {
        if (entityTypeId == null) return false;
        return blocked.contains(entityTypeId);
    }

    public Set<String> blocked() {
        return blocked;
    }
}
