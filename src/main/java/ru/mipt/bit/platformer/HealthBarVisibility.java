package ru.mipt.bit.platformer;

/** One display setting shared by every health bar in a scene. */
public final class HealthBarVisibility {
    private boolean visible;

    public boolean isVisible() { return visible; }
    public void toggle() { visible = !visible; }
}
