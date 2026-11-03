package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Selects one configured command while its tank is alive and idle. */
public final class RandomTankController implements InputHandler {
    private final Tank tank;
    private final Random random;
    private final List<GameCommand> actions;
    private final CommandQueue commands;

    public RandomTankController(Tank tank, List<? extends GameCommand> actions, Random random, CommandQueue commands) {
        this.tank = Objects.requireNonNull(tank);
        this.random = Objects.requireNonNull(random);
        this.commands = Objects.requireNonNull(commands);
        this.actions = new ArrayList<>(Objects.requireNonNull(actions));
        if (this.actions.isEmpty()) { throw new IllegalArgumentException("At least one action is required"); }
        this.actions.forEach(Objects::requireNonNull);
    }

    @Override public void processInput() {
        if (tank.isAlive() && !tank.isMoving()) {
            commands.submit(actions.get(random.nextInt(actions.size())));
        }
    }
}
