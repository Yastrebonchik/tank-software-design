package ru.mipt.bit.platformer.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Immutable initial layout. It is not the live occupancy or position of game objects. */
public final class LevelDefinition {
    private final int width;
    private final int height;
    private final Position playerStart;
    private final List<Position> treePositions;

    public LevelDefinition(int width, int height, Position playerStart, List<Position> treePositions) {
        cellCount(width, height);
        this.width = width;
        this.height = height;
        this.playerStart = Objects.requireNonNull(playerStart);
        checkInside(playerStart);
        List<Position> copy = new ArrayList<>(Objects.requireNonNull(treePositions));
        Set<Position> occupied = new HashSet<>();
        occupied.add(playerStart);
        for (Position position : copy) {
            checkInside(Objects.requireNonNull(position));
            if (!occupied.add(position)) {
                throw new IllegalArgumentException("Overlapping objects at " + position);
            }
        }
        this.treePositions = Collections.unmodifiableList(copy);
    }

    static int cellCount(int width, int height) {
        if (width <= 0 || height <= 0 || (long) width * height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Level dimensions must be positive and their product must fit an int");
        }
        return width * height;
    }

    private void checkInside(Position position) {
        if (position.x < 0 || position.x >= width || position.y < 0 || position.y >= height) {
            throw new IllegalArgumentException("Position outside level: " + position);
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Position getPlayerStart() { return playerStart; }
    public List<Position> getTreePositions() { return treePositions; }

    public static final class Position {
        private final int x;
        private final int y;

        public Position(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }

        @Override public boolean equals(Object other) {
            if (this == other) { return true; }
            if (!(other instanceof Position)) { return false; }
            Position position = (Position) other;
            return x == position.x && y == position.y;
        }

        @Override public int hashCode() { return 31 * x + y; }
        @Override public String toString() { return "(" + x + ", " + y + ")"; }
    }
}
