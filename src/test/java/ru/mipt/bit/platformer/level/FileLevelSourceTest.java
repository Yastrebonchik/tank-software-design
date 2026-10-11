package ru.mipt.bit.platformer.level;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import ru.mipt.bit.platformer.level.LevelDefinition.Position;
import static org.junit.jupiter.api.Assertions.*;

class FileLevelSourceTest {
    @TempDir Path directory;

    private FileLevelSource source(String text) throws IOException {
        Path path = directory.resolve("map.txt");
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        return new FileLevelSource(path);
    }

    @Test void loadsTheLectureExampleWithTheFirstRowAtTheTop() throws IOException {
        LevelDefinition level = source("___T__T___\n__TT__TTTT\n_________T\nTTTT__T__T\n_____X____\n__________\n").load();
        assertEquals(10, level.getWidth());
        assertEquals(6, level.getHeight());
        assertEquals(new Position(5, 1), level.getPlayerStart());
        assertEquals(new HashSet<>(Arrays.asList(
                new Position(3, 5), new Position(6, 5), new Position(2, 4), new Position(3, 4),
                new Position(6, 4), new Position(7, 4), new Position(8, 4), new Position(9, 4),
                new Position(9, 3), new Position(0, 2), new Position(1, 2), new Position(2, 2),
                new Position(3, 2), new Position(6, 2), new Position(9, 2))), new HashSet<>(level.getTreePositions()));
    }

    @ParameterizedTest @ValueSource(strings = {"X_\n_T", "X_\n_T\n", "X_\r\n_T\r\n", "\uFEFFX_\r\n_T"})
    void acceptsCommonLineEndingsAndOptionalBom(String text) throws IOException {
        LevelDefinition level = source(text).load();
        assertEquals(2, level.getHeight());
        assertEquals(new Position(0, 1), level.getPlayerStart());
        assertEquals(Arrays.asList(new Position(1, 0)), level.getTreePositions());
    }

    @ParameterizedTest @ValueSource(strings = {"", "\n", "__", "XX", "X_\n_", "X_\n\n__", "X_\n__\n\n", "X ", "X?", "X\t"})
    void rejectsMalformedMaps(String text) throws IOException {
        FileLevelSource source = source(text);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, source::load);
        assertTrue(error.getMessage().contains("map.txt"));
    }

    @Test void reportsTheLocationOfAnUnknownSymbol() throws IOException {
        FileLevelSource source = source("X_\n_?");
        assertTrue(assertThrows(IllegalArgumentException.class, source::load).getMessage().contains("line 2, column 2"));
    }

    @Test void propagatesReadFailures() {
        assertThrows(IOException.class, () -> new FileLevelSource(directory.resolve("missing.txt")).load());
    }
}
