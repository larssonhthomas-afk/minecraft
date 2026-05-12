package com.heavenlynr.mixin;

import com.heavenlynr.HeavenlyNRMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 1100)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void heavenlyNR$addHeavenlySuffix(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (HeavenlyNRMod.dataStore() == null) return;
        if (!HeavenlyNRMod.dataStore().hasAbility(player.getUuid())) return;

        Text suffix = Text.literal(" Heavenly")
                .styled(s -> s.withColor(Formatting.GOLD).withBold(false).withItalic(false));
        cir.setReturnValue(Text.empty().append(cir.getReturnValue()).append(suffix));
    }
}
