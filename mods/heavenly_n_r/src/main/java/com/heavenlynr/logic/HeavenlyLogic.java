package com.heavenlynr.logic;

public final class HeavenlyLogic {

    public static final long COOLDOWN_MS = 20L * 60 * 1000; // 20 minutes
    public static final String BOOK_DISPLAY_NAME = "Heavenly";
    public static final String ABILITY_NBT_KEY = "heavenly_ability";
    public static final String ABILITY_BOOK_VALUE = "ability_book";
    public static final String DISPLAY_SUFFIX = " Heavenly";
    public static final float HELMET_DAMAGE_FRACTION = 0.40f;

    private HeavenlyLogic() {}

    public static boolean wouldSave(boolean hasAbility, boolean onCooldown) {
        return hasAbility && !onCooldown;
    }

    public static String formatCooldown(long remainingMs) {
        if (remainingMs <= 0) return "0:00";
        long totalSeconds = (remainingMs + 999) / 1000; // round up
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
