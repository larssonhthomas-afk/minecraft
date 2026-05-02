package com.admintooloperatorer.command;

import com.admintooloperatorer.logic.RankSetLogic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.rankedsmprank.RankedSmpRankActions;
import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.logic.RankDefinition;
import com.rankedsmprank.persistence.RankDataStore;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

public final class AdminRankCommand {

    private static final List<String> RANK_SUGGESTIONS =
            List.of("R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9");

    private AdminRankCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("rank")
                        .then(CommandManager.literal("set")
                                .requires(src -> src.hasPermissionLevel(2))
                                .then(CommandManager.argument("rank", StringArgumentType.word())
                                        .suggests((ctx, builder) ->
                                                CommandSource.suggestMatching(RANK_SUGGESTIONS, builder))
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .executes(ctx -> rankSet(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "rank"),
                                                        EntityArgumentType.getPlayer(ctx, "player"))))))
        );
    }

    private static int rankSet(ServerCommandSource source, String rankArg, ServerPlayerEntity target) {
        int tier = RankSetLogic.parseTier(rankArg);
        if (!RankDefinition.isValidTier(tier)) {
            source.sendError(Text.literal("Invalid rank: " + rankArg + " (use R1–R9)"));
            return 0;
        }

        RankDataStore store = RankedSmpRankMod.dataStore();
        if (store == null) {
            source.sendError(Text.literal("Rank system not initialized."));
            return 0;
        }

        RankSetLogic.RankSetResult result =
                RankSetLogic.compute(store.getAllTiers(), target.getUuid(), tier);

        // Displace the previous holder if there is one
        UUID prev = result.previousHolder();
        if (prev != null) {
            store.removeTier(prev);
            ServerPlayerEntity prevOnline = source.getServer().getPlayerManager().getPlayer(prev);
            if (prevOnline != null) {
                RankedSmpRankActions.applyRankToPlayer(prevOnline, store);
                prevOnline.sendMessage(Text.literal(
                        "§cYour rank " + RankDefinition.forTier(tier).rankLabel() + " was reassigned by an admin."), false);
            }
        }

        store.setTier(target.getUuid(), tier);
        store.setPlayerName(target.getUuid(), target.getName().getString());
        RankedSmpRankActions.saveQuiet(store);
        RankedSmpRankActions.applyRankToPlayer(target, store);

        String msg = "§aSet " + RankDefinition.forTier(tier).rankLabel()
                + " to §f" + target.getName().getString();
        if (prev != null) {
            msg += " §7(moved from " + store.getPlayerName(prev) + ")";
        }
        final String finalMsg = msg;
        source.sendFeedback(() -> Text.literal(finalMsg), true);
        return 1;
    }
}
