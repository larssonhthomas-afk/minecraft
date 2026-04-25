package com.armorenchanttre.logic;

import java.util.EnumSet;
import java.util.Set;

/**
 * Härleder vilka effekter som ska vara aktiva på en spelare baserat på
 * vilka enchantments som finns på spelarens rustning.
 *
 * Effekterna mappas direkt mot de tre enchantments:
 *   - IMMUNITY (boots)    -> POISON_IMMUNE, WITHER_IMMUNE, WEAKNESS_IMMUNE, SLOW_FALLING_IMMUNE
 *   - ENDURANCE (leggings) -> SPEED_1, SPRINT_PAST_HUNGER
 *   - EXTINGUISH (leggings) -> FIRE_RESISTANCE
 *
 * Effekterna försvinner direkt när rustningen tas av (ingen avklingning).
 */
public final class ArmorEffects {

    public enum Effect {
        POISON_IMMUNE,
        WITHER_IMMUNE,
        WEAKNESS_IMMUNE,
        SLOW_FALLING_IMMUNE,
        SPEED_1,
        SPRINT_PAST_HUNGER,
        FIRE_RESISTANCE
    }

    private ArmorEffects() {}

    /**
     * @param feetEnchant enchantment på boots, eller null
     * @param legsEnchant enchantment på leggings, eller null
     * @return ovärderad uppsättning av effekter som ska vara aktiva
     */
    public static Set<Effect> effectsFor(EnchantmentType feetEnchant, EnchantmentType legsEnchant) {
        Set<Effect> out = EnumSet.noneOf(Effect.class);
        if (feetEnchant == EnchantmentType.IMMUNITY) {
            out.add(Effect.POISON_IMMUNE);
            out.add(Effect.WITHER_IMMUNE);
            out.add(Effect.WEAKNESS_IMMUNE);
            out.add(Effect.SLOW_FALLING_IMMUNE);
        }
        if (legsEnchant == EnchantmentType.ENDURANCE) {
            out.add(Effect.SPEED_1);
            out.add(Effect.SPRINT_PAST_HUNGER);
        }
        if (legsEnchant == EnchantmentType.EXTINGUISH) {
            out.add(Effect.FIRE_RESISTANCE);
        }
        return out;
    }

    /**
     * @return true om enchantet på boots blockerar effekten med givet vanilla-id
     *         (t.ex. "minecraft:poison").
     */
    public static boolean shouldBlockEffect(EnchantmentType feetEnchant, String vanillaEffectId) {
        if (feetEnchant != EnchantmentType.IMMUNITY) return false;
        if (vanillaEffectId == null) return false;
        return switch (vanillaEffectId) {
            case "minecraft:poison",
                 "minecraft:wither",
                 "minecraft:weakness",
                 "minecraft:slow_falling" -> true;
            default -> false;
        };
    }
}
