package ru.mipt.bit.platformer;

/** Domain update contract used by the application cycle; no framework types. */
@FunctionalInterface
public interface Updatable {
    void update(float deltaTime);
}
