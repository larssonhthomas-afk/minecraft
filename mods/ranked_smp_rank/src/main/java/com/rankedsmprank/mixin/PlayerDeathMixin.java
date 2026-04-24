package com.rankedsmprank.mixin;

import com.rankedsmprank.RankedSmpRankActions;
import com.rankedsmprank.RankedSmpRankMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathMixin {

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void ranked$onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;
        MinecraftServer server = victim.getServer();
        if (server != null && !server.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            dropBagItems(victim);
        }
        if (source.getAttacker() instanceof ServerPlayerEntity killer
                && !killer.getUuid().equals(victim.getUuid())) {
            RankedSmpRankActions.processPvPKill(killer, victim);
        }
    }

    private static void dropBagItems(ServerPlayerEntity player) {
        var invManager = RankedSmpRankMod.extraInventoryManager();
        if (invManager == null) return;
        List<ItemStack> items = invManager.clearAndGetItems(player.getUuid());
        for (ItemStack stack : items) {
            player.dropItem(stack, true, false);
        }
    }
}
