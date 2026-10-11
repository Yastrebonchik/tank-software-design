package ru.mipt.bit.platformer;

import static com.badlogic.gdx.Input.Keys.*;

/** Default desktop controls belong to game configuration, not input dispatch. */
public final class PlayerKeyBindings {
    private PlayerKeyBindings() {
    }

    public static void install(KeyboardController controller, Tank player, GameField field) {
        controller.bind(() -> player.tryMove(Direction.UP, field), UP, W);
        controller.bind(() -> player.tryMove(Direction.LEFT, field), LEFT, A);
        controller.bind(() -> player.tryMove(Direction.DOWN, field), DOWN, S);
        controller.bind(() -> player.tryMove(Direction.RIGHT, field), RIGHT, D);
    }
}
