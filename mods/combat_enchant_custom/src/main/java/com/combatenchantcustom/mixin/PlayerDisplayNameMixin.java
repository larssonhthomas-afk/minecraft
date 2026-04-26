package com.combatenchantcustom.mixin;

import com.combatenchantcustom.integration.ItemMarker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void combatEnchantCustom$addUChainSuffix(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerInventory inv = player.getInventory();

        boolean hasChainSword = false;
        for (ItemStack stack : inv.main) {
            if (ItemMarker.hasChainEnchant(stack)) {
                hasChainSword = true;
                break;
            }
        }
        if (!hasChainSword) {
            for (ItemStack stack : inv.offHand) {
                if (ItemMarker.hasChainEnchant(stack)) {
                    hasChainSword = true;
                    break;
                }
            }
        }

        if (!hasChainSword) return;

        Text suffix = Text.literal(" UChain")
                .styled(s -> s.withColor(Formatting.GOLD).withBold(true));
        cir.setReturnValue(Text.empty().append(cir.getReturnValue()).append(suffix));
    }
}
