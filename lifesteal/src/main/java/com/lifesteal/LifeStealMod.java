package com.lifesteal;

import com.lifesteal.command.LifeStealCommand;
import com.lifesteal.config.LifeStealConfig;
import com.lifesteal.logic.HeartManager;
import com.lifesteal.persistence.HeartDataStore;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class LifeStealMod implements ModInitializer {

    public static final String MOD_ID = "lifesteal";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Vanilla Minecraft startvärde: 10 hjärtan. */
    public static final float VANILLA_MAX_HEALTH = 20.0f;

    private static HeartManager heartManager;
    private static HeartDataStore dataStore;

    public static HeartManager heartManager() {
        return heartManager;
    }

    public static HeartDataStore dataStore() {
        return dataStore;
    }

    @Override
    public void onInitialize() {
        heartManager = buildHeartManagerFromConfig();
        LOGGER.info("LifeSteal initierad: minHealth={}, maxHealth={}, heartsToSteal={}",
                heartManager.getMinHealth(),
                heartManager.getMaxHealth(),
                heartManager.getHeartsToSteal());

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> applyStoredMaxHealth(handler.player));
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, env) -> LifeStealCommand.register(dispatcher));
    }

    private static HeartManager buildHeartManagerFromConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
        LifeStealConfig config;
        try {
            config = LifeStealConfig.loadOrCreate(configPath);
        } catch (Exception ex) {
            LOGGER.error("Kunde inte läsa {} — använder default-värden. Fel: {}",
                    configPath, ex.getMessage());
            config = LifeStealConfig.defaults();
        }
        try {
            return new HeartManager(config.minHealth(), config.maxHealth(), config.heartsToSteal());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Ogiltig config i {} ({}) — använder default-värden",
                    configPath, ex.getMessage());
            LifeStealConfig defaults = LifeStealConfig.defaults();
            return new HeartManager(defaults.minHealth(), defaults.maxHealth(), defaults.heartsToSteal());
        }
    }

    private void onServerStarted(MinecraftServer server) {
        Path file = server.getSavePath(WorldSavePath.ROOT).resolve("lifesteal.json");
        dataStore = HeartDataStore.loadOrCreate(file, VANILLA_MAX_HEALTH);
        LOGGER.info("LifeSteal persistens laddad från {} ({} spelare)", file, dataStore.size());
    }

    private void onServerStopping(MinecraftServer server) {
        if (dataStore != null) {
            try {
                dataStore.save();
            } catch (Exception ex) {
                LOGGER.error("Kunde inte spara LifeSteal-data vid avstängning", ex);
            }
        }
    }

    /**
     * Applicerar lagrad max-HP på spelaren. Kallas vid inloggning och efter varje transfer.
     */
    public static void applyStoredMaxHealth(ServerPlayerEntity player) {
        if (dataStore == null || heartManager == null) return;
        float stored = dataStore.getMaxHealth(player.getUuid());
        float clamped = heartManager.clampToRange(stored);
        if (clamped != stored) {
            dataStore.setMaxHealth(player.getUuid(), clamped);
        }
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(clamped);
            if (player.getHealth() > clamped) {
                player.setHealth(clamped);
            }
        }
        if (heartManager.isEliminated(clamped)) {
            player.changeGameMode(GameMode.SPECTATOR);
        }
    }
}
