package com.rankedsmprank.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * Only grows the inventory — never shrinks. Use resizeAndGetDrops for rank changes.
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

    /**
     * Resizes the inventory to newSlots. Returns items that no longer fit as drops.
     * If newSlots <= 0, removes the inventory entirely and returns all items.
     */
    public synchronized List<ItemStack> resizeAndGetDrops(UUID playerId, int newSlots) {
        SimpleInventory existing = inventories.get(playerId);

        if (newSlots <= 0) {
            inventories.remove(playerId);
            if (existing == null) return List.of();
            List<ItemStack> drops = new ArrayList<>();
            for (int i = 0; i < existing.size(); i++) {
                ItemStack stack = existing.getStack(i);
                if (!stack.isEmpty()) drops.add(stack);
            }
            return drops;
        }

        int actualSlots = ((newSlots + 8) / 9) * 9;

        if (existing == null) {
            inventories.put(playerId, new SimpleInventory(actualSlots));
            return List.of();
        }

        if (existing.size() == actualSlots) {
            return List.of();
        }

        List<ItemStack> drops = new ArrayList<>();
        if (existing.size() > actualSlots) {
            for (int i = actualSlots; i < existing.size(); i++) {
                ItemStack stack = existing.getStack(i);
                if (!stack.isEmpty()) drops.add(stack);
            }
        }

        SimpleInventory fresh = new SimpleInventory(actualSlots);
        for (int i = 0; i < Math.min(actualSlots, existing.size()); i++) {
            fresh.setStack(i, existing.getStack(i));
        }
        inventories.put(playerId, fresh);
        return drops;
    }

    /**
     * Removes the inventory entirely and returns all items it contained.
     */
    public synchronized List<ItemStack> clearAndGetItems(UUID playerId) {
        SimpleInventory existing = inventories.remove(playerId);
        if (existing == null) return List.of();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < existing.size(); i++) {
            ItemStack stack = existing.getStack(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        return items;
    }

    public synchronized void remove(UUID playerId) {
        inventories.remove(playerId);
    }
}
