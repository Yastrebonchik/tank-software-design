package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static ru.mipt.bit.platformer.Tank.MoveDirection.*;

class TankTest {
    private final GameField field = new GameField(5, 4);
    private final Tank tank = new Tank(1, 1);

    TankTest() { field.addOccupant(tank); }

    @Test void initialState() {
        assertEquals(1, tank.getX());
        assertEquals(1, tank.getY());
        assertEquals(1, tank.getMovementStartX());
        assertEquals(1, tank.getMovementStartY());
        assertEquals(RIGHT, tank.getDirection());
        assertEquals(1f, tank.getMovementProgress());
        assertFalse(tank.isMoving());
        assertTrue(tank.tileOccupied(1, 1));
        assertFalse(tank.tileOccupied(0, 1));
        assertFalse(tank.tileOccupied(1, 0));
    }

    @ParameterizedTest @CsvSource({"UP,1,2", "DOWN,1,0", "LEFT,0,1", "RIGHT,2,1"})
    void movesAndFieldImmediatelySeesNewPosition(Tank.MoveDirection direction, int x, int y) {
        assertTrue(tank.tryMove(direction, field));
        assertEquals(x, tank.getX()); assertEquals(y, tank.getY());
        assertEquals(direction, tank.getDirection());
        assertEquals(1, tank.getMovementStartX()); assertEquals(1, tank.getMovementStartY());
        assertEquals(0f, tank.getMovementProgress()); assertTrue(tank.isMoving());
        assertTrue(field.tileOccupied(x, y)); assertFalse(field.tileOccupied(1, 1));
    }

    @ParameterizedTest @CsvSource({"LEFT,0,1", "RIGHT,4,1", "DOWN,1,0", "UP,1,3"})
    void blocksEachBoundary(Tank.MoveDirection direction, int x, int y) {
        Tank edge = new Tank(x, y);
        field.addOccupant(edge);
        assertFalse(edge.tryMove(direction, field));
        assertEquals(x, edge.getX()); assertEquals(y, edge.getY());
        assertEquals(direction, edge.getDirection()); assertFalse(edge.isMoving());
    }

    @Test void blockedByTreeButTurns() {
        field.addOccupant(new Tree(1, 2));
        assertFalse(tank.tryMove(UP, field));
        assertEquals(UP, tank.getDirection()); assertFalse(tank.isMoving());
        assertTrue(tank.tileOccupied(1, 1));
        assertTrue(tank.tryMove(RIGHT, field));
    }

    @Test void blockedByAnotherTank() {
        field.addOccupant(new Tank(2, 1));
        assertFalse(tank.tryMove(RIGHT, field));
        assertTrue(tank.tileOccupied(1, 1));
    }

    @Test void ignoresCommandsWhileMoving() {
        tank.tryMove(UP, field); tank.update(0.1f);
        assertFalse(tank.tryMove(LEFT, field));
        assertEquals(UP, tank.getDirection()); assertTrue(tank.tileOccupied(1, 2));
        assertEquals(0.25f, tank.getMovementProgress(), 0.000001f);
    }

    @Test void stepCompletesInPointFourSeconds() {
        tank.tryMove(UP, field);
        tank.update(0f); assertEquals(0f, tank.getMovementProgress());
        tank.update(0.2f); assertEquals(0.5f, tank.getMovementProgress());
        assertTrue(tank.isMoving());
        tank.update(0.2f); assertFalse(tank.isMoving());
        assertEquals(1f, tank.getMovementProgress());
        assertEquals(tank.getX(), tank.getMovementStartX());
        assertEquals(tank.getY(), tank.getMovementStartY());
        assertTrue(tank.tryMove(RIGHT, field));
        tank.update(100f); assertEquals(1f, tank.getMovementProgress());
    }

    @ParameterizedTest @ValueSource(floats = {-1f, Float.NaN, Float.POSITIVE_INFINITY})
    void rejectsInvalidTimeWithoutChangingState(float delta) {
        assertThrows(IllegalArgumentException.class, () -> tank.update(delta));
        assertEquals(1f, tank.getMovementProgress());
    }

    @Test void destinationBlocksOtherTankAndSourceIsImmediatelyFree() {
        Tank rival = new Tank(3, 1); Tank follower = new Tank(0, 1);
        field.addOccupant(rival); field.addOccupant(follower);
        assertTrue(tank.tryMove(RIGHT, field));
        assertFalse(rival.tryMove(LEFT, field));
        assertTrue(follower.tryMove(RIGHT, field));
        assertTrue(follower.tileOccupied(1, 1));
    }

    @Test void invalidDirectionDoesNotCorruptState() {
        assertThrows(NullPointerException.class, () -> tank.tryMove(null, field));
        assertEquals(RIGHT, tank.getDirection()); assertTrue(tank.tileOccupied(1, 1));
    }
}
