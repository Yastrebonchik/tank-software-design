package ru.mipt.bit.platformer.level;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import ru.mipt.bit.platformer.Direction;
import ru.mipt.bit.platformer.level.LevelDefinition.Position;
import static org.junit.jupiter.api.Assertions.*;

class RandomLevelSourceTest {
    @ParameterizedTest @CsvSource({"1,2,0", "2,1,0", "2,2,2", "10,8,0", "10,8,12", "10,8,78", "1,10,8", "10,1,8"})
    void generatesUniqueObjectsInsideTheLevelAndAllowsTheFirstMove(int width, int height, int count) {
        for (int seed = 0; seed < 50; seed++) {
            LevelDefinition level = new RandomLevelSource(width, height, count, new Random(seed)).load();
            Set<Position> trees = new HashSet<>(level.getTreePositions());
            Position start = level.getPlayerStart();
            assertEquals(count, level.getTreePositions().size());
            assertEquals(count, trees.size());
            assertFalse(trees.contains(start));
            assertTrue(inside(start, width, height));
            for (Position tree : trees) { assertTrue(inside(tree, width, height)); }
            boolean canMove = false;
            for (Direction direction : Direction.values()) {
                Position next = new Position(start.getX() + direction.getDeltaX(), start.getY() + direction.getDeltaY());
                canMove |= inside(next, width, height) && !trees.contains(next);
            }
            assertTrue(canMove, "seed " + seed);
        }
    }

    @Test void equalSeedsAndParametersReproduceTheWholeLayout() {
        LevelDefinition first = new RandomLevelSource(10, 8, 12, new Random(42)).load();
        LevelDefinition second = new RandomLevelSource(10, 8, 12, new Random(42)).load();
        assertEquals(first.getPlayerStart(), second.getPlayerStart());
        assertEquals(first.getTreePositions(), second.getTreePositions());
    }

    @ParameterizedTest @CsvSource({"0,2,0", "2,-1,0", "1,1,0", "2,2,-1", "2,2,3", "2147483647,2,0"})
    void rejectsImpossibleParameters(int width, int height, int count) {
        assertThrows(IllegalArgumentException.class, () -> new RandomLevelSource(width, height, count, new Random(42)));
    }

    private boolean inside(Position p, int width, int height) {
        return p.getX() >= 0 && p.getX() < width && p.getY() >= 0 && p.getY() < height;
    }
}
