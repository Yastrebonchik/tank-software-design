package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;

import static com.badlogic.gdx.Input.Keys.*;

public final class KeyboardController {
    private final IntPredicate isKeyPressed;
    private final List<Binding> bindings = new ArrayList<>();

    public KeyboardController(IntPredicate isKeyPressed) {
        this.isKeyPressed = Objects.requireNonNull(isKeyPressed);
    }

    public static KeyboardController forPlayer(IntPredicate isKeyPressed, Tank player, GameField field) {
        KeyboardController controller = new KeyboardController(isKeyPressed);
        controller.bind(() -> player.tryMove(Tank.MoveDirection.UP, field), UP, W);
        controller.bind(() -> player.tryMove(Tank.MoveDirection.LEFT, field), LEFT, A);
        controller.bind(() -> player.tryMove(Tank.MoveDirection.DOWN, field), DOWN, S);
        controller.bind(() -> player.tryMove(Tank.MoveDirection.RIGHT, field), RIGHT, D);
        return controller;
    }

    public void bind(Runnable action, int... keys) {
        if (keys.length == 0) {
            throw new IllegalArgumentException("A binding needs at least one key");
        }
        bindings.add(new Binding(Objects.requireNonNull(action), keys.clone()));
    }

    public void processInput() {
        for (Binding binding : bindings) {
            for (int key : binding.keys) {
                if (isKeyPressed.test(key)) {
                    binding.action.run();
                    break;
                }
            }
        }
    }

    private static final class Binding {
        private final Runnable action;
        private final int[] keys;

        private Binding(Runnable action, int[] keys) {
            this.action = action;
            this.keys = keys;
        }
    }
}
