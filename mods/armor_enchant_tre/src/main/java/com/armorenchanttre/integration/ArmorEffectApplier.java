package com.armorenchanttre.integration;

import com.armorenchanttre.logic.ArmorEffects;
import com.armorenchanttre.logic.ArmorEffects.Effect;
import com.armorenchanttre.logic.EnchantmentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

/**
 * Kollar varje server-tick vilka spelare som bär våra enchantade rustningsdelar
 * och refreshar matchande potion effects med kort duration. När rustningen tas av
 * upphör effekten av sig själv (ingen avklingning utöver den korta durationen).
 *
 * För Endurance: även setSprinting(true) varje tick så länge spelaren rör sig framåt
 * och har leggings med Endurance.
 */
public final class ArmorEffectApplier {

    private static final int EFFECT_DURATION_TICKS = 40;

    private ArmorEffectApplier() {}

    public static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            applyTo(player);
        }
    }

    private static void applyTo(ServerPlayerEntity player) {
        ItemStack feet = player.getEquippedStack(EquipmentSlot.FEET);
        ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);

        EnchantmentType feetEnchant = EnchantmentType.fromId(ItemMarker.readMarker(feet));
        EnchantmentType legsEnchant = EnchantmentType.fromId(ItemMarker.readMarker(legs));
        EnchantmentType chestEnchant = EnchantmentType.fromId(ItemMarker.readMarker(chest));

        Set<Effect> effects = ArmorEffects.effectsFor(feetEnchant, legsEnchant, chestEnchant);

        if (effects.contains(Effect.SPEED_1)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, EFFECT_DURATION_TICKS, 0, true, false, false));
        }
        if (effects.contains(Effect.FIRE_RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.FIRE_RESISTANCE, EFFECT_DURATION_TICKS, 0, true, false, false));
        }
        if (effects.contains(Effect.SPRINT_PAST_HUNGER)) {
            if (!player.isSprinting() && player.forwardSpeed > 0) {
                player.setSprinting(true);
            }
        }
    }
}
