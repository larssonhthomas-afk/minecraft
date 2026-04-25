package com.armorenchanttre.integration;

import com.armorenchanttre.logic.EnchantmentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Registrerar /give customitem <immunity|endurance|extinguish> som en op-only-kommand
 * (permission level 2). Lägger den färdig-enchantade boken direkt i spelarens inventory.
 */
public final class CustomItemCommand {

    private static final SuggestionProvider<ServerCommandSource> TYPE_SUGGESTIONS =
            (ctx, builder) -> CommandSource.suggestMatching(
                    new String[]{"immunity", "endurance", "extinguish"}, builder);

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
                                            EnchantmentType type = EnchantmentType.fromId(typeArg);
                                            if (type == null) {
                                                source.sendError(Text.literal("Okänd enchantment-typ: " + typeArg
                                                        + " (giltiga: immunity, endurance, extinguish)"));
                                                return 0;
                                            }
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            ItemStack book = BookCraftingMatcher.createEnchantedBook(type);
                                            player.getInventory().offerOrDrop(book);
                                            source.sendFeedback(() -> Text.literal("Gav "
                                                    + type.bookName() + "-bok till "
                                                    + player.getName().getString()), true);
                                            return 1;
                                        })))));
    }
}
