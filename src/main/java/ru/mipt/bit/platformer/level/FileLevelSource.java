package ru.mipt.bit.platformer.level;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.mipt.bit.platformer.level.LevelDefinition.Position;

public final class FileLevelSource implements LevelSource {
    private final Path path;

    public FileLevelSource(Path path) { this.path = Objects.requireNonNull(path); }

    @Override public LevelDefinition load() throws IOException {
        List<String> rows = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (rows.isEmpty()) { throw invalid("file is empty"); }
        // Some Windows editors write a UTF-8 BOM. It is not a map cell.
        if (rows.get(0).startsWith("\uFEFF")) { rows.set(0, rows.get(0).substring(1)); }
        int width = rows.get(0).length();
        if (width == 0) { throw invalid("line 1 is empty"); }
        LevelDefinition.cellCount(width, rows.size());
        Position player = null;
        List<Position> trees = new ArrayList<>();
        for (int row = 0; row < rows.size(); row++) {
            String line = rows.get(row);
            if (line.length() != width) {
                throw invalid("line " + (row + 1) + " has width " + line.length() + "; expected " + width);
            }
            for (int x = 0; x < width; x++) {
                Position position = new Position(x, rows.size() - 1 - row);
                switch (line.charAt(x)) {
                    case '_': break;
                    case 'T': trees.add(position); break;
                    case 'X':
                        if (player != null) { throw invalid("multiple X markers; line " + (row + 1) + ", column " + (x + 1)); }
                        player = position;
                        break;
                    default:
                        throw invalid("unknown symbol '" + line.charAt(x) + "' at line " + (row + 1) + ", column " + (x + 1));
                }
            }
        }
        if (player == null) { throw invalid("exactly one X marker is required"); }
        return new LevelDefinition(width, rows.size(), player, trees);
    }

    private IllegalArgumentException invalid(String reason) {
        return new IllegalArgumentException("Invalid level " + path + ": " + reason);
    }
}
