package com.templatemod;

import com.templatemod.logic.TemplateLogic;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point — rename class and package to match your mod.
 * Keep Minecraft API calls here; pure logic goes in logic/*.
 */
public final class TemplateModMod implements ModInitializer {

    public static final String MOD_ID = "templatemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("{} initialized", MOD_ID);
        // Register events, commands, lifecycle hooks here.
    }
}
