package ru.mipt.bit.platformer;

/** An input action independent of the device that requested it. */
@FunctionalInterface
public interface GameCommand {
    void execute();
}
