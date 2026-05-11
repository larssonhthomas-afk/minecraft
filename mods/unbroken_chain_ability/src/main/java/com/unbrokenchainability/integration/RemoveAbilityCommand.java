package com.unbrokenchainability.integration;

import com.unbrokenchainability.UnbrokenChainAbilityMod;
import com.unbrokenchainability.logic.AbilityDataStore;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class RemoveAbilityCommand {

    private RemoveAbilityCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("remove")
                                .requires(src -> src.hasPermissionLevel(2))
                                .then(CommandManager.literal("U_chain")
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .executes(ctx -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                    AbilityDataStore store = UnbrokenChainAbilityMod.dataStore();
                                                    if (store == null || !store.hasAbility(target.getUuid())) {
                                                        ctx.getSource().sendError(Text.literal(
                                                                "\u00a76[UChain] \u00a7e" + target.getName().getString()
                                                                        + " \u00a77har inte Unbroken Chain-abilityn."));
                                                        return 0;
                                                    }
                                                    UnbrokenChainAbilityActions.revokeAbility(target);
                                                    return 1;
                                                })))
                ));
    }
}
