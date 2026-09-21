package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameWorld {
    private final GameField field;
    private final Tank player;
    private final List<Tank> tanks = new ArrayList<>();
    private final List<Tree> trees = new ArrayList<>();

    public GameWorld(GameField field, Tank player) {
        this.field = Objects.requireNonNull(field);
        this.player = Objects.requireNonNull(player);
        addTank(player);
    }

    public static GameWorld createDefault(int width, int height) {
        GameWorld world = new GameWorld(new GameField(width, height), new Tank(1, 1));
        world.addTree(new Tree(1, 3));
        return world;
    }

    public void addTank(Tank tank) {
        Objects.requireNonNull(tank);
        if (!tanks.contains(tank)) {
            field.addOccupant(tank);
            tanks.add(tank);
        }
    }

    public void addTree(Tree tree) {
        Objects.requireNonNull(tree);
        if (!trees.contains(tree)) {
            field.addOccupant(tree);
            trees.add(tree);
        }
    }

    public void update(float deltaTime) {
        for (Tank tank : tanks) {
            tank.update(deltaTime);
        }
    }

    public GameField getField() { return field; }
    public Tank getPlayer() { return player; }
    public List<Tank> getTanks() { return Collections.unmodifiableList(tanks); }
    public List<Tree> getTrees() { return Collections.unmodifiableList(trees); }
}
