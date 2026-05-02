package com.admintooloperatorer.command;

import com.admintooloperatorer.items.CustomItemFactory;
import com.admintooloperatorer.logic.CustomItemRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Registers /give customitem <player> <item> as a sibling branch to the
 * self-give /give customitem <type> already registered by combat_enchant_custom.
 * Brigadier disambiguates by argument count: one token → self-give, two tokens → admin give.
 */
public final class AdminGiveCustomItemCommand {

    private AdminGiveCustomItemCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("give")
                        .then(CommandManager.literal("customitem")
                                .requires(src -> src.hasPermissionLevel(2))
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("item", StringArgumentType.word())
                                                .suggests((ctx, builder) ->
                                                        CommandSource.suggestMatching(
                                                                CustomItemRegistry.allItemIds(), builder))
                                                .executes(ctx -> giveItem(
                                                        ctx.getSource(),
                                                        EntityArgumentType.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "item"))))))
        );
    }

    private static int giveItem(ServerCommandSource source, ServerPlayerEntity target, String itemId) {
        if (!CustomItemRegistry.isValid(itemId)) {
            source.sendError(Text.literal("Unknown custom item: " + itemId
                    + ". Valid: " + String.join(", ", CustomItemRegistry.allItemIds())));
            return 0;
        }

        ItemStack stack = CustomItemFactory.create(itemId);
        if (stack.isEmpty()) {
            source.sendError(Text.literal("Could not create item: " + itemId));
            return 0;
        }

        target.getInventory().offerOrDrop(stack);
        source.sendFeedback(() -> Text.literal(
                "§aGave §f" + itemId + " §ato §f" + target.getName().getString()), true);
        return 1;
    }
}
