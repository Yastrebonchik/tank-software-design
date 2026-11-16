package ru.mipt.bit.platformer;

import java.util.Collections;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static com.badlogic.gdx.Input.Keys.SPACE;
import static org.junit.jupiter.api.Assertions.*;

class TankStatePolicyTest {
    private final GameWorld world = new GameWorld(new GameField(12, 4));

    private Tank tank(int x, CombatSettings combat, TankStatePolicy policy) {
        Tank tank = new Tank(x, 1, 0.4f, combat, policy);
        world.addObject(tank);
        return tank;
    }

    @Test void aNewStateControlsMovementAndReloadWithoutChangesToTank() {
        TankState custom = new TestState(1.5f, 3f, true);
        Tank tank = tank(2, new CombatSettings(100, 10, 0.8f, 5f), (health, max) -> custom);
        assertSame(custom, tank.getState());
        assertTrue(tank.tryShoot(world));
        assertTrue(tank.tryMove(Direction.RIGHT, world.getField()));
        tank.update(0.1f);
        assertEquals(0.375f, tank.getMovementProgress(), 0.00001f);
        assertEquals(0.5f, tank.getReloadRemaining(), 0.00001f);
        tank.update(1f / 6f);
        assertFalse(tank.isMoving());
        assertEquals(0f, tank.getReloadRemaining());
        assertTrue(tank.tryShoot(world));
    }

    @Test void conditionChangesApplyToTheRemainingStepWithoutDamageOrProgressReset() {
        SelectablePolicy policy = new SelectablePolicy();
        Tank tank = tank(2, CombatSettings.DEFAULT, policy);
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(0.1f);
        policy.selected = new TestState(1.5f, 1f, true);
        assertSame(policy.selected, tank.getState());
        assertEquals(0.25f, tank.getMovementProgress(), 0.00001f);
        tank.update(0.1f);
        assertEquals(0.625f, tank.getMovementProgress(), 0.00001f);
        assertTrue(world.getField().tileOccupied(2, 1));
        assertTrue(world.getField().tileOccupied(3, 1));
        tank.update(0.1f);
        assertFalse(tank.isMoving());
        assertFalse(world.getField().tileOccupied(2, 1));
    }

    @Test void damageDoesNotReplaceTheInjectedPolicyAndEndingAConditionUsesCurrentHealth() {
        SelectablePolicy policy = new SelectablePolicy();
        Tank tank = tank(2, CombatSettings.DEFAULT, policy);
        policy.selected = new TestState(1.5f, 1f, false);
        tank.takeDamage(90);
        assertEquals(10, tank.getHealth());
        assertSame(policy.selected, tank.getState());
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(0.1f);
        assertEquals(0.375f, tank.getMovementProgress(), 0.00001f);
        policy.selected = null;
        assertInstanceOf(HeavyTankState.class, tank.getState());
        tank.update(0.3f);
        assertEquals(0.625f, tank.getMovementProgress(), 0.00001f);
        tank.update(0.45f);
        assertFalse(tank.isMoving());
        assertFalse(tank.tryShoot(world));
    }

    @Test void policyReceivesTheTanksActualCurrentAndMaximumHealth() {
        TankState special = new TestState(1f, 1f, false);
        TankStatePolicy policy = (health, max) -> {
            assertEquals(200, max);
            return health <= 180 ? special : new LightTankState();
        };
        Tank tank = tank(2, new CombatSettings(200, 10, 0.5f, 5f), policy);
        assertInstanceOf(LightTankState.class, tank.getState());
        tank.takeDamage(20); // Not one of the default thresholds.
        assertSame(special, tank.getState());
        assertFalse(tank.tryShoot(world));
    }

    @Test void externallySelectedStateCanPermitShootingWithoutAnImplicitHealthRestriction() {
        TankState permissive = new TestState(1f, 1f, true);
        Tank tank = tank(2, CombatSettings.DEFAULT, (health, max) -> permissive);
        tank.takeDamage(90);
        assertSame(permissive, tank.getState());
        assertTrue(tank.tryShoot(world));
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void queuedKeyboardAndAiCommandsUseThePolicyAtExecutionTime(boolean ai) {
        SelectablePolicy policy = new SelectablePolicy();
        Tank tank = tank(2, CombatSettings.DEFAULT, policy);
        CommandQueue commands = new CommandQueue();
        ShootTankCommand shot = new ShootTankCommand(tank, world);
        InputHandler input;
        if (ai) {
            input = new RandomTankController(tank, Collections.singletonList(shot), new Random(42), commands);
        } else {
            KeyboardController keyboard = new KeyboardController(key -> key == SPACE, commands);
            keyboard.bind(shot, SPACE);
            input = keyboard;
        }
        input.processInput();
        policy.selected = new TestState(1f, 1f, false);
        commands.executePending();
        assertEquals(Collections.singletonList(tank), world.getObjects());
        assertEquals(0f, tank.getReloadRemaining());
        policy.selected = null;
        input.processInput(); commands.executePending();
        assertEquals(1, world.getObjects().stream().filter(Bullet.class::isInstance).count());
    }

    @Test void conditionChangesDoNotResetReloadWork() {
        SelectablePolicy policy = new SelectablePolicy();
        Tank tank = tank(2, CombatSettings.DEFAULT, policy);
        assertTrue(tank.tryShoot(world));
        tank.update(0.1f);
        policy.selected = new TestState(1f, 2f, true);
        assertEquals(0.4f, tank.getReloadRemaining(), 0.00001f);
        tank.update(0.1f);
        assertEquals(0.2f, tank.getReloadRemaining(), 0.00001f);
        policy.selected = null;
        tank.update(0.1f);
        assertFalse(tank.tryShoot(world));
        tank.update(0.1f);
        assertTrue(tank.tryShoot(world));
    }

    @Test void policiesOfDifferentTanksAreIndependent() {
        SelectablePolicy firstPolicy = new SelectablePolicy();
        SelectablePolicy secondPolicy = new SelectablePolicy();
        Tank first = tank(2, CombatSettings.DEFAULT, firstPolicy);
        Tank second = tank(6, CombatSettings.DEFAULT, secondPolicy);
        firstPolicy.selected = new TestState(1.5f, 1f, false);
        first.tryMove(Direction.RIGHT, world.getField());
        second.tryMove(Direction.RIGHT, world.getField());
        world.update(0.1f);
        assertEquals(0.375f, first.getMovementProgress(), 0.00001f);
        assertEquals(0.25f, second.getMovementProgress(), 0.00001f);
        assertInstanceOf(LightTankState.class, second.getState());
    }

    @Test void aPermissiveStateCannotReviveADeadTankOrKeepItsCellsOccupied() {
        Tank tank = tank(2, CombatSettings.DEFAULT, (health, max) -> new TestState(2f, 2f, true));
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(0.1f);
        tank.takeDamage(100);
        float progress = tank.getMovementProgress();
        tank.update(1f);
        assertEquals(progress, tank.getMovementProgress());
        assertFalse(tank.tryShoot(world));
        assertFalse(tank.tryMove(Direction.UP, world.getField()));
        world.update(0f);
        assertFalse(world.containsObject(tank));
        assertFalse(world.getField().tileOccupied(2, 1));
        assertFalse(world.getField().tileOccupied(3, 1));
    }

    @Test void repeatedStateQueriesDoNotChangeProgressOrReload() {
        TankState custom = new TestState(1.5f, 2f, true);
        Tank tank = tank(2, CombatSettings.DEFAULT, (health, max) -> custom);
        tank.tryShoot(world);
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(0.1f);
        for (int query = 0; query < 10; query++) { assertSame(custom, tank.getState()); }
        assertEquals(0.375f, tank.getMovementProgress(), 0.00001f);
        assertEquals(0.3f, tank.getReloadRemaining(), 0.00001f);
    }

    @Test void rejectsMissingPolicyAndNullInitialState() {
        assertThrows(NullPointerException.class, () -> new Tank(0, 0, 0.4f, CombatSettings.DEFAULT, null));
        assertThrows(NullPointerException.class, () -> new Tank(0, 0, 0.4f, CombatSettings.DEFAULT, (health, max) -> null));
    }

    @Test void aNullStateAfterAConditionChangeFailsBeforeMovementOrReloadAreModified() {
        SelectablePolicy policy = new SelectablePolicy();
        policy.selected = new LightTankState();
        Tank tank = tank(2, CombatSettings.DEFAULT, (health, max) -> policy.selected);
        tank.tryShoot(world);
        tank.tryMove(Direction.RIGHT, world.getField());
        tank.update(0.1f);
        float progress = tank.getMovementProgress();
        float reload = tank.getReloadRemaining();
        policy.selected = null;
        assertThrows(NullPointerException.class, () -> tank.update(0.1f));
        assertEquals(progress, tank.getMovementProgress());
        assertEquals(reload, tank.getReloadRemaining());
    }

    @Test void factoryRejectsMissingPolicySupplierBeforeCreatingGraphics() {
        assertThrows(NullPointerException.class,
                () -> new DefaultGameFactory(() -> null, new Random(42), 0.4f, CombatSettings.DEFAULT, null));
    }

    /** Test-only selection condition; no pickup or booster feature is installed in the game. */
    private static final class SelectablePolicy implements TankStatePolicy {
        private final TankStatePolicy fallback = new HealthTankStateResolver();
        private TankState selected;

        @Override public TankState resolve(int health, int maxHealth) {
            return selected == null ? fallback.resolve(health, maxHealth) : selected;
        }
    }

    /** A new behavior known only to the tests; the production Tank has no branches for this type. */
    private static final class TestState implements TankState {
        private final float movementRate;
        private final float reloadRate;
        private final boolean shooting;

        TestState(float movementRate, float reloadRate, boolean shooting) {
            this.movementRate = movementRate;
            this.reloadRate = reloadRate;
            this.shooting = shooting;
        }

        @Override public float movementProgress(float deltaTime, float baseMoveDurationSeconds) {
            return deltaTime / baseMoveDurationSeconds * movementRate;
        }

        @Override public boolean canShoot() { return shooting; }
        @Override public float reloadProgress(float deltaTime) { return deltaTime * reloadRate; }
    }
}
