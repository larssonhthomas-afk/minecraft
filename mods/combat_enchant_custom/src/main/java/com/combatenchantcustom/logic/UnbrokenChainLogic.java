package com.combatenchantcustom.logic;

public final class UnbrokenChainLogic {

    public static final String BOOK_NAME = "Unbroken_chain";
    public static final float BONUS_PER_LEVEL = 0.03f;
    public static final float MAX_BONUS = 0.30f;

    private UnbrokenChainLogic() {}

    public static boolean isSwordItemId(String itemId) {
        if (itemId == null) return false;
        return itemId.endsWith("_sword");
    }

    public static float calculateDamageMultiplier(int bonusLevel) {
        if (bonusLevel <= 0) return 1.0f;
        float bonus = Math.min(bonusLevel * BONUS_PER_LEVEL, MAX_BONUS);
        return 1.0f + bonus;
    }
}
