package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;

public final class KeyboardController implements InputHandler {
    private final IntPredicate isKeyPressed;
    private final CommandQueue commands;
    private final List<Binding> bindings = new ArrayList<>();

    public KeyboardController(IntPredicate isKeyPressed, CommandQueue commands) {
        this.isKeyPressed = Objects.requireNonNull(isKeyPressed);
        this.commands = Objects.requireNonNull(commands);
    }

    public void bind(GameCommand action, int... keys) {
        bind(action, Trigger.WHILE_HELD, keys);
    }

    /** Executes once when any bound key becomes pressed, then waits for release. */
    public void bindOnPress(GameCommand action, int... keys) {
        bind(action, Trigger.ON_PRESS, keys);
    }

    private void bind(GameCommand action, Trigger trigger, int... keys) {
        if (keys.length == 0) {
            throw new IllegalArgumentException("A binding needs at least one key");
        }
        bindings.add(new Binding(Objects.requireNonNull(action), keys.clone(), trigger));
    }

    @Override
    public void processInput() {
        for (Binding binding : bindings) {
            boolean pressed = false;
            for (int key : binding.keys) {
                if (isKeyPressed.test(key)) {
                    pressed = true;
                    break;
                }
            }
            if (pressed && (binding.trigger == Trigger.WHILE_HELD || !binding.wasPressed)) {
                commands.submit(binding.action);
            }
            binding.wasPressed = pressed;
        }
    }

    private enum Trigger { WHILE_HELD, ON_PRESS }

    private static final class Binding {
        private final GameCommand action;
        private final int[] keys;
        private final Trigger trigger;
        private boolean wasPressed;

        private Binding(GameCommand action, int[] keys, Trigger trigger) {
            this.action = action;
            this.keys = keys;
            this.trigger = trigger;
        }
    }
}
