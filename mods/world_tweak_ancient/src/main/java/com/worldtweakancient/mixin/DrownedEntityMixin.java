package com.worldtweakancient.mixin;

import com.worldtweakancient.WorldTweakAncientMod;
import com.worldtweakancient.logic.DrownedEquipmentRules;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrownedEntity.class)
public abstract class DrownedEntityMixin {

    @Inject(method = "initEquipment(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/world/LocalDifficulty;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void worldTweakAncient$noTrident(Random random, LocalDifficulty difficulty, CallbackInfo ci) {
        DrownedEquipmentRules rules = WorldTweakAncientMod.drownedRules();
        if (rules != null && rules.shouldSkipMainHandAssignment()) {
            ci.cancel();
        }
    }
}
