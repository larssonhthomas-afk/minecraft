package com.worldtweakancient.mixin;

import com.worldtweakancient.WorldTweakAncientMod;
import com.worldtweakancient.logic.EnchantmentBanList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.village.TradeOffers$EnchantBookFactory")
public abstract class VillagerEnchantBookTradeMixin {

    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private void worldTweakAncient$filterBannedEnchantmentTrades(
            CallbackInfoReturnable<TradeOffer> cir) {
        TradeOffer offer = cir.getReturnValue();
        if (offer == null) return;
        EnchantmentBanList list = WorldTweakAncientMod.enchantmentBanList();
        if (list == null) return;
        ItemStack sellItem = offer.getSellItem();
        if (sellItem.isEmpty()) return;
        ItemEnchantmentsComponent enchantments = sellItem.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (enchantments == null) return;
        for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
            boolean banned = enchantment.getKey()
                    .map(key -> list.isBanned(key.getValue().toString()))
                    .orElse(false);
            if (banned) {
                cir.setReturnValue(null);
                return;
            }
        }
    }
}
