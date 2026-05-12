package com.worldtweakancient;

import com.worldtweakancient.logic.AncientDebrisRarity;
import com.worldtweakancient.logic.EnchantmentBanList;
import com.worldtweakancient.logic.GlobalLootBanList;
import com.worldtweakancient.logic.DrownedEquipmentRules;
import com.worldtweakancient.logic.EntitySpawnBlockList;
import com.worldtweakancient.logic.NetherWorldGenBlockBanList;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WorldTweakAncientMod implements ModInitializer {

    public static final String MOD_ID = "world_tweak_ancient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final GlobalLootBanList GLOBAL_LOOT_BAN_LIST = GlobalLootBanList.defaults();
    private static final EntitySpawnBlockList SPAWN_BLOCK_LIST = EntitySpawnBlockList.defaults();
    private static final NetherWorldGenBlockBanList BLOCK_BAN_LIST = NetherWorldGenBlockBanList.defaults();
    private static final DrownedEquipmentRules DROWNED_RULES = DrownedEquipmentRules.defaults();
    private static final AncientDebrisRarity DEBRIS_RARITY = AncientDebrisRarity.defaults();
    private static final EnchantmentBanList ENCHANTMENT_BAN_LIST = EnchantmentBanList.defaults();

    public static GlobalLootBanList globalLootBanList() {
        return GLOBAL_LOOT_BAN_LIST;
    }

    public static EntitySpawnBlockList spawnBlockList() {
        return SPAWN_BLOCK_LIST;
    }

    public static NetherWorldGenBlockBanList blockBanList() {
        return BLOCK_BAN_LIST;
    }

    public static DrownedEquipmentRules drownedRules() {
        return DROWNED_RULES;
    }

    public static AncientDebrisRarity debrisRarity() {
        return DEBRIS_RARITY;
    }

    public static EnchantmentBanList enchantmentBanList() {
        return ENCHANTMENT_BAN_LIST;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("WorldTweakAncient initierad:");
        LOGGER.info("  Ancient Debris rarity multiplier: {}x (placed_feature override)", DEBRIS_RARITY.rarityMultiplier());
        LOGGER.info("  Global loot ban: {} (mixin)", GLOBAL_LOOT_BAN_LIST.banned());
        LOGGER.info("  Nether worldgen block ban: {} (mixin)", BLOCK_BAN_LIST.banned());
        LOGGER.info("  Spawn block list: {} (mixin)", SPAWN_BLOCK_LIST.blocked());
        LOGGER.info("  Drowned trident allowed: {} (mixin)", DROWNED_RULES.isTridentAllowed());
        LOGGER.info("  Enchantment ban list: {} (mixin)", ENCHANTMENT_BAN_LIST.banned());
    }
}
