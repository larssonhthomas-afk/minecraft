package com.dropnr.event;

import com.dropnr.item.KeyItemFactory;
import com.dropnr.item.PlayerHeadFactory;
import com.dropnr.logic.BossKeyRegistry;
import com.dropnr.logic.BossKeyType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * Dirigerar dödshändelser till rätt droppbeteende: offrets player head vid PvP-kill,
 * eller motsvarande bossnyckel när ender dragon, warden eller wither dödas.
 */
public final class DeathDropHandler {

    private DeathDropHandler() {}

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.getWorld() instanceof ServerWorld world)) return;

        if (entity instanceof ServerPlayerEntity victim) {
            handlePlayerDeath(world, victim, source);
            return;
        }

        handleBossDeath(world, entity);
    }

    private static void handlePlayerDeath(ServerWorld world,
                                          ServerPlayerEntity victim,
                                          DamageSource source) {
        Entity attacker = source.getAttacker();
        if (!(attacker instanceof ServerPlayerEntity killer)) return;
        if (killer.getUuid().equals(victim.getUuid())) return;

        ItemStack head = PlayerHeadFactory.create(victim.getGameProfile());
        dropItem(world, victim.getPos(), head);
    }

    private static void handleBossDeath(ServerWorld world, LivingEntity entity) {
        Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
        String idString = id != null ? id.toString() : null;
        Optional<BossKeyType> keyType = BossKeyRegistry.forEntityId(idString);
        keyType.ifPresent(type ->
                dropItem(world, entity.getPos(), KeyItemFactory.create(type)));
    }

    private static void dropItem(ServerWorld world, Vec3d pos, ItemStack stack) {
        ItemEntity ie = new ItemEntity(world, pos.x, pos.y, pos.z, stack);
        ie.setToDefaultPickupDelay();
        world.spawnEntity(ie);
    }
}
