package com.combattweakpearl;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CombatTweakPearlMod implements ModInitializer {

    public static final String MOD_ID = "combat_tweak_pearl";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("combat_tweak_pearl: 15s pearl/wind-charge cooldown + mace nerf (-40%, max 7 hearts) active");
    }
}
