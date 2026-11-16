package ru.mipt.bit.platformer;

/** Domain state: 15% through 70% health, half movement speed. */
public final class MediumTankState implements TankState {
    @Override public float movementProgress(float deltaTime, float baseMoveDurationSeconds) {
        return deltaTime / baseMoveDurationSeconds / 2f;
    }

    @Override public boolean canShoot() { return true; }
}
