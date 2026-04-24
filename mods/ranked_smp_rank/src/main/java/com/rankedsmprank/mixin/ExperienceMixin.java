package com.rankedsmprank.mixin;

import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.logic.RankDefinition;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class ExperienceMixin {

    @ModifyVariable(method = "addExperience(I)V", at = @At("HEAD"), argsOnly = true)
    private int ranked$scaleXp(int amount) {
        var ds = RankedSmpRankMod.dataStore();
        if (ds == null) return amount;

        int tier = ds.getTier(((PlayerEntity) (Object) this).getUuid());
        if (!RankDefinition.isValidTier(tier)) return amount;

        float mult = RankDefinition.forTier(tier).xpMultiplier();
        if (mult <= 1.0f) return amount;
        return Math.round(amount * mult);
    }
}
