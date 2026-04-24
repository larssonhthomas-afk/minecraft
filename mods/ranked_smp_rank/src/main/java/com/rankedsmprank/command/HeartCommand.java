package com.rankedsmprank.command;

import com.mojang.brigadier.CommandDispatcher;
import com.rankedsmprank.RankedSmpRankActions;
import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.logic.RankDefinition;
import com.rankedsmprank.persistence.RankDataStore;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class HeartCommand {

    private HeartCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("heart")
                        .then(CommandManager.literal("space")
                                .then(CommandManager.literal("clean")
                                        .executes(ctx -> setClean(ctx.getSource(), true)))
                                .then(CommandManager.literal("reset")
                                        .executes(ctx -> setClean(ctx.getSource(), false))))
        );
    }

    private static int setClean(ServerCommandSource source, boolean clean) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        RankDataStore store = RankedSmpRankMod.dataStore();
        if (store == null) {
            source.sendError(Text.literal("Rank system not initialized."));
            return 0;
        }

        store.setCleanMode(player.getUuid(), clean);
        RankedSmpRankActions.saveQuiet(store);

        int tier = store.getTier(player.getUuid());
        if (RankDefinition.isValidTier(tier)) {
            float maxHp = RankDefinition.forTier(tier).computeMaxHealth(clean);
            RankedSmpRankActions.applyMaxHealth(player, maxHp);
        }

        String mode = clean ? "§bclean §7(compact half-hearts)" : "§adefault §7(full hearts)";
        source.sendFeedback(() -> Text.literal("§7Heart display set to " + mode + "."), false);
        return 1;
    }
}
