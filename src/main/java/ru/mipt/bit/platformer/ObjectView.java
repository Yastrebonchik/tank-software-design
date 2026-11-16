package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Internal graphics contract using LibGDX Batch; not a framework-free application port.
 * <p>A graphical representation; the model does not implement this interface.
 */
public interface ObjectView {
    void prepareFrame();
    void render(Batch batch);
}
