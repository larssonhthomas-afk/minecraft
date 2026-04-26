package com.combatenchantcustom;

import com.combatenchantcustom.integration.CustomItemCommand;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CombatEnchantCustomMod implements ModInitializer {

    public static final String MOD_ID = "combat_enchant_custom";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CustomItemCommand.register();
        LOGGER.info("combat_enchant_custom initialized: Unbroken Chain sword enchantment");
    }
}
