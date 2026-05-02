package com.admintooloperatorer.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Read/write view of the first 36 slots (hotbar + storage) of a PlayerInventory.
 * Changes propagate directly to the target player's inventory.
 */
public final class PlayerInventoryView implements Inventory {

    private final PlayerInventory inv;

    public PlayerInventoryView(PlayerInventory inv) {
        this.inv = inv;
    }

    @Override
    public int size() {
        return 36;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < 36; i++) {
            if (!inv.main.get(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= 36) return ItemStack.EMPTY;
        return inv.main.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot < 0 || slot >= 36) return ItemStack.EMPTY;
        ItemStack result = inv.main.get(slot).split(amount);
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot < 0 || slot >= 36) return ItemStack.EMPTY;
        ItemStack stack = inv.main.get(slot).copy();
        inv.main.set(slot, ItemStack.EMPTY);
        markDirty();
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= 36) return;
        inv.main.set(slot, stack);
        markDirty();
    }

    @Override
    public void markDirty() {
        inv.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; i < 36; i++) {
            inv.main.set(i, ItemStack.EMPTY);
        }
        markDirty();
    }
}
