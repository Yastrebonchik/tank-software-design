package ru.mipt.bit.platformer;

import java.util.Objects;

public final class ShootTankCommand implements GameCommand {
    private final Tank tank;
    private final GameWorld world;

    public ShootTankCommand(Tank tank, GameWorld world) {
        this.tank = Objects.requireNonNull(tank);
        this.world = Objects.requireNonNull(world);
    }

    @Override public void execute() { tank.tryShoot(world); }
}
