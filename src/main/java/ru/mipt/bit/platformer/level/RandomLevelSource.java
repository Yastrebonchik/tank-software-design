package ru.mipt.bit.platformer.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import ru.mipt.bit.platformer.Direction;
import ru.mipt.bit.platformer.level.LevelDefinition.Position;

public final class RandomLevelSource implements LevelSource {
    private final int width;
    private final int height;
    private final int treeCount;
    private final Random random;

    public RandomLevelSource(int width, int height, int treeCount, Random random) {
        int cells = LevelDefinition.cellCount(width, height);
        if (cells < 2 || treeCount < 0 || treeCount > cells - 2) {
            throw new IllegalArgumentException("Random level needs at least two cells and 0 <= trees <= width * height - 2");
        }
        this.width = width;
        this.height = height;
        this.treeCount = treeCount;
        this.random = Objects.requireNonNull(random);
    }

    @Override public LevelDefinition load() {
        int cells = width * height;
        int playerIndex = random.nextInt(cells);
        Position player = position(playerIndex);
        List<Integer> neighbors = new ArrayList<>(4);
        for (Direction direction : Direction.values()) {
            int x = player.getX() + direction.getDeltaX();
            int y = player.getY() + direction.getDeltaY();
            if (x >= 0 && x < width && y >= 0 && y < height) { neighbors.add(y * width + x); }
        }
        int firstMove = neighbors.get(random.nextInt(neighbors.size()));
        List<Integer> candidates = new ArrayList<>(cells - 2);
        for (int cell = 0; cell < cells; cell++) {
            if (cell != playerIndex && cell != firstMove) { candidates.add(cell); }
        }
        Collections.shuffle(candidates, random);
        List<Position> trees = new ArrayList<>(treeCount);
        for (int i = 0; i < treeCount; i++) { trees.add(position(candidates.get(i))); }
        return new LevelDefinition(width, height, player, trees);
    }

    private Position position(int index) { return new Position(index % width, index / width); }
}
