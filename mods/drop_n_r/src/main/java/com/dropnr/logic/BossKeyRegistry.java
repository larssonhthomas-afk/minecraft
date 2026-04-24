package com.dropnr.logic;

import java.util.Map;
import java.util.Optional;

/**
 * Kopplar en entitets registry-id (t.ex. "minecraft:ender_dragon") till den nyckel som
 * ska droppas vid dess död. Returnerar tomt när ingen nyckel är kopplad.
 */
public final class BossKeyRegistry {

    public static final String ENDER_DRAGON_ID = "minecraft:ender_dragon";
    public static final String WARDEN_ID = "minecraft:warden";
    public static final String WITHER_ID = "minecraft:wither";

    private static final Map<String, BossKeyType> MAPPING = Map.of(
            ENDER_DRAGON_ID, BossKeyType.DRAGON,
            WARDEN_ID, BossKeyType.WARDEN,
            WITHER_ID, BossKeyType.WITHER
    );

    private BossKeyRegistry() {}

    public static Optional<BossKeyType> forEntityId(String entityId) {
        if (entityId == null || entityId.isEmpty()) return Optional.empty();
        return Optional.ofNullable(MAPPING.get(entityId));
    }
}
