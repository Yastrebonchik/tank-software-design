package ru.mipt.bit.platformer;

/** Outgoing application rendering port; implemented by GameRenderer without leaking LibGDX types. */
public interface FrameRenderer {
    void prepareFrame();
    void render();
    void dispose();
    void resize(int width, int height);
}
