package ru.mipt.bit.platformer;

import static com.badlogic.gdx.Input.Keys.*;

/** Default desktop controls belong to game configuration, not input dispatch. */
public final class PlayerKeyBindings {
    private PlayerKeyBindings() {
    }

    public static void install(KeyboardController controller, Tank player, GameWorld world) {
        GameField field = world.getField();
        controller.bind(new MoveTankCommand(player, field, Direction.UP), UP, W);
        controller.bind(new MoveTankCommand(player, field, Direction.LEFT), LEFT, A);
        controller.bind(new MoveTankCommand(player, field, Direction.DOWN), DOWN, S);
        controller.bind(new MoveTankCommand(player, field, Direction.RIGHT), RIGHT, D);
        controller.bind(new ShootTankCommand(player, world), SPACE);
    }
}
