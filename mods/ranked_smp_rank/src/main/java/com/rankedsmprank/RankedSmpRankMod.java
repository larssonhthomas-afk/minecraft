package com.rankedsmprank;

import com.rankedsmprank.command.BagCommand;
import com.rankedsmprank.command.RandomRankCommand;
import com.rankedsmprank.command.RankCommand;
import com.rankedsmprank.inventory.ExtraInventoryManager;
import com.rankedsmprank.logic.RankManager;
import com.rankedsmprank.persistence.BagDataStore;
import com.rankedsmprank.persistence.RankDataStore;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class RankedSmpRankMod implements ModInitializer {

    public static final String MOD_ID = "ranked_smp_rank";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static RankDataStore dataStore;
    private static RankManager rankManager;
    private static ExtraInventoryManager extraInventoryManager;
    private static BagDataStore bagDataStore;

    public static RankDataStore dataStore() {
        return dataStore;
    }

    public static RankManager rankManager() {
        return rankManager;
    }

    public static ExtraInventoryManager extraInventoryManager() {
        return extraInventoryManager;
    }

    public static BagDataStore bagDataStore() {
        return bagDataStore;
    }

    @Override
    public void onInitialize() {
        rankManager = new RankManager();
        extraInventoryManager = new ExtraInventoryManager();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Path dataPath = server.getSavePath(WorldSavePath.ROOT).resolve("ranked_smp.json");
            try {
                dataStore = RankDataStore.loadOrCreate(dataPath);
                LOGGER.info("RankedSmpRank loaded from {}", dataPath);
            } catch (Exception e) {
                LOGGER.error("Failed to load rank data, starting fresh", e);
                try {
                    dataStore = RankDataStore.loadOrCreate(dataPath);
                } catch (Exception ex) {
                    LOGGER.error("Critical: could not initialise RankDataStore", ex);
                }
            }

            Path bagPath = server.getSavePath(WorldSavePath.ROOT).resolve("ranked_bag.dat");
            bagDataStore = new BagDataStore(bagPath);
            try {
                Map<UUID, BagDataStore.SavedBag> savedBags = bagDataStore.load(server.getRegistryManager());
                for (Map.Entry<UUID, BagDataStore.SavedBag> entry : savedBags.entrySet()) {
                    BagDataStore.SavedBag saved = entry.getValue();
                    extraInventoryManager.restore(entry.getKey(), saved.usableSlots(), saved.items());
                }
                LOGGER.info("RankedSmpRank bags loaded: {} players", savedBags.size());
            } catch (Exception e) {
                LOGGER.error("Failed to load bag data", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (dataStore != null) {
                try {
                    dataStore.save();
                } catch (Exception e) {
                    LOGGER.error("Failed to save rank data on shutdown", e);
                }
            }
            if (bagDataStore != null) {
                try {
                    bagDataStore.save(extraInventoryManager.getAllInventories(), server.getRegistryManager());
                } catch (Exception e) {
                    LOGGER.error("Failed to save bag data on shutdown", e);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                RankedSmpRankActions.applyRankToPlayer(handler.player, dataStore));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (bagDataStore != null) {
                try {
                    bagDataStore.save(extraInventoryManager.getAllInventories(), server.getRegistryManager());
                } catch (Exception e) {
                    LOGGER.error("Failed to save bag data on disconnect", e);
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            RankCommand.register(dispatcher);
            RandomRankCommand.register(dispatcher);
            BagCommand.register(dispatcher);
        });
    }
}
