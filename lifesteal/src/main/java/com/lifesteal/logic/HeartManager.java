package com.lifesteal.logic;

/**
 * Ren hjärt-matematik. Har medvetet inga Minecraft-importer så den kan
 * enhetstestas utan att starta en server eller ladda mappings.
 */
public final class HeartManager {

    public static final float HEALTH_PER_HEART = 2.0f;

    private final float minHealth;
    private final float maxHealth;
    private final float heartsToSteal;

    public HeartManager(float minHealth, float maxHealth, float heartsToSteal) {
        if (!Float.isFinite(minHealth) || !Float.isFinite(maxHealth) || !Float.isFinite(heartsToSteal)) {
            throw new IllegalArgumentException("Alla värden måste vara ändliga (inte NaN/Infinity)");
        }
        if (minHealth < 0f) {
            throw new IllegalArgumentException("minHealth får inte vara negativ: " + minHealth);
        }
        if (maxHealth <= minHealth) {
            throw new IllegalArgumentException("maxHealth måste vara större än minHealth (" + minHealth + " vs " + maxHealth + ")");
        }
        if (heartsToSteal <= 0f) {
            throw new IllegalArgumentException("heartsToSteal måste vara > 0: " + heartsToSteal);
        }
        this.minHealth = minHealth;
        this.maxHealth = maxHealth;
        this.heartsToSteal = heartsToSteal;
    }

    public static HeartManager createDefault() {
        return new HeartManager(4.0f, 40.0f, 1.0f);
    }

    public float getMinHealth() {
        return minHealth;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getHeartsToSteal() {
        return heartsToSteal;
    }

    public float getStealAmountHp() {
        return heartsToSteal * HEALTH_PER_HEART;
    }

    /**
     * Clamp-ar ett inkommande maxHP-värde till intervallet [0, maxHealth].
     * Används när vi laddar persistent data som kan vara äldre än nuvarande config.
     */
    public float clampToRange(float currentMax) {
        if (currentMax < 0f) return 0f;
        if (currentMax > maxHealth) return maxHealth;
        return currentMax;
    }

    public boolean isAtMaximumHealth(float currentMax) {
        return currentMax >= maxHealth;
    }

    public boolean isEliminated(float currentMax) {
        return currentMax < minHealth;
    }

    /**
     * Beräknar nytt maxHP för mördare och offer efter en PvP-kill.
     * <p>
     * Regler:
     * <ul>
     *   <li>Mördaren kan aldrig gå över {@link #getMaxHealth()}.</li>
     *   <li>Offret kan aldrig gå under 0.</li>
     *   <li>Verklig överföring = min(stealAmount, mördarens ledigheter, offrets resurser).</li>
     *   <li>Är mördaren redan på max sker ingen överföring alls – hjärtat "tappas på marken".</li>
     *   <li>Är offret redan på 0 sker ingen överföring.</li>
     * </ul>
     */
    public TransferResult transferHearts(float killerCurrentMax, float victimCurrentMax) {
        float stealAmount = getStealAmountHp();
        float killerCapacity = Math.max(0f, maxHealth - killerCurrentMax);
        float victimCapacity = Math.max(0f, victimCurrentMax);
        float actualTransfer = Math.min(stealAmount, Math.min(killerCapacity, victimCapacity));

        float newKillerMax = killerCurrentMax + actualTransfer;
        float newVictimMax = victimCurrentMax - actualTransfer;

        boolean transferOccurred = actualTransfer > 0f;
        boolean victimEliminated = newVictimMax < minHealth;

        return new TransferResult(newKillerMax, newVictimMax, actualTransfer, transferOccurred, victimEliminated);
    }

    public record TransferResult(
            float newKillerMax,
            float newVictimMax,
            float heartsTransferredHp,
            boolean transferOccurred,
            boolean victimEliminated
    ) {
        public float heartsTransferred() {
            return heartsTransferredHp / HEALTH_PER_HEART;
        }
    }
}
