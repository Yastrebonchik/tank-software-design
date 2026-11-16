package ru.mipt.bit.platformer;

import java.util.Collections;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static com.badlogic.gdx.Input.Keys.SPACE;
import static org.junit.jupiter.api.Assertions.*;

class TankStateTest {
    private final GameWorld world = new GameWorld(new GameField(12, 4));

    private Tank tank(int x, int health, float baseDuration) {
        Tank tank = new Tank(x, 1, baseDuration);
        if (health < tank.getMaxHealth()) { tank.takeDamage(tank.getMaxHealth() - health); }
        world.addObject(tank);
        return tank;
    }

    @ParameterizedTest @CsvSource({"100,1", "71,1", "70,2", "15,2", "14,3", "10,3"})
    void movementScalesTheConfiguredBaseDurationRatherThanUsingFixedSeconds(int health, float multiplier) {
        Tank tank = tank(2, health, 1.2f);
        assertTrue(tank.tryMove(Direction.RIGHT, world.getField()));
        tank.update(1.2f * multiplier / 2f);
        assertEquals(0.5f, tank.getMovementProgress(), 0.00001f);
        assertTrue(tank.isMoving());
        tank.update(1.2f * multiplier / 2f);
        assertFalse(tank.isMoving());
        assertEquals(3, tank.getX());
        assertEquals(3, tank.getMovementStartX());
    }

    @ParameterizedTest @CsvSource({"100,30,0.2,0.4", "100,90,0.2,0.6", "70,60,0.4,0.6"})
    void aHitChangesRemainingSpeedImmediatelyWithoutResettingProgress(int health, int damage, float firstHalf, float secondHalf) {
        Tank tank = tank(2, health, 0.4f);
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(firstHalf);
        assertEquals(0.5f, tank.getMovementProgress(), 0.00001f);
        tank.takeDamage(damage);
        assertEquals(0.5f, tank.getMovementProgress(), 0.00001f);
        assertEquals(2, tank.getMovementStartX());
        assertEquals(3, tank.getX());
        tank.update(secondHalf / 2);
        assertEquals(0.75f, tank.getMovementProgress(), 0.00001f);
        tank.update(secondHalf / 2);
        assertFalse(tank.isMoving());
    }

    @Test void slowingDownKeepsBothCellsOccupiedUntilActualCompletion() {
        Tank moving = tank(2, 100, 0.4f);
        Tank follower = tank(1, 100, 0.4f);
        Tank opponent = tank(4, 100, 0.4f);
        moving.tryMove(Direction.RIGHT, world.getField());
        moving.update(0.2f);
        moving.takeDamage(90);
        moving.update(0.2f); // The old normal-speed completion time has passed.
        assertTrue(world.getField().tileOccupied(2, 1));
        assertTrue(world.getField().tileOccupied(3, 1));
        assertFalse(follower.tryMove(Direction.RIGHT, world.getField()));
        assertFalse(opponent.tryMove(Direction.LEFT, world.getField()));
        assertFalse(moving.tryMove(Direction.UP, world.getField()));
        moving.update(0.4f);
        assertFalse(world.getField().tileOccupied(2, 1));
        assertTrue(follower.tryMove(Direction.RIGHT, world.getField()));
        assertFalse(opponent.tryMove(Direction.LEFT, world.getField()));
    }

    @Test void repeatedHitsWithinOneStateDoNotRestartMovementOrReload() {
        Tank tank = tank(2, 70, 0.4f);
        assertTrue(tank.tryShoot(world));
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(0.2f);
        tank.takeDamage(10);
        assertEquals(0.25f, tank.getMovementProgress(), 0.00001f);
        assertEquals(0.3f, tank.getReloadRemaining(), 0.00001f);
        tank.update(0.6f);
        assertFalse(tank.isMoving());
        assertEquals(0f, tank.getReloadRemaining());
    }

    @ParameterizedTest @ValueSource(ints = {100, 70, 15})
    void lightAndMediumCanShootOnlyStandingAndRespectConfiguredReload(int health) {
        Tank tank = new Tank(2, 1, 0.6f, new CombatSettings(100, 10, 1.2f, 3f));
        if (health < 100) { tank.takeDamage(100 - health); }
        world.addObject(tank);
        assertTrue(tank.tryShoot(world));
        assertFalse(tank.tryShoot(world));
        tank.update(0.6f);
        assertFalse(tank.tryShoot(world));
        tank.update(0.6f);
        assertTrue(tank.tryShoot(world));
        tank.tryMove(Direction.UP, world.getField());
        assertFalse(tank.tryShoot(world));
    }

    @Test void heavyRejectsShotsWithoutCreatingBulletOrReload() {
        Tank tank = tank(2, 10, 0.4f);
        assertFalse(tank.tryShoot(world));
        assertEquals(0f, tank.getReloadRemaining());
        assertEquals(Collections.singletonList(tank), world.getObjects());
        tank.update(10f);
        assertFalse(tank.tryShoot(world));
        assertTrue(tank.tryMove(Direction.UP, world.getField()));
    }

    @Test void heavyStateDoesNotCancelAnExistingBulletOrResetReload() {
        Tank tank = tank(2, 100, 0.4f);
        Tank target = tank(5, 100, 0.4f);
        assertTrue(tank.tryShoot(world));
        Bullet bullet = (Bullet) world.getObjects().stream().filter(Bullet.class::isInstance).findFirst().get();
        tank.update(0.1f);
        tank.takeDamage(90);
        assertEquals(0.4f, tank.getReloadRemaining(), 0.00001f);
        assertFalse(tank.tryShoot(world));
        world.update(0.4f);
        assertEquals(75, target.getHealth());
        assertFalse(world.containsObject(bullet));
        assertEquals(0f, tank.getReloadRemaining());
        assertFalse(tank.tryShoot(world));
        assertEquals(0f, tank.getReloadRemaining());
    }

    @Test void aStrongHitSkipsMediumAndDeathStillRemovesBothCells() {
        Tank tank = tank(2, 100, 0.4f);
        assertInstanceOf(LightTankState.class, tank.getState());
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(0.1f);
        tank.takeDamage(90);
        assertInstanceOf(HeavyTankState.class, tank.getState());
        tank.takeDamage(Integer.MAX_VALUE);
        world.update(0f);
        assertEquals(0, tank.getHealth());
        assertFalse(world.containsObject(tank));
        assertFalse(world.getField().tileOccupied(2, 1));
        assertFalse(world.getField().tileOccupied(3, 1));
        assertFalse(tank.tryShoot(world));
        assertFalse(tank.tryMove(Direction.UP, world.getField()));
    }

    @Test void stateUsesTheIndividualMaximumHealth() {
        Tank tank = new Tank(2, 1, 0.4f, new CombatSettings(200, 10, 0.5f, 5f));
        tank.takeDamage(60);
        assertInstanceOf(MediumTankState.class, tank.getState());
        tank.takeDamage(110);
        assertInstanceOf(MediumTankState.class, tank.getState());
        tank.takeDamage(1);
        assertInstanceOf(HeavyTankState.class, tank.getState());
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void keyboardAndAiCannotBypassHeavyStateEvenForAnAlreadyQueuedShot(boolean ai) {
        Tank tank = tank(2, 100, 0.4f);
        CommandQueue queue = new CommandQueue();
        ShootTankCommand shot = new ShootTankCommand(tank, world);
        InputHandler input;
        if (ai) {
            input = new RandomTankController(tank, Collections.singletonList(shot), new Random(42), queue);
        } else {
            KeyboardController keyboard = new KeyboardController(key -> key == SPACE, queue);
            keyboard.bind(shot, SPACE);
            input = keyboard;
        }
        input.processInput(); // Decision happened before the hit.
        tank.takeDamage(90);
        queue.executePending();
        input.processInput(); queue.executePending();
        assertEquals(Collections.singletonList(tank), world.getObjects());
        assertEquals(0f, tank.getReloadRemaining());
    }

    @Test void aiRespectsTheLongerStepAndCanMoveAgainAfterIt() {
        Tank tank = tank(2, 10, 0.4f);
        CommandQueue queue = new CommandQueue();
        RandomTankController ai = new RandomTankController(tank,
                Collections.singletonList(new MoveTankCommand(tank, world.getField(), Direction.RIGHT)), new Random(42), queue);
        ai.processInput(); queue.executePending();
        tank.update(0.4f);
        ai.processInput(); queue.executePending();
        assertEquals(3, tank.getX());
        tank.update(0.8f);
        ai.processInput(); queue.executePending();
        assertEquals(4, tank.getX());
    }
}
