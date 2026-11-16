package ru.mipt.bit.platformer;

/**
 * Application action contract; shared by keyboard and AI command sources.
 * <p>An input action independent of the device that requested it.
 */
@FunctionalInterface
public interface GameCommand {
    void execute();
}
