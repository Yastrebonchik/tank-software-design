package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameField {
    private final int width;
    private final int height;
    private final List<CellOccupant> occupants = new ArrayList<>();

    public GameField(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void addOccupant(CellOccupant occupant) {
        Objects.requireNonNull(occupant);
        if (!contains(occupant)) { occupants.add(occupant); }
    }

    public boolean contains(CellOccupant object) {
        for (CellOccupant occupant : occupants) { if (occupant == object) { return true; } }
        return false;
    }

    public void removeOccupant(CellOccupant object) {
        occupants.removeIf(occupant -> occupant == object);
    }

    public List<CellOccupant> occupantsAt(int x, int y) {
        List<CellOccupant> found = new ArrayList<>();
        for (CellOccupant occupant : occupants) {
            if (occupant.tileOccupied(x, y)) { found.add(occupant); }
        }
        return Collections.unmodifiableList(found);
    }

    public boolean tileOccupied(int x, int y) {
        for (CellOccupant occupant : occupants) {
            if (occupant.blocksMovement() && occupant.tileOccupied(x, y)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
