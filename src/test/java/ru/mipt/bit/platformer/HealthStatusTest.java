package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HealthStatusTest {
    @Test void defaultTankExposesCurrentAndMaximumHealthThroughAReadOnlyContract() {
        Tank tank = new Tank(0, 0, 0.4f);
        HealthStatus status = tank;
        assertEquals(100, status.getHealth()); assertEquals(100, status.getMaxHealth());
        tank.takeDamage(25);
        assertEquals(75, status.getHealth()); assertEquals(100, status.getMaxHealth());
        tank.takeDamage(100);
        assertEquals(0, status.getHealth()); assertEquals(100, status.getMaxHealth());
    }

    @Test void maximumHealthComesFromTheTanksOwnConfiguration() {
        Tank light = new Tank(0, 0, 0.4f, new CombatSettings(80, 25, 0.5f, 5f));
        Tank heavy = new Tank(1, 0, 0.4f, new CombatSettings(200, 25, 0.5f, 5f));
        light.takeDamage(40); heavy.takeDamage(40);
        assertEquals(80, light.getMaxHealth()); assertEquals(40, light.getHealth());
        assertEquals(200, heavy.getMaxHealth()); assertEquals(160, heavy.getHealth());
    }
}
