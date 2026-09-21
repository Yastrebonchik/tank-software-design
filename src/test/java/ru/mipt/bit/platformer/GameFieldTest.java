package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class GameFieldTest {
    @Test void emptyFieldHasNoOccupantsIncludingOutsideBounds() {
        GameField field = new GameField(3, 2);
        assertFalse(field.tileOccupied(1, 1)); assertFalse(field.tileOccupied(-1, 0));
    }

    @ParameterizedTest @CsvSource({"0,0,true", "2,1,true", "-1,0,false", "0,-1,false", "3,0,false", "0,2,false"})
    void rectangularBounds(int x, int y, boolean inside) {
        assertEquals(inside, new GameField(3, 2).isInside(x, y));
    }

    @Test void delegatesToObjectsWithDifferentShapes() {
        GameField field = new GameField(10, 8);
        field.addOccupant(new Tree(1, 3));
        field.addOccupant((x, y) -> y == 5 && x >= 2 && x <= 4);
        assertTrue(field.tileOccupied(1, 3));
        assertTrue(field.tileOccupied(2, 5)); assertTrue(field.tileOccupied(4, 5));
        assertFalse(field.tileOccupied(5, 5)); assertFalse(field.tileOccupied(3, 4));
    }

    @Test void stopsAtFirstOccupiedObject() {
        GameField field = new GameField(3, 3);
        AtomicInteger calls = new AtomicInteger();
        field.addOccupant((x, y) -> true);
        field.addOccupant((x, y) -> { calls.incrementAndGet(); return false; });
        assertTrue(field.tileOccupied(0, 0)); assertEquals(0, calls.get());
    }

    @Test void visitsEveryObjectForFreeTile() {
        GameField field = new GameField(3, 3);
        AtomicInteger calls = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            field.addOccupant((x, y) -> { calls.incrementAndGet(); return false; });
        }
        assertFalse(field.tileOccupied(0, 0)); assertEquals(3, calls.get());
    }

    @Test void rejectsNullOccupant() {
        assertThrows(NullPointerException.class, () -> new GameField(3, 3).addOccupant(null));
    }
}
