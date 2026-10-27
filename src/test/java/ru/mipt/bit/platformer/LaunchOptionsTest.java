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
        assertEquals(3, level.getBotStarts().size());
    }

    @Test void acceptsAFilePathContainingSpaces() throws IOException {
        Path file = directory.resolve("my level.txt");
        Files.writeString(file, "X_\n_T");
        LevelDefinition level = LaunchOptions.parse("--level", file.toString(), "--bots", "0").createLevelSource().load();
        assertEquals(2, level.getWidth());
        assertEquals(1, level.getTreePositions().size());
    }

    @Test void passesRandomParametersAndSeedToTheSource() throws IOException {
        LaunchOptions options = LaunchOptions.parse("--random", "--width", "4", "--height", "3", "--trees", "10", "--seed", "-42", "--bots", "0");
        LevelDefinition a = options.createLevelSource().load();
        LevelDefinition b = options.createLevelSource().load();
        assertEquals(4, a.getWidth()); assertEquals(3, a.getHeight());
        assertEquals(10, a.getTreePositions().size());
        assertEquals(a.getPlayerStart(), b.getPlayerStart());
        assertEquals(a.getTreePositions(), b.getTreePositions());
    }

    @Test void fileModeSupportsSeedAndDefaultOrExplicitBots() throws IOException {
        Path file = directory.resolve("bots.txt");
        Files.writeString(file, "X____\n__T__");
        LevelDefinition defaults = LaunchOptions.parse("--level", file.toString()).createLevelSource().load();
        assertEquals(3, defaults.getBotStarts().size());
        LaunchOptions options = LaunchOptions.parse("--level", file.toString(), "--bots", "5", "--seed", "42");
        LevelDefinition a = options.createLevelSource().load();
        LevelDefinition b = options.createLevelSource().load();
        assertEquals(5, a.getBotStarts().size());
        assertEquals(a.getBotStarts(), b.getBotStarts());
        assertEquals(defaults.getTreePositions(), a.getTreePositions());
    }

    @Test void layoutPlacementAndAiHaveIndependentReproducibleStreams() throws IOException {
        LaunchOptions options = LaunchOptions.parse("--seed", "42");
        java.util.Random ai = options.createAiRandom();
        java.util.Random replay = options.createAiRandom();
        for (int i = 0; i < 100; i++) { assertEquals(ai.nextInt(), replay.nextInt()); }
        LevelDefinition withBots = options.createLevelSource().load();
        LevelDefinition again = options.createLevelSource().load();
        LevelDefinition withoutBots = LaunchOptions.parse("--seed", "42", "--bots", "0").createLevelSource().load();
        assertEquals(withBots.getBotStarts(), again.getBotStarts());
        assertEquals(withBots.getPlayerStart(), withoutBots.getPlayerStart());
        assertEquals(withBots.getTreePositions(), withoutBots.getTreePositions());
        assertTrue(withoutBots.getBotStarts().isEmpty());
    }

    @ParameterizedTest @ValueSource(strings = {"--unknown", "--width", "--level --random", "--random --random",
            "--width 4 --width 5", "--level map.txt --random", "--width x",
            "--bots", "--bots x", "--bots -1", "--bots 1 --bots 2",
            "--level map.txt --width 5", "--level map.txt --height 5", "--level map.txt --trees 0", "--seed x", "--width 0", "--trees -1", "--width 1 --height 1 --trees 0", "--trees 79"})
    void rejectsInvalidOrConflictingOptions(String args) {
        assertThrows(IllegalArgumentException.class, () -> LaunchOptions.parse(args.split(" ")).createLevelSource());
    }
}
