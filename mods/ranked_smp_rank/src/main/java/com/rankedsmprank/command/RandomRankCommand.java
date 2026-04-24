package com.rankedsmprank.command;

import com.mojang.brigadier.CommandDispatcher;
import com.rankedsmprank.RankedSmpRankActions;
import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.logic.RankDefinition;
import com.rankedsmprank.logic.RankManager;
import com.rankedsmprank.persistence.RankDataStore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public final class RandomRankCommand {

    private RandomRankCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("rr")
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> run(ctx.getSource()))
        );
    }

    private static int run(ServerCommandSource source) {
        RankDataStore store = RankedSmpRankMod.dataStore();
        MinecraftServer server = source.getServer();
        if (store == null || server == null) {
            source.sendError(Text.literal("Rank system not initialized."));
            return 0;
        }

        List<UUID> online = new ArrayList<>();
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            online.add(p.getUuid());
        }

        if (online.isEmpty()) {
            source.sendError(Text.literal("No players online to assign ranks to."));
            return 0;
        }

        RankManager.AssignResult result = RankedSmpRankMod.rankManager()
                .assignRanks(online, new Random());

        RankedSmpRankActions.applyAllRanks(server, result.assignments(), store);

        StringBuilder sb = new StringBuilder("§6=== Ranks Randomized! ===\n");
        Map<Integer, UUID> byTier = new TreeMap<>();
        for (Map.Entry<UUID, Integer> e : result.assignments().entrySet()) {
            byTier.put(e.getValue(), e.getKey());
        }
        for (Map.Entry<Integer, UUID> e : byTier.entrySet()) {
            int tier = e.getKey();
            String name = store.getPlayerName(e.getValue());
            sb.append(RankCommand.ranked$tierColor(tier))
              .append("[R").append(tier).append("] §f").append(name).append("\n");
        }
        int missing = 9 - result.assignments().size();
        if (missing > 0) {
            sb.append("§8(").append(missing).append(" rank(s) unassigned — not enough online players)\n");
        }

        String msg = sb.toString().stripTrailing();
        server.getPlayerManager().broadcast(Text.literal(msg), false);
        return result.assignments().size();
    }
}
