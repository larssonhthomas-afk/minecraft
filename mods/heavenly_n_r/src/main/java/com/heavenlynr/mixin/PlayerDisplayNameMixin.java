package com.heavenlynr.mixin;

import com.heavenlynr.PlayerSuffixRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 2000)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void heavenlyNR$addSuffixes(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Text suffixes = PlayerSuffixRegistry.buildSuffixes(player.getUuid());
        if (suffixes == null) return;
        cir.setReturnValue(Text.empty().append(cir.getReturnValue()).append(suffixes));
    }
}
