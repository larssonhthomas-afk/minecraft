package com.armorenchanttre.mixin;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ForgingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor för att läsa de protected input/output-fälten på ForgingScreenHandler
 * inifrån vår AnvilEnchantMixin (som ärver fälten men inte kan referera till dem
 * via Mixin's compile-tids-modell utan en accessor).
 */
@Mixin(ForgingScreenHandler.class)
public interface ForgingAccessor {

    @Accessor("input")
    Inventory armorEnchantTre$getInput();

    @Accessor("output")
    CraftingResultInventory armorEnchantTre$getOutput();
}
