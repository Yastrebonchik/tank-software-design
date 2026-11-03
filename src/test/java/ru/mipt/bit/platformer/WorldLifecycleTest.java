package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldLifecycleTest {
    private final GameWorld world = new GameWorld(new GameField(5, 5));

    @Test void initialAndRuntimeObjectsUseTheSameEventsAfterRegistrationAndRemoval() {
        List<String> events = new ArrayList<>();
        WorldListener observer = new WorldListener() {
            public void objectAdded(CellOccupant object) {
                assertTrue(world.containsObject(object));
                assertTrue(world.getField().contains(object));
                events.add("add");
            }
            public void objectRemoved(CellOccupant object) {
                assertFalse(world.containsObject(object));
                assertFalse(world.getField().contains(object));
                events.add("remove");
            }
        };
        world.addListener(observer); world.addListener(observer);
        Tank tank = new Tank(0, 0, 0.4f);
        world.addObject(tank); world.addObject(tank);
        assertTrue(tank.tryShoot(world));
        Bullet bullet = (Bullet) world.getObjects().get(1);
        world.removeObject(bullet); world.removeObject(bullet);
        world.removeObject(tank); world.removeObject(tank);
        assertEquals(Arrays.asList("add", "add", "remove", "remove"), events);
        world.removeListener(observer);
        world.addObject(new Tree(2, 2));
        assertEquals(4, events.size());
    }

    @Test void removalDuringUpdateImmediatelySkipsTheRemovedObject() {
        List<String> calls = new ArrayList<>();
        UpdatingObject victim = new UpdatingObject(() -> calls.add("victim"));
        UpdatingObject remover = new UpdatingObject(() -> { calls.add("remover"); world.removeObject(victim); });
        world.addObject(remover); world.addObject(victim);
        world.update(0.1f);
        assertEquals(Collections.singletonList("remover"), calls);
        assertFalse(world.containsObject(victim));
    }

    @Test void additionsDuringUpdateAreRegisteredImmediatelyButUpdatedNextFrame() {
        List<String> calls = new ArrayList<>();
        UpdatingObject child = new UpdatingObject(() -> calls.add("child"));
        UpdatingObject producer = new UpdatingObject(() -> { calls.add("producer"); world.addObject(child); });
        world.addObject(producer);
        world.update(0f);
        assertEquals(Collections.singletonList("producer"), calls);
        assertTrue(world.containsObject(child));
        world.update(0f);
        assertEquals(Arrays.asList("producer", "producer", "child"), calls);
    }

    @Test void destroyedObjectsAreRemovedBeforeTheirNextUpdate() {
        class Fragile implements CellOccupant, Updatable, Damageable {
            boolean alive = true;
            int updates;
            public boolean tileOccupied(int x, int y) { return alive && x == 1 && y == 1; }
            public void update(float delta) { updates++; alive = false; }
            public void takeDamage(int damage) { alive = false; }
            public boolean isAlive() { return alive; }
        }
        Fragile duringUpdate = new Fragile(), beforeUpdate = new Fragile();
        world.addObject(duringUpdate); world.addObject(beforeUpdate);
        beforeUpdate.takeDamage(1);
        world.update(1f); world.update(1f);
        assertEquals(1, duringUpdate.updates); assertEquals(0, beforeUpdate.updates);
        assertTrue(world.getObjects().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> world.addObject(duringUpdate));
    }

    @Test void observersCanUnsubscribeDuringNotification() {
        List<String> calls = new ArrayList<>();
        world.addListener(new WorldListener() {
            public void objectAdded(CellOccupant object) { calls.add("once"); world.removeListener(this); }
            public void objectRemoved(CellOccupant object) { fail("Unsubscribed listener called"); }
        });
        world.addListener(new WorldListener() {
            public void objectAdded(CellOccupant object) { calls.add("always"); }
            public void objectRemoved(CellOccupant object) { calls.add("removed"); }
        });
        Tree first = new Tree(1, 1);
        world.addObject(first); world.addObject(new Tree(2, 2)); world.removeObject(first);
        assertEquals(Arrays.asList("once", "always", "always", "removed"), calls);
    }

    @Test void collectionQueriesAreSnapshotsAndCannotMutateRegistration() {
        Tree tree = new Tree(1, 1);
        world.addObject(tree);
        List<CellOccupant> objects = world.getObjects(), occupants = world.getField().occupantsAt(1, 1);
        assertThrows(UnsupportedOperationException.class, objects::clear);
        assertThrows(UnsupportedOperationException.class, occupants::clear);
        world.removeObject(tree);
        assertEquals(Collections.singletonList(tree), objects);
        assertEquals(Collections.singletonList(tree), occupants);
        assertTrue(world.getObjects().isEmpty());
        assertTrue(world.getField().occupantsAt(1, 1).isEmpty());
    }

    @Test void fieldUsesIdentityAndReportsAllCollidersIncludingNonBlockingObjects() {
        Tree tree = new Tree(1, 1);
        Bullet bullet = new Bullet(1, 1, Direction.RIGHT, 5f, 25, tree, world);
        GameField field = world.getField();
        field.addOccupant(tree); field.addOccupant(tree); field.addOccupant(bullet);
        assertEquals(Arrays.asList(tree, bullet), field.occupantsAt(1, 1));
        assertTrue(field.tileOccupied(1, 1));
        field.removeOccupant(tree); field.removeOccupant(tree);
        assertFalse(field.contains(tree));
        assertTrue(field.contains(bullet));
        assertFalse(field.tileOccupied(1, 1));
    }

    @Test void rejectsMissingObjectOrObserver() {
        assertThrows(NullPointerException.class, () -> world.addObject(null));
        assertThrows(NullPointerException.class, () -> world.addListener(null));
    }

    private static final class UpdatingObject implements CellOccupant, Updatable {
        private final Runnable action;
        UpdatingObject(Runnable action) { this.action = action; }
        public boolean tileOccupied(int x, int y) { return false; }
        public void update(float delta) { action.run(); }
    }
}
