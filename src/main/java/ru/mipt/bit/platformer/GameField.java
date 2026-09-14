package ru.mipt.bit.platformer;

public final class GameField {
    private final int width;
    private final int height;
    private final CellOccupant[][] occupants;

    public GameField(int width, int height) {
        this.width = width;
        this.height = height;
        occupants = new CellOccupant[height][width];
    }

    /** Places objects during setup; the initial configuration is assumed valid. */
    public void place(CellOccupant occupant, int x, int y) {
        occupants[y][x] = occupant;
    }

    /** Returns true only after transferring occupancy to the destination cell. */
    public boolean tryRelocate(CellOccupant occupant, int fromX, int fromY,
                               int toX, int toY) {
        if (occupant == null || !isInside(fromX, fromY) || !isInside(toX, toY)) {
            return false;
        }
        if (occupants[fromY][fromX] != occupant || occupants[toY][toX] != null) {
            return false;
        }
        occupants[fromY][fromX] = null;
        occupants[toY][toX] = occupant;
        return true;
    }

    private boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
