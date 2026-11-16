package ru.mipt.bit.platformer;

import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class LaunchCombatOptionsTest {
    @Test void defaultsPreserveThePreviousWeapon() {
        CombatSettings combat = LaunchOptions.parse().createCombatSettings();
        assertEquals(100, combat.getHealth());
        assertEquals(25, combat.getDamage());
        assertEquals(0.5f, combat.getReloadSeconds());
        assertEquals(5f, combat.getBulletSpeed());
    }

    @ParameterizedTest @ValueSource(ints = {1, 10, Integer.MAX_VALUE})
    void damageIsAcceptedInBothLevelModesAndReachesTheBullet(int damage) {
        for (String[] args : new String[][]{{"--damage", Integer.toString(damage)},
                {"--level", "levels/example.txt", "--damage", Integer.toString(damage)}}) {
            CombatSettings combat = LaunchOptions.parse(args).createCombatSettings();
            assertEquals(damage, combat.getDamage());
            assertEquals(100, combat.getHealth());
            GameWorld world = new GameWorld(new GameField(10, 8));
            Tank tank = new Tank(0, 0, 0.4f, combat);
            world.addObject(tank);
            tank.tryShoot(world);
            Bullet bullet = (Bullet) world.getObjects().stream().filter(Bullet.class::isInstance).findFirst().get();
            assertEquals(damage, bullet.getDamage());
        }
    }

    @ParameterizedTest @ValueSource(strings = {"--damage", "--damage 0", "--damage -1", "--damage x",
            "--damage 1.5", "--damage 2147483648", "--damage 10 --damage 20", "--damage --seed 42"})
    void rejectsInvalidDamage(String arguments) {
        assertThrows(IllegalArgumentException.class, () -> LaunchOptions.parse(arguments.split(" ")));
    }

    @ParameterizedTest @ValueSource(floats = {0f, -1f, Float.NaN, Float.POSITIVE_INFINITY})
    void sceneConfigurationRejectsInvalidBaseDurationBeforeGraphicsAreCreated(float duration) {
        assertThrows(IllegalArgumentException.class,
                () -> new DefaultGameFactory(() -> null, new Random(42), duration, CombatSettings.DEFAULT));
    }

    @Test void sceneConfigurationRejectsMissingCombatSettings() {
        assertThrows(NullPointerException.class,
                () -> new DefaultGameFactory(() -> null, new Random(42), 0.4f, null));
    }
}
