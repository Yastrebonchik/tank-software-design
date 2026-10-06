package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest {
    private final GameField field = new GameField(10, 8);
    private final GameWorld world = new GameWorld(field);

    @Test void registersAnUnknownOccupantWithoutCopyingItsCoordinates() {
        int[] position = {2, 3};
        world.addOccupant((x, y) -> x == position[0] && y == position[1]);
        assertSame(field, world.getField());
        assertTrue(field.tileOccupied(2, 3));
        position[0] = 4;
        assertFalse(field.tileOccupied(2, 3));
        assertTrue(field.tileOccupied(4, 3));
    }

    @Test void updatesUnknownObjectsInRegistrationOrderOncePerFrame() {
        List<String> calls = new ArrayList<>();
        Updatable first = dt -> calls.add("first:" + dt);
        Updatable second = dt -> calls.add("second:" + dt);
        world.addUpdatable(first);
        world.addUpdatable(second);
        world.addUpdatable(first);
        world.update(0.2f);
        world.update(0f);
        assertEquals(Arrays.asList("first:0.2", "second:0.2", "first:0.0", "second:0.0"), calls);
        assertFalse(field.tileOccupied(0, 0));
    }

    @Test void anObjectCanBothOccupyTilesAndReceiveUpdates() {
        class MovingObstacle implements CellOccupant, Updatable {
            private int x = 1;
            @Override public boolean tileOccupied(int x, int y) { return this.x == x && y == 2; }
            @Override public void update(float dt) { x++; }
        }
        MovingObstacle obstacle = new MovingObstacle();
        world.addOccupant(obstacle);
        world.addUpdatable(obstacle);
        world.update(0.1f);
        assertFalse(field.tileOccupied(1, 2));
        assertTrue(field.tileOccupied(2, 2));
    }

    @ParameterizedTest
    @ValueSource(floats = {-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void rejectsInvalidTimeBeforeUpdatingObjects(float delta) {
        world.addUpdatable(dt -> fail("An invalid frame must not update any object"));
        assertThrows(IllegalArgumentException.class, () -> world.update(delta));
    }

    @Test void rejectsMissingDependencies() {
        assertThrows(NullPointerException.class, () -> new GameWorld(null));
        assertThrows(NullPointerException.class, () -> world.addOccupant(null));
        assertThrows(NullPointerException.class, () -> world.addUpdatable(null));
    }
}
