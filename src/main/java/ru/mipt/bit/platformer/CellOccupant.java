package ru.mipt.bit.platformer;

public interface CellOccupant {
    boolean tileOccupied(int x, int y);

    /** Projectiles can collide with objects without acting as walls for movement. */
    default boolean blocksMovement() { return true; }
}
