package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

public final class GameRenderer implements FrameRenderer {
    private final Batch batch;
    private final Viewport viewport;
    private final MapRenderer levelRenderer;
    private final List<ObjectView> views = new ArrayList<>();
    private final List<Disposable> resources = new ArrayList<>();
    private boolean disposed;

    /** Ownership of the supplied resources is transferred to this renderer. */
    public GameRenderer(Batch batch, MapRenderer levelRenderer, Viewport viewport, List<? extends Disposable> resources) {
        this.batch = Objects.requireNonNull(batch);
        this.viewport = Objects.requireNonNull(viewport);
        this.levelRenderer = Objects.requireNonNull(levelRenderer);
        Set<Disposable> unique = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Disposable resource : resources) {
            if (unique.add(Objects.requireNonNull(resource))) {
                this.resources.add(resource);
            }
        }
    }

    public void addView(ObjectView view) {
        views.add(Objects.requireNonNull(view));
    }

    @Override
    public void prepareFrame() {
        for (ObjectView view : views) {
            view.prepareFrame();
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        levelRenderer.setView(viewport.getCamera().combined, 0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight());
        levelRenderer.render();
        batch.begin();
        try {
            for (ObjectView view : views) {
                view.render(batch);
            }
        } finally {
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        // A minimized window can report zero dimensions.
        if (width > 0 && height > 0) { viewport.update(width, height, true); }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            for (int i = resources.size() - 1; i >= 0; i--) {
                resources.get(i).dispose();
            }
        }
    }
}
