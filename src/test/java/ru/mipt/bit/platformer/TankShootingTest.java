package ru.mipt.bit.platformer;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TankShootingTest {
    private final GameWorld world = new GameWorld(new GameField(10, 8));
    private final Tank tank = new Tank(4, 4, 0.4f);

    TankShootingTest() { world.addObject(tank); }

    @ParameterizedTest @EnumSource(Direction.class)
    void shotStartsInTheAdjacentCellAndUsesTheFacingDirection(Direction direction) {
        assertTrue(tank.tryMove(direction, world.getField()));
        world.update(0.4f);
        assertTrue(tank.tryShoot(world));
        Bullet bullet = bullets().get(0);
        assertEquals(tank.getX() + direction.getDeltaX(), bullet.getX());
        assertEquals(tank.getY() + direction.getDeltaY(), bullet.getY());
        assertEquals(direction, bullet.getDirection());
        assertEquals(25, bullet.getDamage());
        assertEquals(100, tank.getHealth());
    }

    @Test void movingTankCannotShootOrStartReloading() {
        tank.tryMove(Direction.RIGHT, world.getField());
        assertFalse(tank.tryShoot(world));
        assertEquals(0f, tank.getReloadRemaining());
        world.update(0.2f);
        assertFalse(tank.tryShoot(world));
        world.update(0.2f);
        assertTrue(tank.tryShoot(world));
        assertEquals(1, bullets().size());
    }

    @Test void reusableShootCommandRespectsReloadAndDoesNotAffectOtherTanks() {
        Tank other = new Tank(1, 1, 0.4f);
        world.addObject(other);
        ShootTankCommand command = new ShootTankCommand(tank, world);
        command.execute(); command.execute();
        assertEquals(1, bullets().size());
        assertEquals(0.5f, tank.getReloadRemaining());
        assertEquals(0f, other.getReloadRemaining());
        tank.update(0.2f); command.execute();
        assertEquals(1, bullets().size());
        world.update(0.3f); command.execute();
        assertEquals(2, bullets().size());
        tank.update(10f);
        assertEquals(0f, tank.getReloadRemaining());
    }

    @Test void adjacentTargetTakesDamageImmediatelyWithoutWaitingForAnUpdate() {
        Tank target = new Tank(5, 4, 0.4f);
        world.addObject(target);
        assertTrue(tank.tryShoot(world));
        assertEquals(75, target.getHealth());
        assertTrue(target.isAlive());
        assertTrue(bullets().isEmpty());
        assertTrue(world.containsObject(target));
        assertEquals(100, tank.getHealth());
    }

    @ParameterizedTest @ValueSource(ints = {100, 150, Integer.MAX_VALUE})
    void lethalDamageAtOrBelowZeroRemovesTheTankAndFreesBothMovingCells(int damage) {
        Tank shooter = new Tank(0, 1, 0.4f, new CombatSettings(100, damage, 0.5f, 5f));
        Tank victim = new Tank(1, 1, 0.4f);
        world.addObject(shooter); world.addObject(victim);
        assertTrue(victim.tryMove(Direction.RIGHT, world.getField()));
        shooter.tryShoot(world);
        assertEquals(0, victim.getHealth());
        assertFalse(victim.isAlive());
        assertFalse(world.containsObject(victim));
        assertFalse(world.getField().tileOccupied(1, 1));
        assertFalse(world.getField().tileOccupied(2, 1));
        assertFalse(victim.tryMove(Direction.UP, world.getField()));
        assertFalse(victim.tryShoot(world));
        float progress = victim.getMovementProgress();
        victim.update(1f);
        assertEquals(progress, victim.getMovementProgress());
    }

    @Test void fourDefaultHitsKillTheTargetAndLaterShotsPassThroughTheFreedCell() {
        Tank target = new Tank(5, 4, 0.4f);
        world.addObject(target);
        for (int shot = 1; shot <= 4; shot++) {
            assertTrue(tank.tryShoot(world));
            assertEquals(100 - 25 * shot, target.getHealth());
            world.update(0.5f);
        }
        assertFalse(world.containsObject(target));
        assertTrue(tank.tryShoot(world));
        assertEquals(1, bullets().size());
    }

    @Test void customWeaponChangesHealthDamageReloadAndBulletSpeed() {
        Tank custom = new Tank(0, 0, 0.4f, new CombatSettings(60, 12, 1.25f, 2f));
        world.addObject(custom);
        custom.tryShoot(world);
        Bullet bullet = bullets().get(0);
        assertEquals(60, custom.getHealth());
        assertEquals(12, bullet.getDamage());
        world.update(0.25f);
        assertEquals(1.5f, bullet.getDisplayX());
        assertEquals(1f, custom.getReloadRemaining());
    }

    @Test void detachedOrDestroyedTankCannotExecuteStaleCommands() {
        world.removeObject(tank);
        new ShootTankCommand(tank, world).execute();
        new MoveTankCommand(tank, world.getField(), Direction.UP).execute();
        assertTrue(bullets().isEmpty());
        assertFalse(tank.isMoving());
        world.addObject(tank);
        tank.takeDamage(100);
        assertFalse(tank.tryShoot(world));
        assertFalse(tank.tryMove(Direction.UP, world.getField()));
    }

    @Test void invalidDamageAndMissingDependenciesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> tank.takeDamage(0));
        assertThrows(IllegalArgumentException.class, () -> tank.takeDamage(-1));
        assertEquals(100, tank.getHealth());
        assertThrows(NullPointerException.class, () -> new Tank(0, 0, 1f, null));
        assertThrows(NullPointerException.class, () -> tank.tryShoot(null));
        assertThrows(NullPointerException.class, () -> new ShootTankCommand(null, world));
        assertThrows(NullPointerException.class, () -> new ShootTankCommand(tank, null));
    }

    private List<Bullet> bullets() {
        return world.getObjects().stream().filter(Bullet.class::isInstance).map(Bullet.class::cast).collect(Collectors.toList());
    }
}
