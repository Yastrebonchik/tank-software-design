package ru.mipt.bit.platformer;

@FunctionalInterface
public interface Updatable {
    void update(float deltaTime);
}
