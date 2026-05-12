package com.heavenlynr.mixin;

import com.heavenlynr.HeavenlyClientState;
import com.heavenlynr.HeavenlyNRMod;
import com.heavenlynr.logic.HeavenlyDataStore;
import com.heavenlynr.logic.HeavenlyLogic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 9999)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void heavenlyNR$addSuffixes(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        boolean hasAbility;
        if (player.getWorld().isClient()) {
            hasAbility = HeavenlyClientState.hasAbility(player.getUuid());
        } else {
            HeavenlyDataStore store = HeavenlyNRMod.dataStore();
            hasAbility = store != null && store.hasAbility(player.getUuid());
        }
        if (!hasAbility) return;
        Text suffix = Text.literal(HeavenlyLogic.DISPLAY_SUFFIX)
                .styled(s -> s.withColor(Formatting.GOLD).withBold(false).withItalic(false));
        cir.setReturnValue(Text.empty().append(cir.getReturnValue()).append(suffix));
    }
}
