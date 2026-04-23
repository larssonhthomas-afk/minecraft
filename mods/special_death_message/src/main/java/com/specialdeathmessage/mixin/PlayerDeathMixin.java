package com.specialdeathmessage.mixin;

import com.specialdeathmessage.logic.DeathMessageLogic;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathMixin {

    @Redirect(
        method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageTracker;getDeathMessage()Lnet/minecraft/text/Text;")
    )
    private Text specialdeathmessage$replaceDeathMessage(DamageTracker tracker) {
        return Text.literal(DeathMessageLogic.getCustomDeathMessage());
    }
}
