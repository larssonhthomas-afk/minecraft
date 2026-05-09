package com.worldtweakancient.mixin;

import com.worldtweakancient.WorldTweakAncientMod;
import com.worldtweakancient.logic.GlobalLootBanList;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(LootTable.class)
public abstract class LootTableGlobalBanMixin {

    @Inject(method = "generateLoot", at = @At("RETURN"))
    private void worldTweakAncient$filterBannedItems(LootWorldContext context, CallbackInfoReturnable<List<ItemStack>> cir) {
        GlobalLootBanList list = WorldTweakAncientMod.globalLootBanList();
        if (list == null) return;
        List<ItemStack> loot = cir.getReturnValue();
        if (loot == null) return;
        List<ItemStack> filtered = new ArrayList<>(loot);
        filtered.removeIf(stack -> {
            if (stack == null || stack.isEmpty()) return false;
            Identifier id = Registries.ITEM.getId(stack.getItem());
            return id != null && list.isBanned(id.toString());
        });
        cir.setReturnValue(filtered);
    }
}
