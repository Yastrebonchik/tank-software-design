package ru.mipt.bit.platformer;

import com.badlogic.gdx.utils.Disposable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/** Keeps the graphics in sync through the same events for initial and runtime objects. */
public final class WorldViewListener implements WorldListener, Disposable {
    private final GameWorld world;
    private final GameRenderer renderer;
    private final ObjectViewFactory factory;
    private final Map<CellOccupant, ObjectView> views = new IdentityHashMap<>();
    private boolean disposed;

    public WorldViewListener(GameWorld world, GameRenderer renderer, ObjectViewFactory factory) {
        this.world = Objects.requireNonNull(world);
        this.renderer = Objects.requireNonNull(renderer);
        this.factory = Objects.requireNonNull(factory);
        world.addListener(this);
    }

    @Override public void objectAdded(CellOccupant object) {
        if (!disposed && !views.containsKey(object)) {
            ObjectView view = factory.create(object);
            views.put(object, view);
            renderer.addView(view);
        }
    }

    @Override public void objectRemoved(CellOccupant object) {
        ObjectView view = views.remove(object);
        if (view != null) { renderer.removeView(view); }
    }

    @Override public void dispose() {
        if (!disposed) {
            disposed = true;
            world.removeListener(this);
            for (ObjectView view : views.values()) { renderer.removeView(view); }
            views.clear();
        }
    }
}
