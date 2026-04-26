package com.combatenchantcustom.mixin;

import com.combatenchantcustom.integration.ChainStateManager;
import com.combatenchantcustom.integration.ItemMarker;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerHurtMixin {

    // In MC 1.21.4, damage signature is damage(ServerWorld, DamageSource, float)
    @Inject(method = "damage", at = @At("HEAD"))
    private void combatEnchantCustom$resetChainOnDamage(ServerWorld world, DamageSource source,
            float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ItemStack mainHand = self.getMainHandStack();
        if (ItemMarker.hasChainEnchant(mainHand)) {
            ChainStateManager.resetChain(self);
        }
    }
}
