package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class HealthTankStateResolverTest {
    private final HealthTankStateResolver resolver = new HealthTankStateResolver();

    @ParameterizedTest
    @CsvSource({"100,100,Light", "71,100,Light", "70,100,Medium", "15,100,Medium", "14,100,Heavy",
            "1,100,Heavy", "0,100,Heavy", "140,200,Medium", "30,200,Medium", "29,200,Heavy",
            "3,3,Light", "2,3,Medium", "1,7,Heavy", "2,7,Medium",
            "2147483647,2147483647,Light", "1503238552,2147483647,Medium", "1503238553,2147483647,Light",
            "322122547,2147483647,Heavy", "322122548,2147483647,Medium"})
    void selectsExactPercentagesWithoutRoundingOrIntegerOverflow(int health, int max, String state) {
        assertEquals(state + "TankState", resolver.resolve(health, max).getClass().getSimpleName());
    }

    @ParameterizedTest @CsvSource({"-1,100", "101,100", "0,0", "0,-1"})
    void rejectsInvalidHealth(int health, int max) {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(health, max));
    }

    @Test void healthStatesPreserveNormalReloadRate() {
        for (int health : new int[]{100, 70, 10}) {
            TankState state = resolver.resolve(health, 100);
            assertEquals(0f, state.movementProgress(0f, 0.7f));
            assertEquals(0f, state.reloadProgress(0f));
            assertEquals(0.25f, state.reloadProgress(0.25f));
        }
    }
}
