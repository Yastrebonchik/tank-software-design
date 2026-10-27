package ru.mipt.bit.platformer.level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import ru.mipt.bit.platformer.Direction;
import ru.mipt.bit.platformer.level.LevelDefinition.Position;

/** Adds random tank starts to any level source without changing its file format. */
public final class RandomTankLevelSource implements LevelSource {
    private final LevelSource source;
    private final int count;
    private final Random random;

    public RandomTankLevelSource(LevelSource source, int count, Random random) {
        if (count < 0) { throw new IllegalArgumentException("--bots must be non-negative"); }
        this.source = Objects.requireNonNull(source);
        this.count = count;
        this.random = Objects.requireNonNull(random);
    }

    @Override public LevelDefinition load() throws IOException {
        LevelDefinition level = Objects.requireNonNull(source.load());
        if (count == 0) { return level; }
        Set<Position> occupied = new HashSet<>(level.getTreePositions());
        occupied.add(level.getPlayerStart());
        occupied.addAll(level.getBotStarts());
        List<Position> neighbors = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            int x = level.getPlayerStart().getX() + direction.getDeltaX();
            int y = level.getPlayerStart().getY() + direction.getDeltaY();
            Position position = new Position(x, y);
            if (x >= 0 && x < level.getWidth() && y >= 0 && y < level.getHeight()
                    && !occupied.contains(position)) {
                neighbors.add(position);
            }
        }
        // Preserve one possible first move; do not change an intentionally enclosed file level.
        if (!neighbors.isEmpty()) { occupied.add(neighbors.get(random.nextInt(neighbors.size()))); }
        List<Position> candidates = new ArrayList<>();
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                Position position = new Position(x, y);
                if (!occupied.contains(position)) { candidates.add(position); }
            }
        }
        if (count > candidates.size()) {
            throw new IllegalArgumentException("Cannot place " + count + " AI tanks: only "
                    + candidates.size() + " cells available while preserving a possible player move. "
                    + "Reduce --bots or --trees, or enlarge the level.");
        }
        Collections.shuffle(candidates, random);
        List<Position> bots = new ArrayList<>(level.getBotStarts());
        bots.addAll(candidates.subList(0, count));
        return new LevelDefinition(level.getWidth(), level.getHeight(), level.getPlayerStart(),
                level.getTreePositions(), bots);
    }
}
