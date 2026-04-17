package com.lifesteal.command;

import com.lifesteal.LifeStealActions;
import com.lifesteal.LifeStealMod;
import com.lifesteal.logic.HeartManager;
import com.lifesteal.logic.HeartManager.TransferResult;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class LifeStealCommand {

    private LifeStealCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("lifesteal")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("simulate-kill")
                    .then(CommandManager.argument("victim", EntityArgumentType.player())
                        .executes(ctx -> runSimulate(ctx.getSource(),
                                EntityArgumentType.getPlayer(ctx, "victim"),
                                "Virtuell testfiende"))))
                .then(CommandManager.literal("status")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> runStatus(ctx.getSource(),
                                EntityArgumentType.getPlayer(ctx, "player")))))
                .then(CommandManager.literal("reset")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> runReset(ctx.getSource(),
                                EntityArgumentType.getPlayer(ctx, "player")))))
        );
    }

    private static int runReset(ServerCommandSource source, ServerPlayerEntity player) {
        if (LifeStealMod.heartManager() == null || LifeStealMod.dataStore() == null) {
            source.sendError(Text.literal("LifeSteal ej initialiserad."));
            return 0;
        }
        LifeStealActions.resetPlayer(player);
        source.sendFeedback(() -> Text.literal(
                "Återställde " + player.getName().getString()
                        + " till startvärde (" + LifeStealMod.VANILLA_MAX_HEALTH + " HP)"
        ), true);
        return 1;
    }

    private static int runSimulate(ServerCommandSource source, ServerPlayerEntity victim, String attackerLabel) {
        TransferResult r = LifeStealActions.performSimulatedKill(victim, attackerLabel);
        if (r == null) {
            source.sendError(Text.literal("LifeSteal ej initialiserad."));
            return 0;
        }
        float heartsHp = r.heartsTransferredHp();
        source.sendFeedback(() -> Text.literal(
                "Simulerad kill: " + heartsHp + " HP tagen från "
                        + victim.getName().getString()
                        + (r.victimEliminated() ? " (eliminerad)" : "")
        ), true);
        return (int) Math.max(1, heartsHp);
    }

    private static int runStatus(ServerCommandSource source, ServerPlayerEntity player) {
        HeartManager hm = LifeStealMod.heartManager();
        if (hm == null) {
            source.sendError(Text.literal("LifeSteal ej initialiserad."));
            return 0;
        }
        float max = LifeStealActions.currentMaxHealth(player);
        float hearts = max / HeartManager.HEALTH_PER_HEART;
        source.sendFeedback(() -> Text.literal(
                player.getName().getString() + ": max-HP " + max + " (" + hearts + " hjärtan)"
        ), false);
        return (int) max;
    }
}
