package com.unbrokenchainability.mixin;

import com.unbrokenchainability.UnbrokenChainAbilityMod;
import com.unbrokenchainability.integration.AbilityStateManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDamageMixin {

    // ServerPlayerEntity.damage(ServerWorld, DamageSource, float) — method_64397
    @Inject(method = "damage", at = @At("HEAD"))
    private void unbrokenChainAbility$resetChainOnDamage(ServerWorld world, DamageSource source,
            float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (UnbrokenChainAbilityMod.dataStore() != null
                && UnbrokenChainAbilityMod.dataStore().hasAbility(self.getUuid())) {
            AbilityStateManager.resetChain(self);
        }
    }
}
