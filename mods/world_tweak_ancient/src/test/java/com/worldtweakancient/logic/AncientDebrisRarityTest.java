package com.worldtweakancient.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AncientDebrisRarityTest {

    @Test
    void defaultsHaveTenTimesMultiplier() {
        AncientDebrisRarity rarity = AncientDebrisRarity.defaults();
        assertEquals(10, rarity.rarityMultiplier());
    }

    @Test
    void defaultGenerationProbabilityIsOneTenth() {
        AncientDebrisRarity rarity = AncientDebrisRarity.defaults();
        assertEquals(0.1, rarity.generationProbability(), 1e-9);
    }

    @Test
    void customMultiplierIsRespected() {
        AncientDebrisRarity rarity = new AncientDebrisRarity(5);
        assertEquals(5, rarity.rarityMultiplier());
        assertEquals(0.2, rarity.generationProbability(), 1e-9);
    }

    @Test
    void multiplierOneMeansVanillaRate() {
        AncientDebrisRarity rarity = new AncientDebrisRarity(1);
        assertEquals(1.0, rarity.generationProbability(), 1e-9);
    }

    @Test
    void zeroMultiplierThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AncientDebrisRarity(0));
    }

    @Test
    void negativeMultiplierThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AncientDebrisRarity(-3));
    }

    @Test
    void defaultMultiplierConstant() {
        assertEquals(10, AncientDebrisRarity.DEFAULT_MULTIPLIER);
    }
}
