package com.rankedsmprank.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player extra inventory (BagInventory) in memory.
 * Sized by usable-slot count; barrier padding is added automatically to the last row.
 * Inventories persist for the lifetime of the server session.
 */
public class ExtraInventoryManager {

    private final Map<UUID, BagInventory> inventories = new HashMap<>();

    /**
     * Returns the player's extra inventory, creating or reshaping it to match
     * the given usable-slot count. Copies carryover items from any existing inventory.
     */
    public synchronized BagInventory getOrCreate(UUID playerId, int usableSlots) {
        int total = ((usableSlots + 8) / 9) * 9;

        BagInventory existing = inventories.get(playerId);
        if (existing != null && existing.size() == total && existing.getUsableSlots() == usableSlots) {
            return existing;
        }

        BagInventory fresh = new BagInventory(total, usableSlots);
        if (existing != null) {
            int copy = Math.min(existing.getUsableSlots(), usableSlots);
            for (int i = 0; i < copy; i++) {
                fresh.setStack(i, existing.getStack(i));
            }
        }
        inventories.put(playerId, fresh);
        return fresh;
    }

    /**
     * Resizes the inventory to newUsableSlots. Returns usable items that no longer fit as drops.
     * If newUsableSlots <= 0, removes the inventory entirely and returns all usable items.
     */
    public synchronized List<ItemStack> resizeAndGetDrops(UUID playerId, int newUsableSlots) {
        BagInventory existing = inventories.get(playerId);

        if (newUsableSlots <= 0) {
            inventories.remove(playerId);
            return existing == null ? List.of() : extractUsableItems(existing);
        }

        int newTotal = ((newUsableSlots + 8) / 9) * 9;

        if (existing == null) {
            inventories.put(playerId, new BagInventory(newTotal, newUsableSlots));
            return List.of();
        }

        if (existing.size() == newTotal && existing.getUsableSlots() == newUsableSlots) {
            return List.of();
        }

        int copy = Math.min(existing.getUsableSlots(), newUsableSlots);
        List<ItemStack> drops = new ArrayList<>();
        for (int i = copy; i < existing.getUsableSlots(); i++) {
            ItemStack stack = existing.getStack(i);
            if (!stack.isEmpty()) drops.add(stack);
        }

        BagInventory fresh = new BagInventory(newTotal, newUsableSlots);
        for (int i = 0; i < copy; i++) {
            fresh.setStack(i, existing.getStack(i));
        }
        inventories.put(playerId, fresh);
        return drops;
    }

    /**
     * Removes the inventory entirely and returns all usable items it contained (barriers excluded).
     */
    public synchronized List<ItemStack> clearAndGetItems(UUID playerId) {
        BagInventory existing = inventories.remove(playerId);
        return existing == null ? List.of() : extractUsableItems(existing);
    }

    public synchronized void remove(UUID playerId) {
        inventories.remove(playerId);
    }

    private static List<ItemStack> extractUsableItems(BagInventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inv.getUsableSlots(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        return items;
    }
}
