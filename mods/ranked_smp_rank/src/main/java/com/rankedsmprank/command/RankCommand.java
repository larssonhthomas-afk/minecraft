package com.rankedsmprank.command;

import com.mojang.brigadier.CommandDispatcher;
import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.logic.RankDefinition;
import com.rankedsmprank.persistence.RankDataStore;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class RankCommand {

    private RankCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("rank")
                        .executes(ctx -> run(ctx.getSource()))
        );
    }

    private static int run(ServerCommandSource source) {
        RankDataStore store = RankedSmpRankMod.dataStore();
        if (store == null) {
            source.sendError(Text.literal("Rank system not yet initialized."));
            return 0;
        }

        Map<UUID, Integer> all = store.getAllTiers();
        if (all.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§7No ranks assigned yet. An operator can use §f/rr§7 to randomize."), false);
            return 1;
        }

        TreeMap<Integer, UUID> byTier = new TreeMap<>();
        for (Map.Entry<UUID, Integer> e : all.entrySet()) {
            if (!RankDefinition.isValidTier(e.getValue())) continue;
            byTier.put(e.getValue(), e.getKey());
        }

        StringBuilder sb = new StringBuilder("§6=== Rank List ===\n");
        for (Map.Entry<Integer, UUID> e : byTier.entrySet()) {
            int tier = e.getKey();
            UUID uuid = e.getValue();
            String name = store.getPlayerName(uuid);
            RankDefinition def = RankDefinition.forTier(tier);
            boolean online = source.getServer() != null
                    && source.getServer().getPlayerManager().getPlayer(uuid) != null;
            String status = online ? "§a(online)" : "§8(offline)";
            sb.append(ranked$tierColor(tier)).append("[R").append(tier).append("] ")
              .append("§f").append(name).append(" ")
              .append(status).append("§7 — ")
              .append((int) def.rankHp()).append("HP, +")
              .append(Math.round((def.potionMultiplier() - 1) * 100)).append("% pots, +")
              .append(Math.round((def.xpMultiplier() - 1) * 100)).append("% XP")
              .append("\n");
        }

        String msg = sb.toString().stripTrailing();
        source.sendFeedback(() -> Text.literal(msg), false);
        return byTier.size();
    }

    static String ranked$tierColor(int tier) {
        return switch (tier) {
            case 1 -> "§6";
            case 2 -> "§e";
            case 3 -> "§b";
            case 4 -> "§a";
            case 5 -> "§d";
            case 6 -> "§c";
            case 7 -> "§f";
            case 8 -> "§7";
            case 9 -> "§8";
            default -> "§f";
        };
    }
}
