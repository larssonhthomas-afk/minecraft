package com.rankedsmprank.mixin;

import com.rankedsmprank.RankedSmpRankMod;
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
    private void ranked$addRankPrefix(CallbackInfoReturnable<Text> cir) {
        var ds = RankedSmpRankMod.dataStore();
        if (ds == null) return;

        int tier = ds.getTier(((PlayerEntity) (Object) this).getUuid());

        if (tier < 1 || tier > 9) {
            Text prefix = Text.literal("[Unranked] ").formatted(Formatting.GRAY);
            cir.setReturnValue(Text.empty().append(prefix).append(cir.getReturnValue()));
            return;
        }

        Text prefix = Text.literal("[R" + tier + "] ").formatted(Formatting.GOLD);
        cir.setReturnValue(Text.empty().append(prefix).append(cir.getReturnValue()));
    }
}
