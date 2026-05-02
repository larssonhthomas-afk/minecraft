package com.admintooloperatorer.command;

import com.admintooloperatorer.inventory.PlayerInventoryView;
import com.mojang.brigadier.CommandDispatcher;
import com.rankedsmprank.RankedSmpRankMod;
import com.rankedsmprank.inventory.BagInventory;
import com.rankedsmprank.logic.RankDefinition;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class CheckCommand {

    private CheckCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("check")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("inventory")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(ctx -> checkInventory(
                                                ctx.getSource(),
                                                EntityArgumentType.getPlayer(ctx, "player")))))
                        .then(CommandManager.literal("enderchest")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(ctx -> checkEnderchest(
                                                ctx.getSource(),
                                                EntityArgumentType.getPlayer(ctx, "player")))))
                        .then(CommandManager.literal("bag")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(ctx -> checkBag(
                                                ctx.getSource(),
                                                EntityArgumentType.getPlayer(ctx, "player")))))
        );
    }

    private static int checkInventory(ServerCommandSource source, ServerPlayerEntity target) {
        ServerPlayerEntity admin = source.getPlayer();
        if (admin == null) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        PlayerInventoryView view = new PlayerInventoryView(target.getInventory());
        admin.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X4, syncId, playerInv, view, 4),
                Text.literal("Inventory: " + target.getName().getString())
        ));
        return 1;
    }

    private static int checkEnderchest(ServerCommandSource source, ServerPlayerEntity target) {
        ServerPlayerEntity admin = source.getPlayer();
        if (admin == null) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        admin.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new GenericContainerScreenHandler(
                        ScreenHandlerType.GENERIC_9X3, syncId, playerInv,
                        target.getEnderChestInventory(), 3),
                Text.literal("Ender Chest: " + target.getName().getString())
        ));
        return 1;
    }

    private static int checkBag(ServerCommandSource source, ServerPlayerEntity target) {
        ServerPlayerEntity admin = source.getPlayer();
        if (admin == null) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        var rankStore = RankedSmpRankMod.dataStore();
        if (rankStore == null) {
            source.sendError(Text.literal("Rank system not initialized."));
            return 0;
        }

        int tier = rankStore.getTier(target.getUuid());
        if (!RankDefinition.isValidTier(tier)) {
            source.sendError(Text.literal(target.getName().getString() + " has no rank and no bag."));
            return 0;
        }

        RankDefinition def = RankDefinition.forTier(tier);
        if (!def.hasExtraInventory()) {
            source.sendError(Text.literal(
                    target.getName().getString() + "'s rank (" + def.rankLabel() + ") has no bag."));
            return 0;
        }

        BagInventory bag = RankedSmpRankMod.extraInventoryManager()
                .getOrCreate(target.getUuid(), def.extraSlots());
        int rows = def.extraRows();

        ScreenHandlerType<GenericContainerScreenHandler> type = switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            default -> ScreenHandlerType.GENERIC_9X3;
        };

        admin.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new GenericContainerScreenHandler(
                        type, syncId, playerInv, bag, rows),
                Text.literal("Bag: " + target.getName().getString() + " " + def.rankLabel())
        ));
        return 1;
    }
}
