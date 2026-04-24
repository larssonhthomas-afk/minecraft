package com.rankedsmprank.inventory;

import net.minecraft.inventory.SimpleInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player extra inventory slots in memory.
 * Inventories persist for the lifetime of the server session.
 */
public class ExtraInventoryManager {

    private final Map<UUID, SimpleInventory> inventories = new HashMap<>();

    /**
     * Returns the player's extra inventory, creating one if needed.
     * If the player's size increased, a new larger inventory is created and
     * existing items are migrated. If the size decreased, the existing
     * inventory is kept (items are preserved; fewer slots are accessible).
     */
    public synchronized SimpleInventory getOrCreate(UUID playerId, int slots) {
        SimpleInventory existing = inventories.get(playerId);
        if (existing != null && existing.size() >= slots) {
            return existing;
        }
        SimpleInventory fresh = new SimpleInventory(Math.max(slots, 9));
        if (existing != null) {
            for (int i = 0; i < Math.min(existing.size(), fresh.size()); i++) {
                fresh.setStack(i, existing.getStack(i));
            }
        }
        inventories.put(playerId, fresh);
        return fresh;
    }

    public synchronized void remove(UUID playerId) {
        inventories.remove(playerId);
    }
}
