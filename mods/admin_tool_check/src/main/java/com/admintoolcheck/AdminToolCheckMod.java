package com.admintoolcheck;

import com.admintoolcheck.command.CheckCommand;
import com.admintoolcheck.command.RankCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdminToolCheckMod implements ModInitializer {

    public static final String MOD_ID = "admin_tool_check";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CheckCommand.register(dispatcher);
            RankCommand.register(dispatcher);
        });
        LOGGER.info("AdminToolCheck initialized.");
    }
}
