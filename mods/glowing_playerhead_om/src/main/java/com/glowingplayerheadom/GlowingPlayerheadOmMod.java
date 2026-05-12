package com.glowingplayerheadom;

import com.glowingplayerheadom.logic.GlowingLogic;
import com.glowingplayerheadom.logic.GlowingSession;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class GlowingPlayerheadOmMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("glowing_playerhead_om");

    private static final GlowingLogic logic = new GlowingLogic();
    private static final Map<UUID, Long> pendingConfirmations = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT_MS = 60_000L;
    private static int tickCounter = 0;

    @Override
    public void onInitialize() {
        registerItemUseEvents();
        registerCommands();
        registerTickEvent();
        registerDisconnectEvent();
        LOGGER.info("GlowingPlayerheadOm loaded");
    }

    private void registerItemUseEvents() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient() && isPlayerHead(stack) && player instanceof ServerPlayerEntity serverPlayer) {
                UUID pid = player.getUuid();
                if (!pendingConfirmations.containsKey(pid)) {
                    pendingConfirmations.put(pid, System.currentTimeMillis());
                    sendConfirmationMessage(serverPlayer);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() && isPlayerHead(player.getStackInHand(hand))
                    && player instanceof ServerPlayerEntity serverPlayer) {
                UUID pid = player.getUuid();
                if (!pendingConfirmations.containsKey(pid)) {
                    pendingConfirmations.put(pid, System.currentTimeMillis());
                    sendConfirmationMessage(serverPlayer);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("glowing_phead_confirm")
                .then(literal("yes").executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayerEntity player) {
                        handleConfirmYes(player, ctx.getSource().getServer());
                    }
                    return 1;
                }))
                .then(literal("no").executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayerEntity player) {
                        handleConfirmNo(player);
                    }
                    return 1;
                })))
        );
    }

    private void registerTickEvent() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tickCounter % 20 != 0) return;
            tickCounter = 0;

            long now = System.currentTimeMillis();
            pendingConfirmations.entrySet().removeIf(e -> now - e.getValue() > CONFIRMATION_TIMEOUT_MS);

            Map<UUID, GlowingSession> snapshot = new HashMap<>(logic.getActiveSessions());
            for (Map.Entry<UUID, GlowingSession> entry : snapshot.entrySet()) {
                UUID activatorId = entry.getKey();
                GlowingSession session = entry.getValue();

                if (session.isExpired(now)) {
                    cleanupSession(activatorId, server);
                    continue;
                }

                ServerPlayerEntity activator = server.getPlayerManager().getPlayer(activatorId);
                if (activator == null) {
                    cleanupSession(activatorId, server);
                    continue;
                }

                int remainingTicks = (int) (session.remainingMs(now) / 50);
                Set<UUID> nowInRange = new HashSet<>();

                for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
                    if (!target.getWorld().getRegistryKey().equals(activator.getWorld().getRegistryKey())) continue;
                    if (session.isWithinRadius(
                            activator.getX(), activator.getY(), activator.getZ(),
                            target.getX(), target.getY(), target.getZ())) {
                        nowInRange.add(target.getUuid());
                        target.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.GLOWING, remainingTicks, 0, false, false, true));
                    }
                }

                Set<UUID> leftRange = new HashSet<>(session.affectedPlayerIds);
                leftRange.removeAll(nowInRange);
                for (UUID pid : leftRange) {
                    ServerPlayerEntity target = server.getPlayerManager().getPlayer(pid);
                    if (target != null) {
                        target.removeStatusEffect(StatusEffects.GLOWING);
                    }
                }

                session.affectedPlayerIds.clear();
                session.affectedPlayerIds.addAll(nowInRange);
            }
        });
    }

    private void registerDisconnectEvent() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID playerId = handler.player.getUuid();
            pendingConfirmations.remove(playerId);
            cleanupSession(playerId, server);
        });
    }

    private void handleConfirmYes(ServerPlayerEntity player, MinecraftServer server) {
        UUID playerId = player.getUuid();
        Long pendingTime = pendingConfirmations.remove(playerId);

        if (pendingTime == null || System.currentTimeMillis() - pendingTime > CONFIRMATION_TIMEOUT_MS) {
            player.sendMessage(Text.literal("Din bekräftelse har gått ut.").formatted(Formatting.RED), false);
            return;
        }

        ItemStack head = findPlayerHead(player);
        if (head == null) {
            player.sendMessage(Text.literal("Du håller inte längre i ett spelarhuvud.").formatted(Formatting.RED), false);
            return;
        }

        head.decrement(1);

        long now = System.currentTimeMillis();
        cleanupSession(playerId, server);
        GlowingSession session = logic.createSession(playerId, now);
        int remainingTicks = (int) (GlowingSession.DURATION_MS / 50);

        for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
            if (!target.getWorld().getRegistryKey().equals(player.getWorld().getRegistryKey())) continue;
            if (session.isWithinRadius(
                    player.getX(), player.getY(), player.getZ(),
                    target.getX(), target.getY(), target.getZ())) {
                target.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.GLOWING, remainingTicks, 0, false, false, true));
                session.affectedPlayerIds.add(target.getUuid());
            }
        }

        player.sendMessage(
                Text.literal("Spelarhuvudet användes! Alla spelare inom 50 block glöder i 10 minuter.")
                        .formatted(Formatting.GOLD), false);
    }

    private void handleConfirmNo(ServerPlayerEntity player) {
        pendingConfirmations.remove(player.getUuid());
        player.sendMessage(Text.literal("Avbröt.").formatted(Formatting.GRAY), false);
    }

    private void cleanupSession(UUID activatorId, MinecraftServer server) {
        GlowingSession session = logic.getSession(activatorId);
        if (session == null) return;
        for (UUID pid : session.affectedPlayerIds) {
            ServerPlayerEntity target = server.getPlayerManager().getPlayer(pid);
            if (target != null) {
                target.removeStatusEffect(StatusEffects.GLOWING);
            }
        }
        logic.removeSession(activatorId);
    }

    private void sendConfirmationMessage(ServerPlayerEntity player) {
        Text yesButton = Text.literal("[Ja]").styled(s -> s
                .withColor(Formatting.GREEN)
                .withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/glowing_phead_confirm yes"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("Klicka för att använda spelarhuvudet"))));
        Text noButton = Text.literal("[Nej]").styled(s -> s
                .withColor(Formatting.RED)
                .withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/glowing_phead_confirm no"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("Klicka för att avbryta"))));
        Text message = Text.literal("Vill du använda spelarhuvudet? Alla spelare inom 50 block får ")
                .append(Text.literal("Glowing").formatted(Formatting.WHITE, Formatting.BOLD))
                .append(Text.literal(" i 10 minuter. "))
                .append(yesButton)
                .append(Text.literal("  "))
                .append(noButton);
        player.sendMessage(message, false);
    }

    private static boolean isPlayerHead(ItemStack stack) {
        return stack.getItem() == Items.PLAYER_HEAD && stack.contains(DataComponentTypes.PROFILE);
    }

    private static ItemStack findPlayerHead(ServerPlayerEntity player) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (isPlayerHead(stack)) return stack;
        }
        return null;
    }
}
