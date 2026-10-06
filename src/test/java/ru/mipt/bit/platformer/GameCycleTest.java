package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameCycleTest {
    private final List<String> calls = new ArrayList<>();
    private final FrameRenderer renderer = new FrameRenderer() {
        @Override public void prepareFrame() { calls.add("prepare"); }
        @Override public void render() { calls.add("render"); }
        @Override public void dispose() { calls.add("dispose"); }
    };
    private final GameCycle cycle = new GameCycle(
            () -> calls.add("input"), dt -> calls.add("update:" + dt), renderer);

    @Test void coordinatesFramesThroughInterfacesInTheOriginalOrder() {
        cycle.tick(0.2f);
        cycle.tick(0f);
        assertEquals(Arrays.asList("input", "prepare", "update:0.2", "render",
                "input", "prepare", "update:0.0", "render"), calls);
    }

    @Test void preparesTheAcceptedMoveBeforeUpdatingItsProgress() {
        GameField field = new GameField(4, 4);
        Tank tank = new Tank(1, 1, 0.4f);
        field.addOccupant(tank);
        FrameRenderer observer = new FrameRenderer() {
            @Override public void prepareFrame() {
                assertTrue(tank.tileOccupied(2, 1));
                assertEquals(0f, tank.getMovementProgress());
            }
            @Override public void render() { assertEquals(0.5f, tank.getMovementProgress()); }
            @Override public void dispose() { }
        };
        new GameCycle(() -> tank.tryMove(Tank.MoveDirection.RIGHT, field), tank, observer).tick(0.2f);
    }

    @ParameterizedTest
    @ValueSource(floats = {-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void invalidTimeCannotTriggerInputOrRendering(float delta) {
        assertThrows(IllegalArgumentException.class, () -> cycle.tick(delta));
        assertTrue(calls.isEmpty());
    }

    @Test void disposesOnceAndRejectsFurtherFrames() {
        cycle.dispose();
        cycle.dispose();
        assertThrows(IllegalStateException.class, () -> cycle.tick(0.1f));
        assertEquals(Collections.singletonList("dispose"), calls);
    }

    @Test void rejectsMissingDependencies() {
        assertThrows(NullPointerException.class, () -> new GameCycle(null, dt -> {}, renderer));
        assertThrows(NullPointerException.class, () -> new GameCycle(() -> {}, null, renderer));
        assertThrows(NullPointerException.class, () -> new GameCycle(() -> {}, dt -> {}, null));
    }
}
