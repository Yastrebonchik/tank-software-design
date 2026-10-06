package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GameWorld implements Updatable {
    private final GameField field;
    private final List<Updatable> updatables = new ArrayList<>();

    public GameWorld(GameField field) {
        this.field = Objects.requireNonNull(field);
    }

    public void addOccupant(CellOccupant occupant) {
        field.addOccupant(occupant);
    }

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
        for (Updatable updatable : updatables) {
            updatable.update(deltaTime);
        }
    }

    public GameField getField() {
        return field;
    }
}
