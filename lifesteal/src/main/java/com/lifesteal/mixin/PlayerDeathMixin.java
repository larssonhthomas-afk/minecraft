package com.lifesteal.mixin;

import com.lifesteal.LifeStealMod;
import com.lifesteal.logic.HeartManager;
import com.lifesteal.logic.HeartManager.TransferResult;
import com.lifesteal.persistence.HeartDataStore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathMixin {

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void lifesteal$onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;

        ServerPlayerEntity killer = resolveKiller(source, victim);
        if (killer == null) return;

        HeartManager hm = LifeStealMod.heartManager();
        HeartDataStore store = LifeStealMod.dataStore();
        if (hm == null || store == null) return;

        float killerCurrent = currentMaxHealth(killer);
        float victimCurrent = currentMaxHealth(victim);

        TransferResult result = hm.transferHearts(killerCurrent, victimCurrent);
        if (!result.transferOccurred()) {
            return;
        }

        applyMax(killer, result.newKillerMax());
        applyMax(victim, result.newVictimMax());

        store.setMaxHealth(killer.getUuid(), result.newKillerMax());
        store.setMaxHealth(victim.getUuid(), result.newVictimMax());
        try {
            store.save();
        } catch (Exception ex) {
            LifeStealMod.LOGGER.error("Kunde inte spara LifeSteal-data efter kill", ex);
        }

        MinecraftServer server = victim.getServer();
        if (server != null) {
            Text msg = Text.literal("§c" + killer.getName().getString()
                    + " §7stal ett hjärta från §c" + victim.getName().getString());
            server.getPlayerManager().broadcast(msg, false);
        }

        if (result.victimEliminated()) {
            victim.changeGameMode(GameMode.SPECTATOR);
            if (server != null) {
                Text eliminated = Text.literal("§4§l" + victim.getName().getString()
                        + " §char eliminerats och är nu åskådare.");
                server.getPlayerManager().broadcast(eliminated, false);
            }
        }
    }

    private static ServerPlayerEntity resolveKiller(DamageSource source, ServerPlayerEntity victim) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity sp && !sp.getUuid().equals(victim.getUuid())) {
            return sp;
        }
        return null;
    }

    private static float currentMaxHealth(ServerPlayerEntity p) {
        EntityAttributeInstance attr = p.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        return attr != null ? (float) attr.getBaseValue() : 20.0f;
    }

    private static void applyMax(ServerPlayerEntity p, float newMax) {
        EntityAttributeInstance attr = p.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr == null) return;
        attr.setBaseValue(newMax);
        if (p.getHealth() > newMax) {
            p.setHealth(newMax);
        }
    }
}
