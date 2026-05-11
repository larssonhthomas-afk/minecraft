package com.unbrokenchainability.logic;

public final class ChainLogic {

    public static final float BONUS_PER_LEVEL = 0.03f;
    public static final float MAX_BONUS = 0.30f;
    public static final String BOOK_DISPLAY_NAME = "Unbroken Chain";
    public static final String ABILITY_NBT_KEY = "unbroken_chain_ability";
    public static final String ABILITY_BOOK_VALUE = "ability_book";

    private ChainLogic() {}

    public static float calculateDamageMultiplier(int bonusLevel) {
        if (bonusLevel <= 0) return 1.0f;
        float bonus = Math.min(bonusLevel * BONUS_PER_LEVEL, MAX_BONUS);
        return 1.0f + bonus;
    }

    public static boolean isSwordItemId(String itemId) {
        if (itemId == null) return false;
        return itemId.endsWith("_sword");
    }
}
