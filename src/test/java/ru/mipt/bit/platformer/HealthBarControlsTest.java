package ru.mipt.bit.platformer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.badlogic.gdx.Input.Keys.*;

class HealthBarControlsTest {
    private final Set<Integer> pressed = new HashSet<>();
    private final CommandQueue commands = new CommandQueue();
    private final KeyboardController keyboard = new KeyboardController(pressed::contains, commands);
    private final HealthBarVisibility visibility = new HealthBarVisibility();

    HealthBarControlsTest() { keyboard.bindOnPress(new ToggleHealthBarsCommand(visibility), L); }

    @Test void initiallyHiddenAndChangesOnlyInTheCommandExecutionPhase() {
        assertFalse(visibility.isVisible());
        pressed.add(L); keyboard.processInput();
        assertFalse(visibility.isVisible());
        commands.executePending();
        assertTrue(visibility.isVisible());
    }

    @Test void heldKeyTogglesOnceAndASecondPressTogglesBack() {
        pressed.add(L);
        for (int frame = 0; frame < 100; frame++) { frame(); assertTrue(visibility.isVisible()); }
        pressed.clear(); frame(); assertTrue(visibility.isVisible());
        pressed.add(L); frame(); assertFalse(visibility.isVisible());
        frame(); assertFalse(visibility.isVisible());
    }

    @Test void severalInputPollsCannotQueueDuplicateTogglesBeforeExecution() {
        pressed.add(L);
        keyboard.processInput(); keyboard.processInput(); keyboard.processInput();
        commands.executePending(); assertTrue(visibility.isVisible());
        commands.executePending(); assertTrue(visibility.isVisible());
    }

    @Test void unrelatedKeysAndRepeatedBindingsKeepTheirOwnState() {
        AtomicInteger held = new AtomicInteger(), once = new AtomicInteger();
        keyboard.bind(held::incrementAndGet, D);
        keyboard.bindOnPress(once::incrementAndGet, ENTER);
        pressed.add(D); pressed.add(ENTER);
        frame(); frame(); frame();
        assertEquals(3, held.get()); assertEquals(1, once.get()); assertFalse(visibility.isVisible());
        pressed.add(L); frame();
        assertTrue(visibility.isVisible()); assertEquals(4, held.get()); assertEquals(1, once.get());
    }

    @Test void alternativeKeysFormOnePressAndTheBindingOwnsItsArray() {
        AtomicInteger calls = new AtomicInteger();
        int[] keys = {F, ENTER};
        keyboard.bindOnPress(calls::incrementAndGet, keys);
        keys[0] = ESCAPE;
        pressed.add(F); frame(); assertEquals(1, calls.get());
        pressed.add(ENTER); frame(); assertEquals(1, calls.get());
        pressed.remove(F); frame(); assertEquals(1, calls.get());
        pressed.clear(); frame(); pressed.add(ENTER); frame(); assertEquals(2, calls.get());
        pressed.clear(); frame(); pressed.add(ESCAPE); frame(); assertEquals(2, calls.get());
    }

    @Test void commandCanBeReusedAndDoesNotChangeAnotherScenesSetting() {
        HealthBarVisibility otherScene = new HealthBarVisibility();
        ToggleHealthBarsCommand command = new ToggleHealthBarsCommand(visibility);
        command.execute(); assertTrue(visibility.isVisible()); assertFalse(otherScene.isVisible());
        command.execute(); assertFalse(visibility.isVisible());
    }

    @Test void validatesPressBindingsAndCommandReceiver() {
        assertThrows(IllegalArgumentException.class, () -> keyboard.bindOnPress(() -> {}));
        assertThrows(NullPointerException.class, () -> keyboard.bindOnPress(null, L));
        assertThrows(NullPointerException.class, () -> keyboard.bindOnPress(() -> {}, (int[]) null));
        assertThrows(NullPointerException.class, () -> new ToggleHealthBarsCommand(null));
    }

    private void frame() { keyboard.processInput(); commands.executePending(); }
}
