package ru.mipt.bit.platformer;

import java.util.Objects;

/** Coordinates a frame without depending on a window or a graphics library. */
public final class GameCycle {
    private final InputHandler input;
    private final Updatable world;
    private final FrameRenderer renderer;
    private boolean disposed;

    public GameCycle(InputHandler input, Updatable world, FrameRenderer renderer) {
        this.input = Objects.requireNonNull(input);
        this.world = Objects.requireNonNull(world);
        this.renderer = Objects.requireNonNull(renderer);
    }

    public void tick(float deltaTime) {
        if (disposed) {
            throw new IllegalStateException("Game cycle has been disposed");
        }
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("deltaTime must be finite and non-negative");
        }
        input.processInput();
        renderer.prepareFrame();
        world.update(deltaTime);
        renderer.render();
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            renderer.dispose();
        }
    }
}
