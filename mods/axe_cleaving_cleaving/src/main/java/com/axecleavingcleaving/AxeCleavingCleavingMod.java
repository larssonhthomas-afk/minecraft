package com.axecleavingcleaving;

import com.axecleavingcleaving.logic.CleavingLogic;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class AxeCleavingCleavingMod implements ModInitializer {

    public static final String MOD_ID = "axe_cleaving_cleaving";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        registerCommand();
        registerItemUse();
        LOGGER.info("AxeCleavingCleaving initialized");
    }

    private void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                literal("give")
                    .then(literal("customitem")
                        .then(literal("Cleaving")
                            .requires(src -> src.hasPermissionLevel(2))
                            .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                player.getInventory().offerOrDrop(createCleavingBook());
                                ctx.getSource().sendFeedback(
                                    () -> Text.literal("Gave Cleaving book to " + player.getName().getString()), false);
                                return 1;
                            })
                            .then(argument("target", EntityArgumentType.player())
                                .requires(src -> src.hasPermissionLevel(2))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    target.getInventory().offerOrDrop(createCleavingBook());
                                    ctx.getSource().sendFeedback(
                                        () -> Text.literal("Gave Cleaving book to " + target.getName().getString()), false);
                                    return 1;
                                })
                            )
                        )
                    )
            )
        );
    }

    private void registerItemUse() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!(world instanceof ServerWorld)) return ActionResult.PASS;

            ItemStack heldStack = player.getStackInHand(hand);
            if (!isCleavingBook(heldStack)) return ActionResult.PASS;

            Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
            ItemStack otherStack = player.getStackInHand(otherHand);

            String itemId = Registries.ITEM.getId(otherStack.getItem()).toString();
            if (!CleavingLogic.isAxeById(itemId)) {
                player.sendMessage(Text.literal("Hold an axe in the other hand to apply Cleaving"), true);
                return ActionResult.PASS;
            }

            if (hasCleaving(otherStack)) {
                player.sendMessage(Text.literal("This axe already has Cleaving"), true);
                return ActionResult.PASS;
            }

            applyCleaving(otherStack);
            heldStack.decrement(1);
            player.sendMessage(
                Text.literal("Cleaving applied!").styled(s -> s.withColor(Formatting.GOLD).withBold(true)), true);
            return ActionResult.SUCCESS;
        });
    }

    public static ItemStack createCleavingBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponentTypes.CUSTOM_NAME,
            Text.literal("Cleaving").styled(s -> s.withColor(Formatting.GOLD).withBold(true).withItalic(false)));
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean(CleavingLogic.CLEAVING_BOOK_NBT_KEY, true);
        book.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return book;
    }

    public static boolean isCleavingBook(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;
        NbtCompound nbt = component.copyNbt();
        return CleavingLogic.isCleavingBook(nbt.getBoolean(CleavingLogic.CLEAVING_BOOK_NBT_KEY));
    }

    public static boolean hasCleaving(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;
        NbtCompound nbt = component.copyNbt();
        return CleavingLogic.hasCleaving(nbt.getBoolean(CleavingLogic.CLEAVING_NBT_KEY));
    }

    public static void applyCleaving(ItemStack stack) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = existing != null ? existing.copyNbt() : new NbtCompound();
        nbt.putBoolean(CleavingLogic.CLEAVING_NBT_KEY, true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}
