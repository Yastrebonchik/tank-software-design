package ru.mipt.bit.platformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mipt.bit.platformer.level.LevelDefinition;
import static org.junit.jupiter.api.Assertions.*;

class MultiTankSimulationTest {
    @ParameterizedTest @ValueSource(longs = {1, 42, 2026})
    void manyFramesNeverOverlapTanksTreesOrBoundaries(long seed) throws IOException {
        LevelDefinition level = LaunchOptions.parse("--trees", "25", "--bots", "20", "--seed", Long.toString(seed))
                .createLevelSource().load();
        GameField field = new GameField(level.getWidth(), level.getHeight());
        GameWorld world = new GameWorld(field);
        List<Tank> tanks = new ArrayList<>();
        List<InputHandler> controls = new ArrayList<>();
        List<LevelDefinition.Position> starts = new ArrayList<>(level.getBotStarts());
        starts.add(level.getPlayerStart());
        for (LevelDefinition.Position position : starts) {
            Tank tank = new Tank(position.getX(), position.getY(), 0.4f);
            tanks.add(tank); world.addOccupant(tank); world.addUpdatable(tank);
            controls.add(new RandomTankController(tank, field, new Random(seed + tanks.size())));
        }
        for (LevelDefinition.Position tree : level.getTreePositions()) { world.addOccupant(new Tree(tree.getX(), tree.getY())); }
        CompositeInputHandler input = new CompositeInputHandler(controls);
        boolean moved = false;
        for (int frame = 0; frame < 1000; frame++) {
            input.processInput();
            verifyNoOverlap(field, tanks, level);
            moved |= tanks.stream().anyMatch(Tank::isMoving);
            world.update(frame % 7 == 0 ? 0.5f : 0.1f);
            verifyNoOverlap(field, tanks, level);
        }
        assertTrue(moved);
    }

    private void verifyNoOverlap(GameField field, List<Tank> tanks, LevelDefinition level) {
        for (Tank tank : tanks) {
            assertTrue(field.isInside(tank.getX(), tank.getY()));
            assertTrue(field.isInside(tank.getMovementStartX(), tank.getMovementStartY()));
        }
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                int occupied = level.getTreePositions().contains(new LevelDefinition.Position(x, y)) ? 1 : 0;
                for (Tank tank : tanks) { if (tank.tileOccupied(x, y)) { occupied++; } }
                assertTrue(occupied <= 1, "Overlap at " + x + "," + y);
                assertEquals(occupied == 1, field.tileOccupied(x, y));
            }
        }
    }
}
