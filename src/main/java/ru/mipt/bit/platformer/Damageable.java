package ru.mipt.bit.platformer;

/** A collision target that can be destroyed by damage. */
public interface Damageable {
    void takeDamage(int damage);
    boolean isAlive();
}
