package ru.mipt.bit.platformer;

public interface FrameRenderer {
    void prepareFrame();
    void render();
    void dispose();
    void resize(int width, int height);
}
