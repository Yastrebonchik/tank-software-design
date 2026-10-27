package ru.mipt.bit.platformer;

import java.util.Objects;

/** Both keyboard and AI use the same movement rules of the receiver. */
public final class MoveTankCommand implements GameCommand {
    private final Tank tank;
    private final GameField field;
    private final Direction direction;

    public MoveTankCommand(Tank tank, GameField field, Direction direction) {
        this.tank = Objects.requireNonNull(tank);
        this.field = Objects.requireNonNull(field);
        this.direction = Objects.requireNonNull(direction);
    }

    @Override public void execute() {
        tank.tryMove(direction, field);
    }
}
