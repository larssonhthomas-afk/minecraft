package com.specialdeathmessage;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpecialDeathMessageMod implements ModInitializer {

    public static final String MOD_ID = "special_death_message";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("SpecialDeathMessage initierad");
    }
}
