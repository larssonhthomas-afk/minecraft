package com.combatenchantcustom.integration;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class CustomItemCommand {

    private static final String[] TYPES = {"Unbroken_chain"};

    private static final SuggestionProvider<ServerCommandSource> TYPE_SUGGESTIONS =
            (ctx, builder) -> CommandSource.suggestMatching(TYPES, builder);

    private CustomItemCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
                dispatcher.register(CommandManager.literal("give")
                        .then(CommandManager.literal("customitem")
                                .requires(src -> src.hasPermissionLevel(2))
                                .then(CommandManager.argument("type", StringArgumentType.word())
                                        .suggests(TYPE_SUGGESTIONS)
                                        .executes(ctx -> {
                                            ServerCommandSource source = ctx.getSource();
                                            String typeArg = StringArgumentType.getString(ctx, "type");
                                            if (!"Unbroken_chain".equalsIgnoreCase(typeArg)) {
                                                source.sendError(Text.literal("Unknown item: " + typeArg
                                                        + " (valid: Unbroken_chain)"));
                                                return 0;
                                            }
                                            ItemStack item = BookCraftingMatcher.createUnbrokenChainBook();
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            player.getInventory().offerOrDrop(item);
                                            source.sendFeedback(() -> Text.literal("Gave Unbroken_chain book to "
                                                    + player.getName().getString()), true);
                                            return 1;
                                        })))));
    }
}
