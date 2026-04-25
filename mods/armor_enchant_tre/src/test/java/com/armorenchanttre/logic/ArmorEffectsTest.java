package com.armorenchanttre.logic;

import com.armorenchanttre.logic.ArmorEffects.Effect;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArmorEffectsTest {

    @Test
    void noEnchants_givesNoEffects() {
        assertTrue(ArmorEffects.effectsFor(null, null).isEmpty());
    }

    @Test
    void immunityBoots_givesAllFourImmunityFlags() {
        Set<Effect> effects = ArmorEffects.effectsFor(EnchantmentType.IMMUNITY, null);
        assertEquals(Set.of(
                Effect.POISON_IMMUNE,
                Effect.WITHER_IMMUNE,
                Effect.WEAKNESS_IMMUNE,
                Effect.SLOW_FALLING_IMMUNE), effects);
    }

    @Test
    void enduranceLeggings_givesSpeedAndSprintFlag() {
        Set<Effect> effects = ArmorEffects.effectsFor(null, EnchantmentType.ENDURANCE);
        assertEquals(Set.of(Effect.SPEED_1, Effect.SPRINT_PAST_HUNGER), effects);
    }

    @Test
    void extinguishLeggings_givesFireResistance() {
        Set<Effect> effects = ArmorEffects.effectsFor(null, EnchantmentType.EXTINGUISH);
        assertEquals(Set.of(Effect.FIRE_RESISTANCE), effects);
    }

    @Test
    void immunityBootsWithEnduranceLeggings_combinesEffects() {
        Set<Effect> effects = ArmorEffects.effectsFor(EnchantmentType.IMMUNITY, EnchantmentType.ENDURANCE);
        assertTrue(effects.contains(Effect.POISON_IMMUNE));
        assertTrue(effects.contains(Effect.WITHER_IMMUNE));
        assertTrue(effects.contains(Effect.WEAKNESS_IMMUNE));
        assertTrue(effects.contains(Effect.SLOW_FALLING_IMMUNE));
        assertTrue(effects.contains(Effect.SPEED_1));
        assertTrue(effects.contains(Effect.SPRINT_PAST_HUNGER));
        assertEquals(6, effects.size());
    }

    @Test
    void immunityBootsWithExtinguishLeggings_combinesEffects() {
        Set<Effect> effects = ArmorEffects.effectsFor(EnchantmentType.IMMUNITY, EnchantmentType.EXTINGUISH);
        assertTrue(effects.contains(Effect.FIRE_RESISTANCE));
        assertTrue(effects.contains(Effect.POISON_IMMUNE));
    }

    @Test
    void enduranceOnFeet_isIgnored() {
        Set<Effect> effects = ArmorEffects.effectsFor(EnchantmentType.ENDURANCE, null);
        assertTrue(effects.isEmpty(),
                "Endurance ska bara appliceras när den är på leggings, inte på boots.");
    }

    @Test
    void extinguishOnFeet_isIgnored() {
        Set<Effect> effects = ArmorEffects.effectsFor(EnchantmentType.EXTINGUISH, null);
        assertTrue(effects.isEmpty(),
                "Extinguish ska bara appliceras när den är på leggings, inte på boots.");
    }

    @Test
    void immunityOnLegs_isIgnored() {
        Set<Effect> effects = ArmorEffects.effectsFor(null, EnchantmentType.IMMUNITY);
        assertTrue(effects.isEmpty(),
                "Immunity ska bara appliceras när den är på boots, inte på leggings.");
    }

    @Test
    void shouldBlockEffect_immunityBoots_blocksAllFour() {
        assertTrue(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:poison"));
        assertTrue(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:wither"));
        assertTrue(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:weakness"));
        assertTrue(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:slow_falling"));
    }

    @Test
    void shouldBlockEffect_immunityBoots_doesNotBlockOtherEffects() {
        assertFalse(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:speed"));
        assertFalse(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:regeneration"));
        assertFalse(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:strength"));
        assertFalse(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, "minecraft:hunger"));
    }

    @Test
    void shouldBlockEffect_noImmunityBoots_blocksNothing() {
        assertFalse(ArmorEffects.shouldBlockEffect(null, "minecraft:poison"));
        assertFalse(ArmorEffects.shouldBlockEffect(EnchantmentType.ENDURANCE, "minecraft:poison"));
        assertFalse(ArmorEffects.shouldBlockEffect(EnchantmentType.EXTINGUISH, "minecraft:poison"));
    }

    @Test
    void shouldBlockEffect_nullEffectId_returnsFalse() {
        assertFalse(ArmorEffects.shouldBlockEffect(EnchantmentType.IMMUNITY, null));
    }
}
