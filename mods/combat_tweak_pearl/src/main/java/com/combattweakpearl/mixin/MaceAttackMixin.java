package com.combattweakpearl.mixin;

import com.combattweakpearl.logic.MaceDamageLogic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public abstract class MaceAttackMixin {

    // MC 1.21.4: PlayerEntity.attack() calls Entity.sidedDamage() — not Entity.damage()
    @ModifyArg(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;sidedDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            ),
            index = 1
    )
    private float combatTweakPearl$nerfMaceDamage(float amount) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!self.getMainHandStack().isOf(Items.MACE)) return amount;
        return MaceDamageLogic.applyNerfedDamage(amount);
    }
}
