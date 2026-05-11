package com.unbrokenchainability.mixin;

import com.unbrokenchainability.UnbrokenChainAbilityMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void unbrokenChainAbility$addUChainSuffix(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (UnbrokenChainAbilityMod.dataStore() == null) return;
        if (!UnbrokenChainAbilityMod.dataStore().hasAbility(player.getUuid())) return;

        Text suffix = Text.literal(" [⛓]")
                .styled(s -> s.withColor(Formatting.GOLD).withBold(true));
        cir.setReturnValue(Text.empty().append(cir.getReturnValue()).append(suffix));
    }
}
