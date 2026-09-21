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
    private final GameWorld world = GameWorld.createDefault(10, 8);
    private final Tank tank = world.getPlayer();
    private final KeyboardController controller = KeyboardController.forPlayer(pressed::contains, tank, world.getField());

    @ParameterizedTest @CsvSource({"UP,1,2", "W,1,2", "LEFT,0,1", "A,0,1", "DOWN,1,0", "S,1,0", "RIGHT,2,1", "D,2,1"})
    void eachMovementKey(String key, int x, int y) throws Exception {
        pressed.add(Keys.class.getField(key).getInt(null));
        controller.processInput(); assertTrue(tank.tileOccupied(x, y));
    }

    @Test void noInputDoesNotMove() {
        controller.processInput(); assertFalse(tank.isMoving()); assertTrue(tank.tileOccupied(1, 1));
    }

    @Test void heldKeyWaitsForStepCompletionThenRepeats() {
        pressed.add(D); controller.processInput(); controller.processInput();
        assertTrue(tank.tileOccupied(2, 1));
        world.update(0.4f); controller.processInput();
        assertTrue(tank.tileOccupied(3, 1));
        pressed.clear(); world.update(0.4f); controller.processInput();
        assertTrue(tank.tileOccupied(3, 1)); assertFalse(tank.isMoving());
    }

    @Test void successfulEarlierDirectionWins() {
        pressed.add(UP); pressed.add(LEFT); pressed.add(DOWN); pressed.add(RIGHT);
        controller.processInput(); assertTrue(tank.tileOccupied(1, 2));
        assertEquals(Tank.MoveDirection.UP, tank.getDirection());
    }

    @Test void blockedEarlierDirectionAllowsNextDirection() {
        world.addTree(new Tree(1, 2));
        pressed.add(UP); pressed.add(LEFT);
        controller.processInput(); assertTrue(tank.tileOccupied(0, 1));
        assertEquals(Tank.MoveDirection.LEFT, tank.getDirection());
    }

    @Test void newBindingWorksAndAlternativeKeysInvokeActionOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();
        int[] keys = { SPACE, ENTER };
        controller.bind(calls::incrementAndGet, keys);
        keys[0] = ESCAPE;
        pressed.add(SPACE); pressed.add(ENTER);
        controller.processInput(); assertEquals(1, calls.get());
        assertFalse(tank.isMoving());
        pressed.clear(); pressed.add(ESCAPE);
        controller.processInput(); assertEquals(1, calls.get());
    }

    @Test void invalidBindingRejected() {
        assertThrows(IllegalArgumentException.class, () -> controller.bind(() -> {}));
        assertThrows(NullPointerException.class, () -> controller.bind(null, SPACE));
    }
}
