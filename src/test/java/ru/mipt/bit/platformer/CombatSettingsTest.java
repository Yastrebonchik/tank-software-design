package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class CombatSettingsTest {
    @Test void defaultWeaponParameters() {
        assertEquals(100, CombatSettings.DEFAULT.getHealth());
        assertEquals(25, CombatSettings.DEFAULT.getDamage());
        assertEquals(0.5f, CombatSettings.DEFAULT.getReloadSeconds());
        assertEquals(5f, CombatSettings.DEFAULT.getBulletSpeed());
    }

    @ParameterizedTest @ValueSource(floats = {0f, -1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void rejectsNonPositiveOrNonFiniteTimesAndSpeeds(float value) {
        assertThrows(IllegalArgumentException.class, () -> new CombatSettings(100, 25, value, 5f));
        assertThrows(IllegalArgumentException.class, () -> new CombatSettings(100, 25, 0.5f, value));
    }

    @ParameterizedTest @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void rejectsNonPositiveHealthAndDamage(int value) {
        assertThrows(IllegalArgumentException.class, () -> new CombatSettings(value, 25, 0.5f, 5f));
        assertThrows(IllegalArgumentException.class, () -> new CombatSettings(100, value, 0.5f, 5f));
    }
}
