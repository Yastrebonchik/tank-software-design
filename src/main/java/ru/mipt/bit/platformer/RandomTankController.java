package ru.mipt.bit.platformer;

import java.util.Objects;
import java.util.Random;

/** One random attempt per frame while idle; no direction changes during a step. */
public final class RandomTankController implements InputHandler {
    private final Tank tank;
    private final Random random;
    private final GameCommand[] moves;

    public RandomTankController(Tank tank, GameField field, Random random) {
        this.tank = Objects.requireNonNull(tank);
        this.random = Objects.requireNonNull(random);
        Direction[] directions = Direction.values();
        moves = new GameCommand[directions.length];
        for (int i = 0; i < directions.length; i++) {
            moves[i] = new MoveTankCommand(tank, field, directions[i]);
        }
    }

    @Override public void processInput() {
        if (!tank.isMoving()) {
            moves[random.nextInt(moves.length)].execute();
        }
    }
}
