package com.combatenchantcustom.mixin;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ForgingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ForgingScreenHandler.class)
public interface ForgingAccessor {

    @Accessor("input")
    Inventory combatEnchantCustom$getInput();

    @Accessor("output")
    CraftingResultInventory combatEnchantCustom$getOutput();
}
