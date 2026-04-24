package com.rankedsmprank.mixin;

import com.rankedsmprank.RankedSmpRankMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        if (tier < 1 || tier > 9) return;

        Formatting color = ranked$tierColor(tier);
        Text prefix = Text.literal("[R" + tier + "] ").formatted(color);
        cir.setReturnValue(Text.empty().append(prefix).append(cir.getReturnValue()));
    }

    @Unique
    private static Formatting ranked$tierColor(int tier) {
        return switch (tier) {
            case 1 -> Formatting.GOLD;
            case 2 -> Formatting.YELLOW;
            case 3 -> Formatting.AQUA;
            case 4 -> Formatting.GREEN;
            case 5 -> Formatting.LIGHT_PURPLE;
            case 6 -> Formatting.RED;
            case 7 -> Formatting.WHITE;
            case 8 -> Formatting.GRAY;
            case 9 -> Formatting.DARK_GRAY;
            default -> Formatting.WHITE;
        };
    }
}
