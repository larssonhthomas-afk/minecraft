package com.unbrokenchainability;

import com.unbrokenchainability.integration.BookCreator;
import com.unbrokenchainability.integration.RemoveAbilityCommand;
import com.unbrokenchainability.integration.UnbrokenChainAbilityActions;
import com.unbrokenchainability.logic.AbilityDataStore;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class UnbrokenChainAbilityMod implements ModInitializer {

    public static final String MOD_ID = "unbroken_chain_ability";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static AbilityDataStore dataStore;

    public static AbilityDataStore dataStore() {
        return dataStore;
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Path dataPath = server.getSavePath(WorldSavePath.ROOT).resolve("unbroken_chain_ability.json");
            try {
                dataStore = AbilityDataStore.loadOrCreate(dataPath);
                LOGGER.info("UnbrokenChainAbility loaded from {}", dataPath);
            } catch (Exception e) {
                LOGGER.error("Failed to load ability data", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (dataStore != null) {
                try {
                    dataStore.save();
                } catch (Exception e) {
                    LOGGER.error("Failed to save ability data on shutdown", e);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (dataStore != null) {
                dataStore.setPlayerName(handler.player.getUuid(), handler.player.getName().getString());
            }
        });

        // Right-click the Unbroken Chain book to apply the ability permanently
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!BookCreator.isAbilityBook(stack)) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            if (dataStore != null && dataStore.hasAbility(serverPlayer.getUuid())) {
                serverPlayer.sendMessage(
                        Text.literal("§6[UChain] §7Du har redan Unbroken Chain-abilityn."), false);
                return ActionResult.FAIL;
            }

            stack.decrement(1);
            UnbrokenChainAbilityActions.grantAbility(serverPlayer);
            return ActionResult.SUCCESS;
        });

        RemoveAbilityCommand.register();
        LOGGER.info("UnbrokenChainAbility initialized");
    }
}
