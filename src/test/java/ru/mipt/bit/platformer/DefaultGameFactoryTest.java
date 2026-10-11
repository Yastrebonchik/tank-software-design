package ru.mipt.bit.platformer;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefaultGameFactoryTest {
    @Test void readsTheLevelBeforeTryingToAllocateGraphicsResources() {
        IOException failure = new IOException("missing map.txt");
        DefaultGameFactory factory = new DefaultGameFactory(() -> { throw failure; });
        UncheckedIOException error = assertThrows(UncheckedIOException.class, factory::create);
        assertSame(failure, error.getCause());
        assertTrue(error.getMessage().contains("missing map.txt"));
    }

    @Test void malformedLevelsAreNotSilentlyReplacedWithRandomOnes() {
        IllegalArgumentException failure = new IllegalArgumentException("invalid map");
        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> new DefaultGameFactory(() -> { throw failure; }).create()));
    }
}
