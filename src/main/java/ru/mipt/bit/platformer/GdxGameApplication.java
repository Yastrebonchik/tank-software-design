package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import java.util.Objects;
import java.util.function.Supplier;

public final class GdxGameApplication extends ApplicationAdapter {
    private final Supplier<GameCycle> factory;
    private GameCycle cycle;

    public GdxGameApplication(Supplier<GameCycle> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    @Override
    public void create() {
        // GPU resources can only be constructed after LibGDX creates its context.
        cycle = Objects.requireNonNull(factory.get());
    }

    @Override
    public void render() {
        cycle.tick(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        if (cycle != null) {
            cycle.dispose();
        }
    }
}
