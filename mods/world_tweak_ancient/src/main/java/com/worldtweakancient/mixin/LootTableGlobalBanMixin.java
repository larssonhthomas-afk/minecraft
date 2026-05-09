package com.worldtweakancient.mixin;

import com.worldtweakancient.WorldTweakAncientMod;
import com.worldtweakancient.logic.GlobalLootBanList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fix: target the List-returning overload explicitly; the void overload
// (LootWorldContext, long, Consumer) would mismatch CallbackInfoReturnable.
@Mixin(LootTable.class)
public abstract class LootTableGlobalBanMixin {

    @Inject(
        method = "generateLoot(Lnet/minecraft/loot/context/LootWorldContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At("RETURN")
    )
    private void worldTweakAncient$filterBannedItems(LootWorldContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        GlobalLootBanList list = WorldTweakAncientMod.globalLootBanList();
        if (list == null) return;
        ObjectArrayList<ItemStack> loot = cir.getReturnValue();
        if (loot == null) return;
        loot.removeIf(stack -> {
            if (stack == null || stack.isEmpty()) return false;
            Identifier id = Registries.ITEM.getId(stack.getItem());
            return id != null && list.isBanned(id.toString());
        });
    }
}
