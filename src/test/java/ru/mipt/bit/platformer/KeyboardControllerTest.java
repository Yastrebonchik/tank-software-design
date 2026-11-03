package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.badlogic.gdx.Input.Keys;
import static org.junit.jupiter.api.Assertions.*;
import static com.badlogic.gdx.Input.Keys.*;

class KeyboardControllerTest {
    private final Set<Integer> pressed = new HashSet<>();
    private final GameWorld world = new GameWorld(new GameField(10, 8));
    private final Tank tank = new Tank(1, 1, 0.4f);
    private final CommandQueue commands = new CommandQueue();
    private final KeyboardController controller = new KeyboardController(pressed::contains, commands);

    KeyboardControllerTest() {
        world.addOccupant(tank);
        world.addUpdatable(tank);
        world.addOccupant(new Tree(1, 3));
        PlayerKeyBindings.install(controller, tank, world);
    }

    @ParameterizedTest @CsvSource({"UP,1,2", "W,1,2", "LEFT,0,1", "A,0,1", "DOWN,1,0", "S,1,0", "RIGHT,2,1", "D,2,1"})
    void eachMovementKey(String key, int x, int y) throws Exception {
        pressed.add(Keys.class.getField(key).getInt(null));
        processInput(); assertTrue(tank.tileOccupied(x, y));
    }

    @Test void noInputDoesNotMove() {
        processInput(); assertFalse(tank.isMoving()); assertTrue(tank.tileOccupied(1, 1));
    }

    @Test void heldKeyWaitsForStepCompletionThenRepeats() {
        pressed.add(D); processInput(); processInput();
        assertTrue(tank.tileOccupied(2, 1));
        world.update(0.4f); processInput();
        assertTrue(tank.tileOccupied(3, 1));
        pressed.clear(); world.update(0.4f); processInput();
        assertTrue(tank.tileOccupied(3, 1)); assertFalse(tank.isMoving());
    }

    @Test void successfulEarlierDirectionWins() {
        pressed.add(UP); pressed.add(LEFT); pressed.add(DOWN); pressed.add(RIGHT);
        processInput(); assertTrue(tank.tileOccupied(1, 2));
        assertEquals(Direction.UP, tank.getDirection());
    }

    @Test void blockedEarlierDirectionAllowsNextDirection() {
        world.addOccupant(new Tree(1, 2));
        pressed.add(UP); pressed.add(LEFT);
        processInput(); assertTrue(tank.tileOccupied(0, 1));
        assertEquals(Direction.LEFT, tank.getDirection());
    }

    @Test void newBindingWorksAndAlternativeKeysInvokeActionOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();
        int[] keys = { F, ENTER };
        controller.bind(calls::incrementAndGet, keys);
        keys[0] = ESCAPE;
        pressed.add(F); pressed.add(ENTER);
        processInput(); assertEquals(1, calls.get());
        assertFalse(tank.isMoving());
        pressed.clear(); pressed.add(ESCAPE);
        processInput(); assertEquals(1, calls.get());
    }

    private void processInput() {
        controller.processInput();
        commands.executePending();
    }

    @Test void inputOnlyQueuesCommandsUntilTheExecutionPhase() {
        pressed.add(D);
        controller.processInput();
        assertFalse(tank.isMoving());
        commands.executePending();
        assertTrue(tank.isMoving());
    }

    @Test void heldSpaceRepeatsOnlyWhenReloadedAndStopsOnRelease() {
        pressed.add(SPACE);
        processInput();
        assertEquals(1, bulletCount());
        processInput();
        assertEquals(1, bulletCount());
        world.update(0.5f);
        processInput();
        assertEquals(2, bulletCount());
        pressed.clear();
        world.update(0.5f);
        processInput();
        assertEquals(2, bulletCount());
    }

    @Test void successfulMovePreventsShootingInTheSameFrame() {
        pressed.add(D); pressed.add(SPACE);
        processInput();
        assertTrue(tank.isMoving());
        assertEquals(0, bulletCount());
        assertEquals(0f, tank.getReloadRemaining());
    }

    private long bulletCount() {
        return world.getObjects().stream().filter(Bullet.class::isInstance).count();
    }

    @Test void invalidBindingRejected() {
        assertThrows(IllegalArgumentException.class, () -> controller.bind(() -> {}));
        assertThrows(NullPointerException.class, () -> controller.bind(null, SPACE));
    }
}
