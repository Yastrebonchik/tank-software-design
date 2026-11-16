package ru.mipt.bit.platformer;

/** Domain state: below 15% health, one-third movement speed and no shooting. */
public final class HeavyTankState implements TankState {
    @Override public float movementProgress(float deltaTime, float baseMoveDurationSeconds) {
        return deltaTime / baseMoveDurationSeconds / 3f;
    }

    @Override public boolean canShoot() { return false; }
}
