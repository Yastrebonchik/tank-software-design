package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class BulletTest {
    private final GameWorld world = new GameWorld(new GameField(10, 8));
    private final Tank owner = new Tank(0, 0, 0.4f);

    BulletTest() { world.addObject(owner); }

    private Bullet bullet(int x, int y, Direction direction) {
        Bullet bullet = new Bullet(x, y, direction, 5f, 25, owner, world);
        world.addObject(bullet);
        return bullet;
    }

    @ParameterizedTest @EnumSource(Direction.class)
    void movesSmoothlyInEveryDirectionAndTracksItsCurrentCell(Direction direction) {
        Bullet bullet = bullet(4, 4, direction);
        world.update(0.1f);
        assertEquals(4 + direction.getDeltaX() * 0.5f, bullet.getDisplayX(), 0.00001f);
        assertEquals(4 + direction.getDeltaY() * 0.5f, bullet.getDisplayY(), 0.00001f);
        assertTrue(bullet.tileOccupied(4, 4));
        assertFalse(bullet.tileOccupied(0, 0));
        world.update(0.1f);
        assertEquals(4 + direction.getDeltaX(), bullet.getX());
        assertEquals(4 + direction.getDeltaY(), bullet.getY());
        assertFalse(bullet.tileOccupied(4, 4));
    }

    @Test void largeTimeStepCannotSkipTheNearestTargetOrHitMultipleTargets() {
        Tank first = new Tank(4, 2, 0.4f), second = new Tank(7, 2, 0.4f);
        world.addObject(first); world.addObject(second);
        Bullet bullet = bullet(1, 2, Direction.RIGHT);
        world.update(1000f);
        assertEquals(75, first.getHealth());
        assertEquals(100, second.getHealth());
        assertFalse(world.containsObject(bullet));
        assertFalse(bullet.isAlive());
        assertFalse(bullet.tileOccupied(4, 2));
        bullet.update(100f);
        assertEquals(75, first.getHealth());
    }

    @Test void treeAbsorbsTheProjectileAndShieldsTheTankBehindIt() {
        Tree tree = new Tree(3, 2);
        Tank target = new Tank(4, 2, 0.4f);
        world.addObject(tree); world.addObject(target);
        Bullet bullet = bullet(1, 2, Direction.RIGHT);
        world.update(10f);
        assertFalse(world.containsObject(bullet));
        assertTrue(world.containsObject(tree));
        assertEquals(100, target.getHealth());
    }

    @Test void adjacentTreeAbsorbsTheShotAtCreation() {
        Tree tree = new Tree(1, 0);
        world.addObject(tree);
        assertTrue(owner.tryShoot(world));
        assertEquals(2, world.getObjects().size());
        assertTrue(world.containsObject(tree));
    }

    @ParameterizedTest @EnumSource(Direction.class)
    void projectilesDisappearAtEveryMapBoundary(Direction direction) {
        Bullet bullet = bullet(direction == Direction.LEFT ? 0 : direction == Direction.RIGHT ? 9 : 4,
                direction == Direction.DOWN ? 0 : direction == Direction.UP ? 7 : 4, direction);
        world.update(0.2f);
        assertFalse(world.containsObject(bullet));
        assertFalse(bullet.isAlive());
    }

    @ParameterizedTest @EnumSource(Direction.class)
    void shootingOutOfBoundsLeavesNoProjectile(Direction direction) {
        Tank edge = new Tank(direction == Direction.LEFT ? 0 : direction == Direction.RIGHT ? 9 : 4,
                direction == Direction.DOWN ? 0 : direction == Direction.UP ? 7 : 4, 0.4f);
        world.addObject(edge);
        assertFalse(edge.tryMove(direction, world.getField()));
        assertTrue(edge.tryShoot(world));
        assertEquals(2, world.getObjects().size());
        assertEquals(100, edge.getHealth());
    }

    @Test void opposingBulletsDestroyEachOtherWithoutSkippingDuringALongFrame() {
        Bullet right = bullet(2, 2, Direction.RIGHT);
        Bullet left = bullet(6, 2, Direction.LEFT);
        world.update(2f);
        assertFalse(right.isAlive()); assertFalse(left.isAlive());
        assertFalse(world.containsObject(right)); assertFalse(world.containsObject(left));
        assertEquals(1, world.getObjects().size());
    }

    @Test void newlyCreatedShotCanHitAnotherBulletImmediately() {
        Bullet incoming = bullet(1, 0, Direction.LEFT);
        owner.tryShoot(world);
        assertFalse(incoming.isAlive());
        assertEquals(1, world.getObjects().size());
        assertEquals(100, owner.getHealth());
    }

    @Test void ownerIsImmuneToItsOwnProjectile() {
        Bullet bullet = bullet(0, 0, Direction.RIGHT);
        world.update(0f);
        assertEquals(100, owner.getHealth());
        assertTrue(bullet.isAlive());
        world.update(0.2f);
        assertEquals(1, bullet.getX());
    }

    @Test void aBulletDoesNotBlockMovementButDamagesATankEnteringItsCell() {
        Tank target = new Tank(2, 2, 0.4f);
        world.addObject(target);
        Bullet bullet = bullet(3, 2, Direction.RIGHT);
        assertFalse(world.getField().tileOccupied(3, 2));
        assertTrue(world.getField().occupantsAt(3, 2).contains(bullet));
        assertTrue(target.tryMove(Direction.RIGHT, world.getField()));
        world.update(0f);
        assertEquals(75, target.getHealth());
        assertFalse(bullet.isAlive());
    }

    @Test void bothCellsOfAMovingTankCanBeHit() {
        Tank target = new Tank(3, 2, 0.4f);
        world.addObject(target);
        target.tryMove(Direction.RIGHT, world.getField());
        bullet(3, 2, Direction.UP);
        bullet(4, 2, Direction.UP);
        world.update(0f);
        assertEquals(50, target.getHealth());
    }

    @Test void collisionSupportsNewDamageableTypesWithoutConcreteTankChecks() {
        class Crate implements CellOccupant, Damageable {
            int hp = 20;
            public boolean tileOccupied(int x, int y) { return x == 2 && y == 2; }
            public void takeDamage(int damage) { hp -= damage; }
            public boolean isAlive() { return hp > 0; }
        }
        Crate crate = new Crate(); world.addObject(crate);
        Bullet bullet = bullet(1, 2, Direction.RIGHT);
        world.update(1f);
        assertEquals(-5, crate.hp);
        assertFalse(world.containsObject(crate));
        assertFalse(world.containsObject(bullet));
    }

    @Test void unregisteredAndRemovedProjectilesCannotMoveOrCauseDamage() {
        Bullet bullet = new Bullet(1, 0, Direction.LEFT, 5f, 25, owner, world);
        bullet.update(1f);
        assertEquals(1, bullet.getX());
        world.addObject(bullet); world.removeObject(bullet);
        bullet.update(1f);
        assertEquals(1, bullet.getX());
        assertEquals(100, owner.getHealth());
    }

    @ParameterizedTest @ValueSource(floats = {-1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void rejectsInvalidTimeWithoutChangingState(float delta) {
        Bullet bullet = bullet(2, 2, Direction.RIGHT);
        assertThrows(IllegalArgumentException.class, () -> bullet.update(delta));
        assertEquals(2f, bullet.getDisplayX());
    }

    @Test void validatesConstructionAndDamage() {
        for (float speed : new float[] {0f, -1f, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Bullet(1, 1, Direction.UP, speed, 25, owner, world));
        }
        assertThrows(IllegalArgumentException.class, () -> new Bullet(1, 1, Direction.UP, 1f, 0, owner, world));
        assertThrows(NullPointerException.class, () -> new Bullet(1, 1, null, 1f, 25, owner, world));
        assertThrows(NullPointerException.class, () -> new Bullet(1, 1, Direction.UP, 1f, 25, null, world));
        assertThrows(NullPointerException.class, () -> new Bullet(1, 1, Direction.UP, 1f, 25, owner, null));
        Bullet bullet = bullet(2, 2, Direction.UP);
        assertThrows(IllegalArgumentException.class, () -> bullet.takeDamage(0));
        bullet.takeDamage(1);
        world.update(0f);
        assertFalse(world.containsObject(bullet));
    }
}
