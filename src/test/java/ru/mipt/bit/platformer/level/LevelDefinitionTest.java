package ru.mipt.bit.platformer.level;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ru.mipt.bit.platformer.level.LevelDefinition.Position;
import static org.junit.jupiter.api.Assertions.*;

class LevelDefinitionTest {
    @Test void ownsAnImmutableCopyOfTheInitialPositions() {
        List<Position> trees = new ArrayList<>(Collections.singletonList(new Position(2, 1)));
        LevelDefinition level = new LevelDefinition(3, 2, new Position(0, 0), trees);
        trees.clear();
        assertEquals(3, level.getWidth());
        assertEquals(2, level.getHeight());
        assertEquals(new Position(0, 0), level.getPlayerStart());
        assertEquals(Collections.singletonList(new Position(2, 1)), level.getTreePositions());
        assertThrows(UnsupportedOperationException.class, () -> level.getTreePositions().clear());
        assertEquals(new Position(2, 1).hashCode(), level.getTreePositions().get(0).hashCode());
    }

    @ParameterizedTest @CsvSource({"0,2", "2,0", "-1,2", "2,-1", "2147483647,2"})
    void rejectsInvalidDimensions(int width, int height) {
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(width, height, new Position(0, 0), Collections.emptyList()));
    }

    @ParameterizedTest @CsvSource({"-1,0", "0,-1", "3,0", "0,2"})
    void rejectsPositionsOutsideTheLevel(int x, int y) {
        Position outside = new Position(x, y);
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(3, 2, outside, Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(3, 2, new Position(0, 0), Collections.singletonList(outside)));
    }

    @Test void rejectsOverlappingTreesAndPlayer() {
        Position start = new Position(0, 0);
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(3, 2, start, Collections.singletonList(start)));
        assertThrows(IllegalArgumentException.class, () -> new LevelDefinition(3, 2, start, Arrays.asList(new Position(2, 1), new Position(2, 1))));
    }

    @Test void acceptsASingleCellFileLayout() {
        assertEquals(1, new LevelDefinition(1, 1, new Position(0, 0), Collections.emptyList()).getWidth());
    }

    @Test void rejectsMissingPositions() {
        assertThrows(NullPointerException.class, () -> new LevelDefinition(2, 2, null, Collections.emptyList()));
        assertThrows(NullPointerException.class, () -> new LevelDefinition(2, 2, new Position(0, 0), null));
        assertThrows(NullPointerException.class, () -> new LevelDefinition(2, 2, new Position(0, 0), Collections.singletonList(null)));
    }
}
