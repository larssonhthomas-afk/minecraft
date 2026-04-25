package com.armorenchanttre;

import com.armorenchanttre.integration.ArmorEffectApplier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ArmorEnchantTreMod implements ModInitializer {

    public static final String MOD_ID = "armor_enchant_tre";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(ArmorEffectApplier::onServerTick);
        LOGGER.info("armor_enchant_tre initierad: Immunity (boots), Endurance (leggings), Extinguish (leggings)");
    }
}
