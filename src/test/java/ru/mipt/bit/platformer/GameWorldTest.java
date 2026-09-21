package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest {
    @Test void defaultSceneRegistersInitialObjects() {
        GameWorld world = GameWorld.createDefault(10, 8);
        assertEquals(1, world.getTanks().size()); assertEquals(1, world.getTrees().size());
        assertSame(world.getPlayer(), world.getTanks().get(0));
        assertTrue(world.getPlayer().tileOccupied(1, 1));
        assertTrue(world.getField().tileOccupied(1, 1));
        assertTrue(world.getField().tileOccupied(1, 3));
        assertFalse(world.getField().isInside(10, 0));
    }

    @Test void updatesEveryTankOnceAndRegistersAddedObjects() {
        GameWorld world = GameWorld.createDefault(10, 8);
        Tank second = new Tank(5, 5);
        world.addTank(second); world.addTank(second);
        Tree tree = new Tree(7, 7); world.addTree(tree); world.addTree(tree);
        assertEquals(2, world.getTanks().size()); assertEquals(2, world.getTrees().size());
        assertTrue(world.getField().tileOccupied(7, 7));
        assertTrue(world.getPlayer().tryMove(Tank.MoveDirection.RIGHT, world.getField()));
        assertTrue(second.tryMove(Tank.MoveDirection.UP, world.getField()));
        world.update(0.2f);
        assertEquals(0.5f, world.getPlayer().getMovementProgress());
        assertEquals(0.5f, second.getMovementProgress());
    }

    @Test void collectionsCannotBeChangedExternally() {
        GameWorld world = GameWorld.createDefault(10, 8);
        assertThrows(UnsupportedOperationException.class, () -> world.getTanks().clear());
        assertThrows(UnsupportedOperationException.class, () -> world.getTrees().clear());
    }

    @Test void rejectsNullObjects() {
        GameWorld world = GameWorld.createDefault(10, 8);
        assertThrows(NullPointerException.class, () -> world.addTank(null));
        assertThrows(NullPointerException.class, () -> world.addTree(null));
    }
}
