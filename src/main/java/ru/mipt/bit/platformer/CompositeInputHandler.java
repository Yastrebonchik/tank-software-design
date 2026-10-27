package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Runs control strategies in a stable order before the world advances. */
public final class CompositeInputHandler implements InputHandler {
    private final List<InputHandler> handlers;

    public CompositeInputHandler(List<? extends InputHandler> handlers) {
        this.handlers = new ArrayList<>(Objects.requireNonNull(handlers));
        this.handlers.forEach(Objects::requireNonNull);
    }

    @Override public void processInput() {
        for (InputHandler handler : handlers) {
            handler.processInput();
        }
    }
}
