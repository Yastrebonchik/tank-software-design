package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TreeTest {
    @Test void occupiesOnlyItsOwnTileAndExposesReadOnlyCoordinates() {
        Tree tree = new Tree(1, 3);
        assertEquals(1, tree.getX()); assertEquals(3, tree.getY());
        assertTrue(tree.tileOccupied(1, 3));
        assertFalse(tree.tileOccupied(3, 1));
        assertFalse(tree.tileOccupied(1, 2)); assertFalse(tree.tileOccupied(0, 3));
    }
}
