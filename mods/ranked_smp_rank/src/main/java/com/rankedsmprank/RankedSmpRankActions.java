package com.rankedsmprank;

import com.rankedsmprank.logic.RankDefinition;
import com.rankedsmprank.logic.RankManager;
import com.rankedsmprank.persistence.RankDataStore;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;

/**
 * Applies Minecraft side-effects for rank changes and initial rank setup.
 * Called by mixins and commands.
 */
public final class RankedSmpRankActions {

    private RankedSmpRankActions() {}

    /** Apply the player's stored rank (health, name update) on login or rank change. */
    public static void applyRankToPlayer(ServerPlayerEntity player, RankDataStore store) {
        if (store == null) return;
        store.setPlayerName(player.getUuid(), player.getName().getString());

        int tier = store.getTier(player.getUuid());
        if (!RankDefinition.isValidTier(tier)) {
            applyMaxHealth(player, 20.0f);
            return;
        }
        RankDefinition def = RankDefinition.forTier(tier);
        applyMaxHealth(player, def.computeMaxHealth());
    }

    /** Handle a PvP kill: swap ranks if applicable. */
    public static void processPvPKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        RankDataStore store = RankedSmpRankMod.dataStore();
        if (store == null) return;

        int killerTier = store.getTier(killer.getUuid());
        int victimTier = store.getTier(victim.getUuid());

        RankManager.SwapResult swap = RankedSmpRankMod.rankManager()
                .processPvPKill(killer.getUuid(), killerTier, victim.getUuid(), victimTier);

        if (!swap.swapOccurred()) return;

        store.setTier(killer.getUuid(), swap.killerNewTier());
        store.setTier(victim.getUuid(), swap.victimNewTier());
        saveQuiet(store);

        applyRankToPlayer(killer, store);
        applyRankToPlayer(victim, store);

        broadcast(killer,
                Text.literal("§6[Rank] §e" + killer.getName().getString()
                        + " §7(R" + swap.killerOldTier() + " → R" + swap.killerNewTier() + ") "
                        + "killed §e" + victim.getName().getString()
                        + " §7(R" + swap.victimOldTier() + " → R" + swap.victimNewTier() + ")"));
    }

    /** Apply all online players' ranks (called after /rr). */
    public static void applyAllRanks(MinecraftServer server, Map<UUID, Integer> assignments, RankDataStore store) {
        store.clearAllTiers();
        for (Map.Entry<UUID, Integer> e : assignments.entrySet()) {
            store.setTier(e.getKey(), e.getValue());
        }
        saveQuiet(store);

        for (UUID uuid : assignments.keySet()) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(uuid);
            if (p != null) applyRankToPlayer(p, store);
        }
    }

    public static void applyMaxHealth(ServerPlayerEntity player, float maxHp) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr == null) return;
        attr.setBaseValue(maxHp);
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }

    public static float currentMaxHealth(ServerPlayerEntity player) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        return attr != null ? (float) attr.getBaseValue() : 20.0f;
    }

    private static void broadcast(ServerPlayerEntity any, Text msg) {
        MinecraftServer server = any.getServer();
        if (server != null) server.getPlayerManager().broadcast(msg, false);
    }

    public static void saveQuiet(RankDataStore store) {
        try {
            store.save();
        } catch (Exception e) {
            RankedSmpRankMod.LOGGER.error("Could not save rank data", e);
        }
    }
}
