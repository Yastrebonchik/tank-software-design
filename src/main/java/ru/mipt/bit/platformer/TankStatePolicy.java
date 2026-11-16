package ru.mipt.bit.platformer;

/**
 * Domain policy for selecting a tank's current State behavior, not an I/O port.
 * May consider both health and additional conditions owned by the implementation.
 * Selection must be read-only and return a non-null state: repeated queries must
 * not advance time or consume events. Mutable policies must belong to one tank.
 * Effect activation and timing, if needed, are managed outside Tank.
 */
@FunctionalInterface
public interface TankStatePolicy {
    TankState resolve(int health, int maxHealth);
}
