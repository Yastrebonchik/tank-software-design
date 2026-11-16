package ru.mipt.bit.platformer;

/**
 * Domain capability, not an external-system port; accepts damage and reports survival.
 * <p>A collision target that can be destroyed by damage.
 */
public interface Damageable {
    void takeDamage(int damage);
    boolean isAlive();
}
