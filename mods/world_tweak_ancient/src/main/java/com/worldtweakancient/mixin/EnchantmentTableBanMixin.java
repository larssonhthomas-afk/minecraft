package com.worldtweakancient.mixin;

import com.worldtweakancient.WorldTweakAncientMod;
import com.worldtweakancient.logic.EnchantmentBanList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentTableBanMixin {

    @Inject(method = "generateEnchantments", at = @At("RETURN"))
    private static void worldTweakAncient$filterBannedEnchantments(
            CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        EnchantmentBanList list = WorldTweakAncientMod.enchantmentBanList();
        if (list == null) return;
        List<EnchantmentLevelEntry> entries = cir.getReturnValue();
        if (entries == null) return;
        entries.removeIf(entry -> {
            if (entry == null) return false;
            return entry.enchantment.getKey()
                    .map(key -> list.isBanned(key.getValue().toString()))
                    .orElse(false);
        });
    }
}
