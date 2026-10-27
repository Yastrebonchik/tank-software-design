package ru.mipt.bit.platformer.level;

import java.io.IOException;

@FunctionalInterface
public interface LevelSource {
    LevelDefinition load() throws IOException;
}
