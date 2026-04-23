package com.specialdeathmessage.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeathMessageLogicTest {

    @Test
    void customDeathMessageIsCorrect() {
        assertEquals("du doooggg", DeathMessageLogic.getCustomDeathMessage());
    }

    @Test
    void customDeathMessageIsNotNull() {
        assertNotNull(DeathMessageLogic.getCustomDeathMessage());
    }

    @Test
    void customDeathMessageIsNotEmpty() {
        assertFalse(DeathMessageLogic.getCustomDeathMessage().isEmpty());
    }

    @Test
    void customDeathMessageConstantMatchesMethod() {
        assertEquals(DeathMessageLogic.CUSTOM_DEATH_MESSAGE, DeathMessageLogic.getCustomDeathMessage());
    }
}
