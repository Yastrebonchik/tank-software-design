package ru.mipt.bit.platformer;

import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

class TankControlTest {
    private final Tank tank = new Tank(2, 2, 0.4f);
    private final GameField field = new GameField(5, 5);

    TankControlTest() { field.addOccupant(tank); }

    @ParameterizedTest @EnumSource(Direction.class)
    void commandsAndAiUseTheChosenDirection(Direction direction) {
        CountingRandom random = new CountingRandom(direction);
        new RandomTankController(tank, field, random).processInput();
        assertEquals(2 + direction.getDeltaX(), tank.getX());
        assertEquals(2 + direction.getDeltaY(), tank.getY());
        assertEquals(direction, tank.getDirection());
        assertEquals(1, random.calls);
        assertTrue(tank.tileOccupied(2, 2));
    }

    @Test void reusableCommandActsOnlyOnItsReceiverAndCannotInterruptAMove() {
        Tank other = new Tank(4, 4, 0.4f);
        field.addOccupant(other);
        GameCommand command = new MoveTankCommand(tank, field, Direction.UP);
        command.execute(); command.execute();
        assertEquals(3, tank.getY());
        assertFalse(other.isMoving());
        tank.update(0.4f); command.execute();
        assertEquals(4, tank.getY());
        tank.update(0.4f); command.execute();
        assertEquals(4, tank.getY()); assertFalse(tank.isMoving());
    }

    @Test void aiDoesNotChooseAnotherDirectionWhileMoving() {
        CountingRandom random = new CountingRandom(Direction.RIGHT);
        RandomTankController controller = new RandomTankController(tank, field, random);
        controller.processInput(); tank.update(0.2f);
        controller.processInput(); controller.processInput();
        assertEquals(1, random.calls); assertEquals(3, tank.getX());
        tank.update(0.2f); controller.processInput();
        assertEquals(2, random.calls); assertEquals(4, tank.getX());
    }

    @Test void blockedAiMakesOnlyOneAttemptAndCanRetryNextFrame() {
        field.addOccupant(new Tree(3, 2));
        Random random = new Random() {
            int calls;
            @Override public int nextInt(int bound) {
                return (calls++ == 0 ? Direction.RIGHT : Direction.UP).ordinal();
            }
        };
        RandomTankController controller = new RandomTankController(tank, field, random);
        controller.processInput();
        assertFalse(tank.isMoving()); assertEquals(Direction.RIGHT, tank.getDirection());
        controller.processInput();
        assertTrue(tank.isMoving()); assertEquals(3, tank.getY());
    }

    @ParameterizedTest @EnumSource(Direction.class)
    void aiCannotLeaveAnyBoundary(Direction direction) {
        Tank edge = new Tank(direction == Direction.LEFT ? 0 : direction == Direction.RIGHT ? 4 : 2,
                direction == Direction.DOWN ? 0 : direction == Direction.UP ? 4 : 2, 0.4f);
        field.addOccupant(edge);
        new RandomTankController(edge, field, new CountingRandom(direction)).processInput();
        assertFalse(edge.isMoving());
        assertTrue(field.isInside(edge.getX(), edge.getY()));
    }

    @Test void rejectsMissingDependencies() {
        assertThrows(NullPointerException.class, () -> new MoveTankCommand(null, field, Direction.UP));
        assertThrows(NullPointerException.class, () -> new MoveTankCommand(tank, null, Direction.UP));
        assertThrows(NullPointerException.class, () -> new MoveTankCommand(tank, field, null));
        assertThrows(NullPointerException.class, () -> new RandomTankController(null, field, new Random()));
        assertThrows(NullPointerException.class, () -> new RandomTankController(tank, null, new Random()));
        assertThrows(NullPointerException.class, () -> new RandomTankController(tank, field, null));
    }

    private static final class CountingRandom extends Random {
        private final Direction direction;
        private int calls;
        CountingRandom(Direction direction) { this.direction = direction; }
        @Override public int nextInt(int bound) {
            assertEquals(4, bound);
            calls++;
            return direction.ordinal();
        }
    }
}
