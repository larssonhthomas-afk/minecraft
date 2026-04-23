package com.lifesteal;

import com.lifesteal.logic.HeartManager;
import com.lifesteal.logic.HeartManager.TransferResult;
import com.lifesteal.persistence.HeartDataStore;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

/**
 * Sidoeffekter på Minecraft-världen vid en kill. Anropas av PlayerDeathMixin
 * (riktiga PvP-kills) och av /lifesteal simulate-kill (solo-test).
 */
public final class LifeStealActions {

    private LifeStealActions() {}

    public static float currentMaxHealth(ServerPlayerEntity p) {
        EntityAttributeInstance attr = p.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        return attr != null ? (float) attr.getBaseValue() : 20.0f;
    }

    public static void applyMax(ServerPlayerEntity p, float newMax) {
        EntityAttributeInstance attr = p.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr == null) return;
        attr.setBaseValue(newMax);
        if (p.getHealth() > newMax) {
            p.setHealth(newMax);
        }
    }

    /**
     * Vanlig PvP-kill där båda sidor är riktiga spelare.
     * Returnerar {@code null} om modden inte är initierad eller killer == victim.
     */
    public static TransferResult performKill(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        HeartManager hm = LifeStealMod.heartManager();
        HeartDataStore store = LifeStealMod.dataStore();
        if (hm == null || store == null) return null;
        if (killer.getUuid().equals(victim.getUuid())) return null;

        float killerMax = currentMaxHealth(killer);
        float victimMax = currentMaxHealth(victim);
        TransferResult r = hm.transferHearts(killerMax, victimMax);

        if (r.transferOccurred()) {
            applyMax(killer, r.newKillerMax());
            applyMax(victim, r.newVictimMax());
            store.setMaxHealth(killer.getUuid(), r.newKillerMax());
            store.setMaxHealth(victim.getUuid(), r.newVictimMax());
            saveQuiet(store);
            broadcast(victim, Text.literal("§c" + killer.getName().getString()
                    + " §7stal ett hjärta från §c" + victim.getName().getString()));
        }

        maybeEliminate(victim, r);
        return r;
    }

    /**
     * Simulerad kill där "angriparen" är en virtuell entitet (konsol/admin-test).
     * Offrets max-HP minskas som vanligt; det avstulna hjärtat försvinner snarare
     * än att hamna hos en annan spelare — vilket är exakt vad solo-testning behöver.
     */
    public static TransferResult performSimulatedKill(ServerPlayerEntity victim, String attackerLabel) {
        HeartManager hm = LifeStealMod.heartManager();
        HeartDataStore store = LifeStealMod.dataStore();
        if (hm == null || store == null) return null;

        float victimMax = currentMaxHealth(victim);
        // Låtsas att "angriparen" har 0 max-HP så killer-sidan aldrig tappar
        // kapacitet — offrets sida utvärderas identiskt som i riktig kill.
        TransferResult r = hm.transferHearts(0f, victimMax);

        if (r.transferOccurred()) {
            applyMax(victim, r.newVictimMax());
            store.setMaxHealth(victim.getUuid(), r.newVictimMax());
            saveQuiet(store);
            broadcast(victim, Text.literal("§8[TEST]§r §c" + attackerLabel
                    + " §7stal ett hjärta från §c" + victim.getName().getString()));
        }

        maybeEliminate(victim, r);
        return r;
    }

    /**
     * Återställer en spelare till startvärdet (vanilla 10 hjärtan).
     * Flyttar dem även ut ur spectator om de var eliminerade.
     */
    public static void resetPlayer(ServerPlayerEntity player) {
        HeartDataStore store = LifeStealMod.dataStore();
        float reset = LifeStealMod.VANILLA_MAX_HEALTH;
        applyMax(player, reset);
        if (player.getHealth() < reset) {
            player.setHealth(reset);
        }
        if (store != null) {
            store.setMaxHealth(player.getUuid(), reset);
            try {
                store.save();
            } catch (Exception ex) {
                LifeStealMod.LOGGER.error("Kunde inte spara LifeSteal-data vid reset", ex);
            }
        }
        if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
            player.changeGameMode(GameMode.SURVIVAL);
        }
        broadcast(player, Text.literal("§a" + player.getName().getString()
                + " §7har återställts till startvärdet."));
    }

    private static void maybeEliminate(ServerPlayerEntity victim, TransferResult r) {
        if (!r.victimEliminated()) return;
        if (victim.interactionManager.getGameMode() == GameMode.SPECTATOR) return;
        victim.changeGameMode(GameMode.SPECTATOR);
        broadcast(victim, Text.literal("§4§l" + victim.getName().getString()
                + " §char eliminerats och är nu åskådare."));
    }

    private static void broadcast(ServerPlayerEntity anyPlayer, Text msg) {
        MinecraftServer server = anyPlayer.getServer();
        if (server != null) {
            server.getPlayerManager().broadcast(msg, false);
        }
    }

    private static void saveQuiet(HeartDataStore store) {
        try {
            store.save();
        } catch (Exception ex) {
            LifeStealMod.LOGGER.error("Kunde inte spara LifeSteal-data", ex);
        }
    }
}
