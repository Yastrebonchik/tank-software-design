package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;

/** A graphical representation; the model does not implement this interface. */
public interface ObjectView {
    void prepareFrame();
    void render(Batch batch);
}
