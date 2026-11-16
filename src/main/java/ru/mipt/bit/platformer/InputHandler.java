package ru.mipt.bit.platformer;

/** Application polling boundary for command sources; does not represent a keyboard device API. */
@FunctionalInterface
public interface InputHandler {
    void processInput();
}
