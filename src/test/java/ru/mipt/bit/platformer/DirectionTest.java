package ru.mipt.bit.platformer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {
    @ParameterizedTest @CsvSource({"UP,0,1", "DOWN,0,-1", "LEFT,-1,0", "RIGHT,1,0"})
    void describesLogicalOffsets(Direction direction, int x, int y) {
        assertEquals(x, direction.getDeltaX());
        assertEquals(y, direction.getDeltaY());
    }
}
