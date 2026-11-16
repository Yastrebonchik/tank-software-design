package ru.mipt.bit.platformer;

/** Domain state: above 70% health, normal movement and shooting. */
public final class LightTankState implements TankState {
    @Override public float movementProgress(float deltaTime, float baseMoveDurationSeconds) {
        return deltaTime / baseMoveDurationSeconds;
    }

    @Override public boolean canShoot() { return true; }
}
