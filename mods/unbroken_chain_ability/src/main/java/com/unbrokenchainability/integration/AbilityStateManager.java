package com.unbrokenchainability.integration;

import com.unbrokenchainability.logic.HitTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class AbilityStateManager {

    private static final HitTracker TRACKER = new HitTracker();

    private AbilityStateManager() {}

    public static HitTracker.HitResult recordHit(PlayerEntity attacker, Entity target) {
        long nowMs = System.currentTimeMillis();
        return TRACKER.recordHit(attacker.getUuidAsString(), target.getUuidAsString(), nowMs);
    }

    public static void resetChain(PlayerEntity player) {
        TRACKER.resetChain(player.getUuidAsString());
    }

    public static int getBonusLevel(PlayerEntity player) {
        return TRACKER.getBonusLevel(player.getUuidAsString());
    }
}
