package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomTankShootingTest {
    @Test void canChooseShootingAsItsFifthActionAndOnlyQueuesItDuringInput() {
        GameWorld world = new GameWorld(new GameField(8, 4));
        Tank bot = new Tank(1, 1, 0.4f);
        world.addObject(bot);
        List<GameCommand> actions = new ArrayList<>();
        for (Direction direction : Direction.values()) { actions.add(new MoveTankCommand(bot, world.getField(), direction)); }
        actions.add(new ShootTankCommand(bot, world));
        CommandQueue commands = new CommandQueue();
        Random chooseShooting = new Random() {
            @Override public int nextInt(int bound) { assertEquals(5, bound); return 4; }
        };
        RandomTankController controller = new RandomTankController(bot, actions, chooseShooting, commands);
        actions.clear(); // The controller owns its action list.
        controller.processInput();
        assertEquals(1, world.getObjects().size());
        commands.executePending();
        assertEquals(2, world.getObjects().size());
        assertFalse(bot.isMoving());
        controller.processInput(); commands.executePending();
        assertEquals(2, world.getObjects().size()); // Reload prevents another shot.
    }

    @Test void deadTankDoesNotSelectOrSubmitCommands() {
        Tank tank = new Tank(1, 1, 0.4f);
        tank.takeDamage(100);
        CommandQueue queue = new CommandQueue();
        Random random = new Random() {
            @Override public int nextInt(int bound) { fail("Dead bot must not choose an action"); return 0; }
        };
        new RandomTankController(tank, Collections.singletonList(() -> fail("Dead bot acted")), random, queue).processInput();
        queue.executePending();
    }

    @Test void commandsQueuedBeforeTheReceiverDiesHaveNoEffect() {
        GameWorld world = new GameWorld(new GameField(3, 1));
        Tank player = new Tank(0, 0, 0.4f, new CombatSettings(100, 100, 0.5f, 5f));
        Tank victim = new Tank(1, 0, 0.4f);
        world.addObject(player); world.addObject(victim);
        CommandQueue queue = new CommandQueue();
        queue.submit(new ShootTankCommand(player, world));
        queue.submit(new MoveTankCommand(victim, world.getField(), Direction.RIGHT));
        queue.submit(new ShootTankCommand(victim, world));
        queue.executePending();
        assertFalse(victim.isAlive()); assertFalse(victim.isMoving());
        assertEquals(1, world.getObjects().size());
        assertEquals(100, player.getHealth());
    }

    @Test void requiresValidNonEmptyActionsAndAQueue() {
        Tank tank = new Tank(0, 0, 1f);
        assertThrows(IllegalArgumentException.class, () -> new RandomTankController(tank, Collections.emptyList(), new Random(), new CommandQueue()));
        assertThrows(NullPointerException.class, () -> new RandomTankController(tank, Arrays.asList(() -> {}, null), new Random(), new CommandQueue()));
        assertThrows(NullPointerException.class, () -> new RandomTankController(tank, Collections.singletonList(() -> {}), new Random(), null));
    }
}
