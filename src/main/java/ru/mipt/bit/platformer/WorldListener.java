package ru.mipt.bit.platformer;

/** Logical lifecycle events; no dependency on a rendering library. */
public interface WorldListener {
    void objectAdded(CellOccupant object);
    void objectRemoved(CellOccupant object);
}
