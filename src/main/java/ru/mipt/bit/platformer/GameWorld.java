package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameWorld implements Updatable {
    private final GameField field;
    private final List<CellOccupant> objects = new ArrayList<>();
    private final List<Updatable> updatables = new ArrayList<>();
    private final List<WorldListener> listeners = new ArrayList<>();

    public GameWorld(GameField field) {
        this.field = Objects.requireNonNull(field);
    }

    public void addObject(CellOccupant object) {
        Objects.requireNonNull(object);
        if (containsObject(object)) { return; }
        if (!alive(object)) { throw new IllegalArgumentException("Cannot add a destroyed object"); }
        objects.add(object);
        field.addOccupant(object);
        if (object instanceof Updatable) { addUpdatable((Updatable) object); }
        for (WorldListener listener : new ArrayList<>(listeners)) { listener.objectAdded(object); }
    }

    public void removeObject(CellOccupant object) {
        if (!objects.removeIf(registered -> registered == object)) { return; }
        field.removeOccupant(object);
        updatables.removeIf(registered -> registered == object);
        for (WorldListener listener : new ArrayList<>(listeners)) { listener.objectRemoved(object); }
    }

    public boolean containsObject(CellOccupant object) {
        return objects.stream().anyMatch(registered -> registered == object);
    }

    public List<CellOccupant> getObjects() {
        return Collections.unmodifiableList(new ArrayList<>(objects));
    }

    public void addListener(WorldListener listener) {
        Objects.requireNonNull(listener);
        if (listeners.stream().noneMatch(registered -> registered == listener)) { listeners.add(listener); }
    }

    public void removeListener(WorldListener listener) { listeners.removeIf(registered -> registered == listener); }

    public void addOccupant(CellOccupant occupant) { addObject(occupant); }

    /** Also supports updating objects that do not occupy a cell. */
    public void addUpdatable(Updatable updatable) {
        Objects.requireNonNull(updatable);
        for (Updatable registered : updatables) {
            if (registered == updatable) {
                return;
            }
        }
        updatables.add(updatable);
    }

    @Override
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("deltaTime must be finite and non-negative");
        }
        removeDestroyedObjects();
        // Additions wait for the next update; removals take effect immediately.
        for (Updatable updatable : new ArrayList<>(updatables)) {
            if (alive(updatable) && updatables.stream().anyMatch(registered -> registered == updatable)) {
                updatable.update(deltaTime);
            }
        }
        removeDestroyedObjects();
    }

    private void removeDestroyedObjects() {
        for (CellOccupant object : new ArrayList<>(objects)) {
            if (!alive(object)) { removeObject(object); }
        }
    }

    private static boolean alive(Object object) {
        return !(object instanceof Damageable) || ((Damageable) object).isAlive();
    }

    public GameField getField() {
        return field;
    }
}
