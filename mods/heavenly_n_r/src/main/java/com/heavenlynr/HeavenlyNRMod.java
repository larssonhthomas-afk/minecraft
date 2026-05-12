package com.heavenlynr;

import com.heavenlynr.logic.HeavenlyDataStore;
import com.heavenlynr.logic.HeavenlyLogic;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class HeavenlyNRMod implements ModInitializer {

    public static final String MOD_ID = "heavenly_n_r";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static HeavenlyDataStore dataStore;
    private static MinecraftServer server;

    public static HeavenlyDataStore dataStore() {
        return dataStore;
    }

    @Override
    public void onInitialize() {
        PlayerSuffixRegistry.register(uuid -> {
            HeavenlyDataStore store = dataStore;
            if (store == null || !store.hasAbility(uuid)) return null;
            return Text.literal(HeavenlyLogic.DISPLAY_SUFFIX)
                    .styled(s -> s.withColor(Formatting.GOLD).withBold(false).withItalic(false));
        });
        registerLifecycleEvents();
        registerDeathEvent();
        registerItemUse();
        registerTickEvent();
        registerCommands();
        LOGGER.info("HeavenlyNR initialized");
    }

    private void registerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(srv -> {
            server = srv;
            Path dataPath = srv.getSavePath(WorldSavePath.ROOT).resolve("heavenly_n_r.json");
            try {
                dataStore = HeavenlyDataStore.loadOrCreate(dataPath);
                LOGGER.info("HeavenlyNR data loaded from {}", dataPath);
            } catch (Exception e) {
                LOGGER.error("Failed to load Heavenly data", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(srv -> {
            if (dataStore != null) {
                try {
                    dataStore.save();
                } catch (Exception e) {
                    LOGGER.error("Failed to save Heavenly data on shutdown", e);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, srv) -> {
            if (dataStore != null) {
                dataStore.setPlayerName(handler.player.getUuid(), handler.player.getName().getString());
            }
        });
    }

    private void registerDeathEvent() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            if (dataStore == null) return true;

            UUID uuid = player.getUuid();
            if (!dataStore.hasAbility(uuid)) return true;
            if (dataStore.isOnCooldown(uuid)) return true; // protection on cooldown — player dies, PvP transfer handled by mixin

            applyHeavenlyProtection(player);
            return false; // cancel death
        });
    }

    private static void applyHeavenlyProtection(ServerPlayerEntity player) {
        // Restore to 1 HP so player is not stuck at 0
        player.setHealth(1.0f);

        // Totem-equivalent effects
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 900, 0, false, true, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1, false, true, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1, false, true, true));

        // Broadcast totem animation to nearby clients (entity status 35 = USE_TOTEM_OF_UNDYING)
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.sendEntityStatus(player, (byte) 35);
        }

        // Damage helmet by 40% of its max durability
        damageHelmet(player);

        // Start 20-minute cooldown
        dataStore.triggerCooldown(player.getUuid());
        saveQuiet();

        player.sendMessage(
                Text.literal("§6[Heavenly] §7Din Heavenly-förmåga räddade dig! Cooldown: §e20:00"), true);
    }

    private static void damageHelmet(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !helmet.isDamageable()) return;

        int maxDamage = helmet.getMaxDamage();
        int additionalDamage = Math.max(1, (int) (maxDamage * HeavenlyLogic.HELMET_DAMAGE_FRACTION));
        int newDamage = helmet.getDamage() + additionalDamage;

        if (newDamage >= maxDamage) {
            player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        } else {
            helmet.setDamage(newDamage);
        }
    }

    private void registerItemUse() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!isHeavenlyBook(stack)) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            if (dataStore != null && dataStore.hasAbility(serverPlayer.getUuid())) {
                serverPlayer.sendMessage(
                        Text.literal("§6[Heavenly] §7Du har redan Heavenly-förmågan."), false);
                return ActionResult.FAIL;
            }

            stack.decrement(1);
            grantAbility(serverPlayer);
            return ActionResult.SUCCESS;
        });
    }

    private void registerTickEvent() {
        ServerTickEvents.END_SERVER_TICK.register(srv -> {
            if (dataStore == null) return;
            for (ServerPlayerEntity player : srv.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                if (dataStore.hasAbility(uuid) && dataStore.isOnCooldown(uuid)) {
                    long remaining = dataStore.remainingCooldownMs(uuid);
                    String timeStr = HeavenlyLogic.formatCooldown(remaining);
                    player.sendMessage(
                            Text.literal("§6[Heavenly] §7Cooldown: §e" + timeStr), true);
                }
            }
        });
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        literal("give")
                                .then(literal("customitem")
                                        .then(literal("heavenly")
                                                .requires(src -> src.hasPermissionLevel(2))
                                                .executes(ctx -> {
                                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                                    player.getInventory().offerOrDrop(createHeavenlyBook());
                                                    ctx.getSource().sendFeedback(
                                                            () -> Text.literal("Gave Heavenly book to " + player.getName().getString()), false);
                                                    return 1;
                                                })
                                                .then(literal("remove")
                                                        .then(argument("target", EntityArgumentType.player())
                                                                .requires(src -> src.hasPermissionLevel(2))
                                                                .executes(ctx -> {
                                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                                                    if (dataStore == null || !dataStore.hasAbility(target.getUuid())) {
                                                                        ctx.getSource().sendError(Text.literal(
                                                                                "§6[Heavenly] §e" + target.getName().getString() + " §7har inte Heavenly-förmågan."));
                                                                        return 0;
                                                                    }
                                                                    revokeAbility(target);
                                                                    ctx.getSource().sendFeedback(
                                                                            () -> Text.literal("§6[Heavenly] §7Heavenly togs bort från §e" + target.getName().getString() + "§7."), true);
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                ));
    }

    public static void grantAbility(ServerPlayerEntity player) {
        if (dataStore == null) return;
        dataStore.grantAbility(player.getUuid());
        saveQuiet();
        if (server != null) {
            server.getPlayerManager().broadcast(
                    Text.literal("§6[Heavenly] §e" + player.getName().getString() + " §7har fått Heavenly-förmågan!"), false);
        }
        refreshTabEntry(player);
    }

    public static void revokeAbility(ServerPlayerEntity player) {
        if (dataStore == null) return;
        dataStore.revokeAbility(player.getUuid());
        saveQuiet();
        if (server != null) {
            server.getPlayerManager().broadcast(
                    Text.literal("§6[Heavenly] §e" + player.getName().getString() + " §7har förlorat Heavenly-förmågan."), false);
        }
        refreshTabEntry(player);
    }

    public static void transferAbility(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        if (dataStore == null) return;
        dataStore.revokeAbility(victim.getUuid());
        dataStore.grantAbility(killer.getUuid());
        saveQuiet();
        if (server != null) {
            server.getPlayerManager().broadcast(
                    Text.literal("§6[Heavenly] §e" + killer.getName().getString()
                            + " §7tog Heavenly-förmågan från §e" + victim.getName().getString() + "§7!"), false);
        }
        refreshTabEntry(victim);
        refreshTabEntry(killer);
    }

    private static void refreshTabEntry(ServerPlayerEntity player) {
        MinecraftServer srv = player.getServer();
        if (srv == null) return;
        srv.getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(player)));
    }

    private static void saveQuiet() {
        if (dataStore == null) return;
        try {
            dataStore.save();
        } catch (Exception e) {
            LOGGER.error("Failed to save Heavenly data", e);
        }
    }

    public static ItemStack createHeavenlyBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(HeavenlyLogic.BOOK_DISPLAY_NAME)
                        .styled(s -> s.withColor(Formatting.GOLD).withBold(true).withItalic(false)));
        NbtCompound nbt = new NbtCompound();
        nbt.putString(HeavenlyLogic.ABILITY_NBT_KEY, HeavenlyLogic.ABILITY_BOOK_VALUE);
        book.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return book;
    }

    public static boolean isHeavenlyBook(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;
        NbtCompound nbt = component.copyNbt();
        return HeavenlyLogic.ABILITY_BOOK_VALUE.equals(nbt.getString(HeavenlyLogic.ABILITY_NBT_KEY));
    }
}
