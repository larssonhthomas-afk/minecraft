package com.combattweakpearl.logic;

public final class MaceDamageLogic {
    private static final float NERF_MULTIPLIER = 0.6f; // 40% reduction
    public static final float MAX_DAMAGE = 14.0f;       // 7 hearts

    private MaceDamageLogic() {}

    public static float applyNerfedDamage(float rawDamage) {
        return Math.min(rawDamage * NERF_MULTIPLIER, MAX_DAMAGE);
    }
}
