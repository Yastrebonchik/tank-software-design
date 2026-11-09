package ru.mipt.bit.platformer;

/** Read-only layout bounds in world units, refreshed by prepareFrame(). */
public interface PositionedObjectView extends ObjectView {
    float getX();
    float getY();
    float getWidth();
    float getHeight();
}
