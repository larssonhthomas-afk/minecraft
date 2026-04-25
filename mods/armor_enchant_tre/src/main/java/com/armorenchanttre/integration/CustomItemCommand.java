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

    private static final String[] TYPES = {
            "immunity", "endurance", "extinguish",
            "dragon_key", "warden_key", "wither_key",
            "neutral_player_head"
    };

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
                                            ItemStack item = createCustomItem(typeArg);
                                            if (item == null) {
                                                source.sendError(Text.literal("Okänd custom-item: " + typeArg
                                                        + " (giltiga: " + String.join(", ", TYPES) + ")"));
                                                return 0;
                                            }
                                            ServerPlayerEntity player = source.getPlayerOrThrow();
                                            String label = item.getName().getString();
                                            player.getInventory().offerOrDrop(item);
                                            source.sendFeedback(() -> Text.literal("Gav " + label
                                                    + " till " + player.getName().getString()), true);
                                            return 1;
                                        })))));
    }

    private static ItemStack createCustomItem(String typeArg) {
        EnchantmentType enchant = EnchantmentType.fromId(typeArg);
        if (enchant != null) {
            return BookCraftingMatcher.createEnchantedBook(enchant);
        }
        return switch (typeArg) {
            case "dragon_key" -> BookCraftingMatcher.createKey(EnchantmentType.IMMUNITY);
            case "warden_key" -> BookCraftingMatcher.createKey(EnchantmentType.ENDURANCE);
            case "wither_key" -> BookCraftingMatcher.createKey(EnchantmentType.EXTINGUISH);
            case "neutral_player_head" -> BookCraftingMatcher.createNeutralPlayerHead();
            default -> null;
        };
    }
}
