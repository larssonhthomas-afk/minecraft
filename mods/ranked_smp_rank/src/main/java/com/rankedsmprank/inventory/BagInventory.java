package com.rankedsmprank.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Extra inventory backed by SimpleInventory with locked barrier slots at the tail.
 * Barrier slots cannot be modified and always display a barrier item.
 * Used for ranks whose extra slots don't fill a full 9-slot row (R2, R4).
 */
public class BagInventory extends SimpleInventory {

    private final int usableSlots;

    public BagInventory(int totalSlots, int usableSlots) {
        super(totalSlots);
        this.usableSlots = usableSlots;
        for (int i = usableSlots; i < totalSlots; i++) {
            super.setStack(i, new ItemStack(Items.BARRIER));
        }
    }

    public int getUsableSlots() {
        return usableSlots;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= usableSlots) return;
        super.setStack(slot, stack);
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot >= usableSlots) return ItemStack.EMPTY;
        return super.removeStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot >= usableSlots) return ItemStack.EMPTY;
        return super.removeStack(slot, amount);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot < usableSlots;
    }
}
