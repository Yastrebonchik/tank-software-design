package ru.mipt.bit.platformer;

/**
 * Internal graphics layout contract; extends ObjectView and belongs to presentation.
 * <p>Read-only layout bounds in world units, refreshed by prepareFrame().
 */
public interface PositionedObjectView extends ObjectView {
    float getX();
    float getY();
    float getWidth();
    float getHeight();
}
