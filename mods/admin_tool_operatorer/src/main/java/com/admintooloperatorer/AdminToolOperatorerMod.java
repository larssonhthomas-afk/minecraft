package com.admintooloperatorer;

import com.admintooloperatorer.command.AdminRankCommand;
import com.admintooloperatorer.command.CheckCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdminToolOperatorerMod implements ModInitializer {

    public static final String MOD_ID = "admin_tool_operatorer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            CheckCommand.register(dispatcher);
            AdminRankCommand.register(dispatcher);
        });
        LOGGER.info("AdminToolOperatorer initialized");
    }
}
