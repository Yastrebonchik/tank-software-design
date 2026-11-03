package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandQueue {
    private final List<GameCommand> pending = new ArrayList<>();

    public void submit(GameCommand command) { pending.add(Objects.requireNonNull(command)); }

    public void executePending() {
        List<GameCommand> frame = new ArrayList<>(pending);
        pending.clear();
        // Commands submitted during execution belong to the next frame.
        for (GameCommand command : frame) { command.execute(); }
    }
}
