package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import ru.mipt.bit.platformer.level.LevelDefinition;
import static org.junit.jupiter.api.Assertions.*;

class LaunchOptionsTest {
    @TempDir Path directory;

    @Test void defaultModeIsTenByEightWithTwelveTrees() throws IOException {
        LevelDefinition level = LaunchOptions.parse().createLevelSource().load();
        assertEquals(10, level.getWidth());
        assertEquals(8, level.getHeight());
        assertEquals(12, level.getTreePositions().size());
    }

    @Test void acceptsAFilePathContainingSpaces() throws IOException {
        Path file = directory.resolve("my level.txt");
        Files.writeString(file, "X_\n_T");
        LevelDefinition level = LaunchOptions.parse("--level", file.toString()).createLevelSource().load();
        assertEquals(2, level.getWidth());
        assertEquals(1, level.getTreePositions().size());
    }

    @Test void passesRandomParametersAndSeedToTheSource() throws IOException {
        LaunchOptions options = LaunchOptions.parse("--random", "--width", "4", "--height", "3", "--trees", "10", "--seed", "-42");
        LevelDefinition a = options.createLevelSource().load();
        LevelDefinition b = options.createLevelSource().load();
        assertEquals(4, a.getWidth()); assertEquals(3, a.getHeight());
        assertEquals(10, a.getTreePositions().size());
        assertEquals(a.getPlayerStart(), b.getPlayerStart());
        assertEquals(a.getTreePositions(), b.getTreePositions());
    }

    @ParameterizedTest @ValueSource(strings = {"--unknown", "--width", "--level --random", "--random --random",
            "--width 4 --width 5", "--level map.txt --random", "--level map.txt --seed 1", "--width x",
            "--seed x", "--width 0", "--trees -1", "--width 1 --height 1 --trees 0", "--trees 79"})
    void rejectsInvalidOrConflictingOptions(String args) {
        assertThrows(IllegalArgumentException.class, () -> LaunchOptions.parse(args.split(" ")).createLevelSource());
    }
}
