package ru.mipt.bit.platformer;

/**
 * Domain: the default TankStatePolicy; the single place defining health thresholds.
 * Death is handled by Tank/GameWorld, not by a fourth movement state.
 */
public final class HealthTankStateResolver implements TankStatePolicy {
    private static final TankState LIGHT = new LightTankState();
    private static final TankState MEDIUM = new MediumTankState();
    private static final TankState HEAVY = new HeavyTankState();

    @Override
    public TankState resolve(int health, int maxHealth) {
        if (maxHealth <= 0 || health < 0 || health > maxHealth) {
            throw new IllegalArgumentException("Health must be between zero and a positive maximum");
        }
        // Long products avoid both percentage rounding and int overflow.
        long percentage = 100L * health;
        if (percentage > 70L * maxHealth) { return LIGHT; }
        if (percentage >= 15L * maxHealth) { return MEDIUM; }
        return HEAVY;
    }
}
