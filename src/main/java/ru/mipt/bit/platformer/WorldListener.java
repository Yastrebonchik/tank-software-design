package ru.mipt.bit.platformer;

/**
 * Outgoing domain event port; WorldViewListener is its graphics adapter.
 * <p>Logical lifecycle events; no dependency on a rendering library.
 */
public interface WorldListener {
    void objectAdded(CellOccupant object);
    void objectRemoved(CellOccupant object);
}
