package ru.mipt.bit.platformer.level;

import java.io.IOException;

/** Outgoing level-loading port used during scene assembly; returns a framework-free LevelDefinition. */
@FunctionalInterface
public interface LevelSource {
    LevelDefinition load() throws IOException;
}
