package com.goldguldspawn;

import com.goldguldspawn.logic.GoldSpawnCalculator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GoldGuldSpawnMod implements ModInitializer {

    public static final String MOD_ID = "gold_guld_spawn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final RegistryKey<PlacedFeature> EXTRA_GOLD_ORE =
        RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "extra_gold_ore"));

    private static final RegistryKey<PlacedFeature> EXTRA_NETHER_GOLD_ORE =
        RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "extra_nether_gold_ore"));

    @Override
    public void onInitialize() {
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            EXTRA_GOLD_ORE
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInTheNether(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            EXTRA_NETHER_GOLD_ORE
        );
        LOGGER.info("[GoldGuldSpawn] +{}% guld aktiverat — overworld +{}/chunk, nether +{}/chunk",
            (int) ((GoldSpawnCalculator.SPAWN_MULTIPLIER - 1.0) * 100),
            GoldSpawnCalculator.extraOverworldCount(),
            GoldSpawnCalculator.extraNetherCount());
    }
}
