package com.dropnr.logic;

/**
 * Beskriver en bossnyckel som släpps vid bossdöd. Ren data; ingen Minecraft-koppling.
 * colorRgb tolkas som 24-bitars 0xRRGGBB.
 */
public record BossKeyType(String displayName, int colorRgb) {

    public static final BossKeyType DRAGON = new BossKeyType("Dragon Key", 0x800080);
    public static final BossKeyType WARDEN = new BossKeyType("Warden Key", 0x00FF00);
    public static final BossKeyType WITHER = new BossKeyType("Wither Key", 0x000000);

    public BossKeyType {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        if (colorRgb < 0 || colorRgb > 0xFFFFFF) {
            throw new IllegalArgumentException(
                    "colorRgb must be a 24-bit value (0x000000..0xFFFFFF)");
        }
    }
}
