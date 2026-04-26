package com.combatenchantcustom.integration;

import com.combatenchantcustom.logic.HitTracker;
import net.minecraft.entity.player.PlayerEntity;

public final class ChainStateManager {

    private static final HitTracker TRACKER = new HitTracker();

    private ChainStateManager() {}

    public static HitTracker.HitResult recordHit(PlayerEntity attacker) {
        return TRACKER.recordHit(attacker.getUuidAsString());
    }

    public static void resetChain(PlayerEntity player) {
        TRACKER.resetChain(player.getUuidAsString());
    }

    public static int getBonusLevel(PlayerEntity player) {
        return TRACKER.getBonusLevel(player.getUuidAsString());
    }
}
