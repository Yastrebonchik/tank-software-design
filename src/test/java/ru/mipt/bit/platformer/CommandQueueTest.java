package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandQueueTest {
    @Test void executesInSubmissionOrderExactlyOnceAndDefersNestedCommands() {
        CommandQueue queue = new CommandQueue();
        List<Integer> calls = new ArrayList<>();
        queue.submit(() -> { calls.add(1); queue.submit(() -> calls.add(3)); });
        queue.submit(() -> calls.add(2));
        assertTrue(calls.isEmpty());
        queue.executePending();
        assertEquals(Arrays.asList(1, 2), calls);
        queue.executePending(); queue.executePending();
        assertEquals(Arrays.asList(1, 2, 3), calls);
    }

    @Test void anAlreadyExecutedCommandIsNotRetriedAfterAnException() {
        CommandQueue queue = new CommandQueue();
        List<Integer> calls = new ArrayList<>();
        queue.submit(() -> calls.add(1));
        queue.submit(() -> { throw new IllegalStateException(); });
        assertThrows(IllegalStateException.class, queue::executePending);
        queue.executePending();
        assertEquals(Collections.singletonList(1), calls);
        assertThrows(NullPointerException.class, () -> queue.submit(null));
    }
}
