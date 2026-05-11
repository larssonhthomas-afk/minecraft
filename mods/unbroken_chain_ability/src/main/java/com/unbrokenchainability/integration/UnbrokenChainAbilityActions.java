package com.unbrokenchainability.integration;

import com.unbrokenchainability.UnbrokenChainAbilityMod;
import com.unbrokenchainability.logic.AbilityDataStore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class UnbrokenChainAbilityActions {

    private UnbrokenChainAbilityActions() {}

    public static void grantAbility(ServerPlayerEntity player) {
        AbilityDataStore store = UnbrokenChainAbilityMod.dataStore();
        if (store == null) return;
        store.grantAbility(player.getUuid());
        saveQuiet(store);
        broadcastServer(player,
                Text.literal("§6[UChain] §e" + player.getName().getString() + " §7har fått Unbroken Chain-abilityn!"));
    }

    public static void revokeAbility(ServerPlayerEntity player) {
        AbilityDataStore store = UnbrokenChainAbilityMod.dataStore();
        if (store == null) return;
        store.revokeAbility(player.getUuid());
        AbilityStateManager.resetChain(player);
        saveQuiet(store);
    }

    /** If killer doesn't have the ability and victim does, transfer it. */
    public static void processPvPKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        AbilityDataStore store = UnbrokenChainAbilityMod.dataStore();
        if (store == null) return;

        boolean killerHas = store.hasAbility(killer.getUuid());
        boolean victimHas = store.hasAbility(victim.getUuid());

        if (!killerHas && victimHas) {
            store.revokeAbility(victim.getUuid());
            AbilityStateManager.resetChain(victim);
            store.grantAbility(killer.getUuid());
            saveQuiet(store);
            broadcastServer(killer,
                    Text.literal("§6[UChain] §e" + killer.getName().getString()
                            + " §7tog Unbroken Chain-abilityn från §e"
                            + victim.getName().getString() + "§7!"));
        }
    }

    private static void broadcastServer(ServerPlayerEntity any, Text msg) {
        MinecraftServer server = any.getServer();
        if (server != null) server.getPlayerManager().broadcast(msg, false);
    }

    private static void saveQuiet(AbilityDataStore store) {
        try {
            store.save();
        } catch (Exception e) {
            UnbrokenChainAbilityMod.LOGGER.error("Could not save ability data", e);
        }
    }
}
