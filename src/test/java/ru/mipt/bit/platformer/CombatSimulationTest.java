package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mipt.bit.platformer.level.LevelDefinition;
import static org.junit.jupiter.api.Assertions.*;

class CombatSimulationTest {
    @ParameterizedTest @ValueSource(longs = {1, 42, 2026})
    void commandsCollisionsUpdatesAndObserverStayConsistentDuringABattle(long seed) throws Exception {
        LevelDefinition level = LaunchOptions.parse("--trees", "12", "--bots", "15", "--seed", Long.toString(seed))
                .createLevelSource().load();
        GameWorld world = new GameWorld(new GameField(level.getWidth(), level.getHeight()));
        Set<CellOccupant> observed = Collections.newSetFromMap(new IdentityHashMap<>());
        int[] shots = {0}, deaths = {0};
        world.addListener(new WorldListener() {
            public void objectAdded(CellOccupant object) {
                assertTrue(observed.add(object));
                if (object instanceof Bullet) { shots[0]++; }
            }
            public void objectRemoved(CellOccupant object) {
                assertTrue(observed.remove(object));
                if (object instanceof Tank) { deaths[0]++; }
            }
        });
        CommandQueue commands = new CommandQueue();
        List<InputHandler> controllers = new ArrayList<>();
        List<Tank> tanks = new ArrayList<>();
        List<LevelDefinition.Position> starts = new ArrayList<>(level.getBotStarts());
        starts.add(level.getPlayerStart());
        for (LevelDefinition.Position start : starts) {
            Tank tank = new Tank(start.getX(), start.getY(), 0.4f);
            world.addObject(tank); tanks.add(tank);
            List<GameCommand> actions = new ArrayList<>();
            for (Direction direction : Direction.values()) { actions.add(new MoveTankCommand(tank, world.getField(), direction)); }
            actions.add(new ShootTankCommand(tank, world));
            controllers.add(new RandomTankController(tank, actions, new Random(seed + tanks.size()), commands));
        }
        for (LevelDefinition.Position tree : level.getTreePositions()) { world.addObject(new Tree(tree.getX(), tree.getY())); }
        InputHandler input = new CompositeInputHandler(controllers);
        for (int frame = 0; frame < 1500; frame++) {
            input.processInput(); commands.executePending();
            world.update(frame % 7 == 0 ? 0.5f : 0.1f);
            assertEquals(observed.size(), world.getObjects().size());
            assertTrue(observed.containsAll(world.getObjects()));
            for (Tank tank : tanks) {
                assertEquals(tank.isAlive(), world.containsObject(tank));
                assertEquals(tank.isAlive(), world.getField().contains(tank));
                assertTrue(tank.getHealth() >= 0 && tank.getHealth() <= 100);
            }
            for (CellOccupant object : world.getObjects()) {
                if (object instanceof Bullet) {
                    Bullet bullet = (Bullet) object;
                    assertTrue(bullet.isAlive());
                    assertTrue(world.getField().isInside(bullet.getX(), bullet.getY()));
                }
            }
            for (int y = 0; y < level.getHeight(); y++) {
                for (int x = 0; x < level.getWidth(); x++) {
                    long blockers = world.getField().occupantsAt(x, y).stream().filter(CellOccupant::blocksMovement).count();
                    assertTrue(blockers <= 1, "Overlapping tanks or trees");
                    assertEquals(blockers == 1, world.getField().tileOccupied(x, y));
                }
            }
        }
        assertTrue(shots[0] > 0);
        assertTrue(deaths[0] > 0);
    }
}
