package com.rankedsmprank.command;

import com.mojang.brigadier.CommandDispatcher;
import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.logic.RankDefinition;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class BagCommand {

    private BagCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("bag")
                        .executes(ctx -> run(ctx.getSource()))
        );
    }

    private static int run(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        var store = RankedSmpRankMod.dataStore();
        if (store == null) {
            source.sendError(Text.literal("Rank system not initialized."));
            return 0;
        }

        int tier = store.getTier(player.getUuid());
        if (!RankDefinition.isValidTier(tier)) {
            source.sendError(Text.literal("§cYou need a rank to access extra inventory."));
            return 0;
        }

        RankDefinition def = RankDefinition.forTier(tier);
        if (!def.hasExtraInventory()) {
            source.sendError(Text.literal("§cYour rank (" + def.rankLabel() + ") has no extra inventory."));
            return 0;
        }

        int rows = def.extraRows();
        SimpleInventory inv = RankedSmpRankMod.extraInventoryManager()
                .getOrCreate(player.getUuid(), def.extraSlots());

        ScreenHandlerType<GenericContainerScreenHandler> type = switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            default -> ScreenHandlerType.GENERIC_9X3;
        };

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new GenericContainerScreenHandler(type, syncId, playerInv, inv, rows),
                Text.literal(def.rankLabel() + " Extra Inventory")
        ));
        return 1;
    }
}
