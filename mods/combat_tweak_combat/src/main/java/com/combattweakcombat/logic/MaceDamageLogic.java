package com.combattweakcombat.logic;

public final class MaceDamageLogic {
    private static final float NERF_MULTIPLIER = 0.6f;
    public static final float MAX_DAMAGE = 14.0f;

    private MaceDamageLogic() {}

    public static float applyNerfedDamage(float rawDamage) {
        return Math.min(rawDamage * NERF_MULTIPLIER, MAX_DAMAGE);
    }
}
