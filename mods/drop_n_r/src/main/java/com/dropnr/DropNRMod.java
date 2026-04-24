package com.dropnr;

import com.dropnr.event.DeathDropHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DropNRMod implements ModInitializer {

    public static final String MOD_ID = "drop_n_r";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register(DeathDropHandler::onDeath);
        LOGGER.info("drop_n_r initierad: PvP player head + Dragon/Warden/Wither keys");
    }
}
