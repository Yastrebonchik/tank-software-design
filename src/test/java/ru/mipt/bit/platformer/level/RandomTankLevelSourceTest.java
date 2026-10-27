package ru.mipt.bit.platformer.level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.mipt.bit.platformer.Direction;
import ru.mipt.bit.platformer.level.LevelDefinition.Position;
import static org.junit.jupiter.api.Assertions.*;

class RandomTankLevelSourceTest {
    @Test void denseLevelsHaveExactCountsUniquePositionsAndAFreePlayerNeighbor() throws IOException {
        for (int seed = 0; seed < 100; seed++) {
            LevelDefinition level = new RandomTankLevelSource(
                    new RandomLevelSource(10, 8, 50, new Random(seed)), 28, new Random(seed)).load();
            assertEquals(28, level.getBotStarts().size());
            Set<Position> occupied = new HashSet<>(level.getTreePositions());
            assertTrue(occupied.add(level.getPlayerStart()));
            for (Position bot : level.getBotStarts()) {
                assertTrue(bot.getX() >= 0 && bot.getX() < 10 && bot.getY() >= 0 && bot.getY() < 8);
                assertTrue(occupied.add(bot));
            }
            assertEquals(79, occupied.size());
            assertTrue(Arrays.stream(Direction.values()).anyMatch(direction -> {
                int x = level.getPlayerStart().getX() + direction.getDeltaX();
                int y = level.getPlayerStart().getY() + direction.getDeltaY();
                return x >= 0 && x < 10 && y >= 0 && y < 8 && !occupied.contains(new Position(x, y));
            }));
        }
    }

    @Test void sameSeedRepeatsPlacementWithoutChangingTerrainOrExistingBots() throws IOException {
        LevelDefinition original = new LevelDefinition(4, 3, new Position(0, 0),
                Collections.singletonList(new Position(1, 1)), Collections.singletonList(new Position(3, 2)));
        LevelDefinition a = new RandomTankLevelSource(() -> original, 3, new Random(42)).load();
        LevelDefinition b = new RandomTankLevelSource(() -> original, 3, new Random(42)).load();
        assertEquals(a.getBotStarts(), b.getBotStarts());
        assertEquals(4, a.getBotStarts().size());
        assertEquals(original.getBotStarts().get(0), a.getBotStarts().get(0));
        assertEquals(original.getTreePositions(), a.getTreePositions());
        assertEquals(original.getPlayerStart(), a.getPlayerStart());
        assertEquals(1, original.getBotStarts().size());
    }

    @Test void noBotsWorksEvenForSingleCellAndDoesNotConsumeRandomness() throws IOException {
        LevelDefinition single = new LevelDefinition(1, 1, new Position(0, 0), Collections.emptyList());
        Random unused = new Random() {
            @Override public int nextInt(int bound) { throw new AssertionError("Unexpected random choice"); }
        };
        assertSame(single, new RandomTankLevelSource(() -> single, 0, unused).load());
        assertThrows(IllegalArgumentException.class, () -> new RandomTankLevelSource(() -> single, 1, new Random()).load());
    }

    @ParameterizedTest @CsvSource({"1,2", "2,1", "1,5", "5,1"})
    void narrowMapsKeepTheOnlyPlayerExit(int width, int height) throws IOException {
        LevelDefinition source = new LevelDefinition(width, height, new Position(0, 0), Collections.emptyList());
        int capacity = width * height - 2;
        LevelDefinition level = new RandomTankLevelSource(() -> source, capacity, new Random(1)).load();
        assertEquals(capacity, level.getBotStarts().size());
        assertFalse(level.getBotStarts().contains(new Position(width == 1 ? 0 : 1, height == 1 ? 0 : 1)));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new RandomTankLevelSource(() -> source, capacity + 1, new Random(1)).load());
        assertTrue(error.getMessage().contains("--bots"));
    }

    @Test void enclosedPlayerInFileIsPreservedWithoutInventingAnExit() throws IOException {
        LevelDefinition source = new LevelDefinition(3, 2, new Position(0, 0),
                Arrays.asList(new Position(1, 0), new Position(0, 1)));
        LevelDefinition level = new RandomTankLevelSource(() -> source, 3, new Random(42)).load();
        assertEquals(3, level.getBotStarts().size());
        assertEquals(source.getTreePositions(), level.getTreePositions());
    }

    @Test void propagatesSourceFailuresAndRejectsInvalidDependencies() {
        IOException failure = new IOException("unreadable");
        assertSame(failure, assertThrows(IOException.class,
                () -> new RandomTankLevelSource(() -> { throw failure; }, 3, new Random()).load()));
        LevelSource source = () -> null;
        assertThrows(IllegalArgumentException.class, () -> new RandomTankLevelSource(source, -1, new Random()));
        assertThrows(NullPointerException.class, () -> new RandomTankLevelSource(null, 0, new Random()));
        assertThrows(NullPointerException.class, () -> new RandomTankLevelSource(source, 0, null));
        assertThrows(NullPointerException.class, () -> new RandomTankLevelSource(source, 0, new Random()).load());
    }

    @Test void botStartsAreValidatedAndDefensivelyCopied() {
        Position player = new Position(0, 0), tree = new Position(1, 0), bot = new Position(2, 0);
        List<Position> bots = new ArrayList<>(Collections.singletonList(bot));
        LevelDefinition level = new LevelDefinition(3, 2, player, Collections.singletonList(tree), bots);
        bots.clear();
        assertEquals(Collections.singletonList(bot), level.getBotStarts());
        assertThrows(UnsupportedOperationException.class, () -> level.getBotStarts().clear());
        for (List<Position> invalid : Arrays.asList(Collections.singletonList(player), Collections.singletonList(tree),
                Arrays.asList(bot, bot), Collections.singletonList(new Position(3, 0)), Collections.singletonList(new Position(0, -1)))) {
            assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(3, 2, player, Collections.singletonList(tree), invalid));
        }
        assertThrows(NullPointerException.class, () -> new LevelDefinition(3, 2, player, Collections.emptyList(), null));
        assertThrows(NullPointerException.class, () -> new LevelDefinition(3, 2, player, Collections.emptyList(), Collections.singletonList(null)));
    }
}
