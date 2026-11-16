package ru.mipt.bit.platformer;

/**
 * Domain: State-pattern behavior used by Tank, not an infrastructure port.
 * Implementations are immutable and may be shared between tanks.
 * Time arguments are finite and non-negative; base duration is positive.
 * Progress results must be non-negative (Tank clamps completion itself).
 */
public interface TankState {
    /** Fraction of a grid step completed during this update. */
    float movementProgress(float deltaTime, float baseMoveDurationSeconds);

    boolean canShoot();

    /** Normal-speed reload work completed, in seconds; a booster can return 2 * deltaTime. */
    default float reloadProgress(float deltaTime) { return deltaTime; }
}
