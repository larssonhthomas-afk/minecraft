package com.dropnr.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossKeyTypeTest {

    @Test
    void dragonKey_hasCorrectNameAndPurpleColor() {
        assertEquals("Dragon Key", BossKeyType.DRAGON.displayName());
        assertEquals(0x800080, BossKeyType.DRAGON.colorRgb());
    }

    @Test
    void wardenKey_hasCorrectNameAndGreenColor() {
        assertEquals("Warden Key", BossKeyType.WARDEN.displayName());
        assertEquals(0x00FF00, BossKeyType.WARDEN.colorRgb());
    }

    @Test
    void witherKey_hasCorrectNameAndBlackColor() {
        assertEquals("Wither Key", BossKeyType.WITHER.displayName());
        assertEquals(0x000000, BossKeyType.WITHER.colorRgb());
    }

    @Test
    void constructor_rejectsNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new BossKeyType(null, 0x000000));
    }

    @Test
    void constructor_rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new BossKeyType("   ", 0x000000));
    }

    @Test
    void constructor_rejectsEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> new BossKeyType("", 0x000000));
    }

    @Test
    void constructor_rejectsNegativeColor() {
        assertThrows(IllegalArgumentException.class,
                () -> new BossKeyType("Test Key", -1));
    }

    @Test
    void constructor_rejectsColorAbove24Bit() {
        assertThrows(IllegalArgumentException.class,
                () -> new BossKeyType("Test Key", 0x1000000));
    }

    @Test
    void constructor_acceptsBoundaryColors() {
        assertDoesNotThrow(() -> new BossKeyType("Test", 0x000000));
        assertDoesNotThrow(() -> new BossKeyType("Test", 0xFFFFFF));
    }

    @Test
    void records_withSameValuesAreEqual() {
        BossKeyType a = new BossKeyType("Dragon Key", 0x800080);
        assertEquals(a, BossKeyType.DRAGON);
    }

    @Test
    void keys_withDifferentColorsAreDistinct() {
        assertNotEquals(BossKeyType.DRAGON, BossKeyType.WARDEN);
        assertNotEquals(BossKeyType.WARDEN, BossKeyType.WITHER);
        assertNotEquals(BossKeyType.DRAGON, BossKeyType.WITHER);
    }
}
