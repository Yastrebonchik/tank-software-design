package ru.mipt.bit.platformer;

import java.util.Objects;

/** Application presentation action; changes a framework-free display preference. */
public final class ToggleHealthBarsCommand implements GameCommand {
    private final HealthBarVisibility visibility;

    public ToggleHealthBarsCommand(HealthBarVisibility visibility) {
        this.visibility = Objects.requireNonNull(visibility);
    }

    @Override public void execute() { visibility.toggle(); }
}
