package com.rankedsmprank.mixin;

import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.logic.RankDefinition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class StatusEffectMixin {

    @Unique
    private boolean ranked$scalingEffect = false;

    @Inject(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ranked$scaleEffectDuration(StatusEffectInstance effect, Entity source,
            CallbackInfoReturnable<Boolean> cir) {
        if (ranked$scalingEffect) return;
        if (!((Object) this instanceof ServerPlayerEntity player)) return;

        var ds = RankedSmpRankMod.dataStore();
        if (ds == null) return;

        int tier = ds.getTier(player.getUuid());
        if (!RankDefinition.isValidTier(tier)) return;

        float mult = RankDefinition.forTier(tier).potionMultiplier();
        int dur = effect.getDuration();
        if (mult <= 1.0f || dur <= 0) return;

        StatusEffectInstance scaled = new StatusEffectInstance(
                effect.getEffectType(),
                Math.round(dur * mult),
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.shouldShowParticles(),
                effect.shouldShowIcon()
        );

        ranked$scalingEffect = true;
        try {
            boolean result = ((LivingEntity) (Object) this).addStatusEffect(scaled, source);
            cir.setReturnValue(result);
            cir.cancel();
        } finally {
            ranked$scalingEffect = false;
        }
    }
}
