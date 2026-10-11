package ru.mipt.bit.platformer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import ru.mipt.bit.platformer.level.FileLevelSource;
import ru.mipt.bit.platformer.level.LevelSource;
import ru.mipt.bit.platformer.level.RandomLevelSource;

/** Command-line configuration is kept outside the game loop and input dispatch. */
public final class LaunchOptions {
    private final Path file;
    private final int width;
    private final int height;
    private final int trees;
    private final Long seed;

    private LaunchOptions(Path file, int width, int height, int trees, Long seed) {
        this.file = file;
        this.width = width;
        this.height = height;
        this.trees = trees;
        this.seed = seed;
    }

    public static LaunchOptions parse(String... args) {
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String option = args[i];
            if (values.containsKey(option)) { throw new IllegalArgumentException("Duplicate option: " + option); }
            switch (option) {
                case "--random": values.put(option, "true"); break;
                case "--level": case "--width": case "--height": case "--trees": case "--seed":
                    if (i + 1 == args.length || args[i + 1].startsWith("--") || args[i + 1].isEmpty()) {
                        throw new IllegalArgumentException("Missing value for " + option);
                    }
                    values.put(option, args[++i]);
                    break;
                default: throw new IllegalArgumentException("Unknown option: " + option);
            }
        }
        if (values.containsKey("--level")) {
            if (values.size() != 1) { throw new IllegalArgumentException("--level cannot be combined with random level options"); }
            return new LaunchOptions(Paths.get(values.get("--level")), 0, 0, 0, null);
        }
        return new LaunchOptions(null, integer(values, "--width", 10), integer(values, "--height", 8),
                integer(values, "--trees", 12), values.containsKey("--seed") ? seed(values.get("--seed")) : null);
    }

    private static int integer(Map<String, String> values, String name, int fallback) {
        try { return Integer.parseInt(values.getOrDefault(name, Integer.toString(fallback))); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer", error); }
    }

    private static long seed(String value) {
        try { return Long.parseLong(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("--seed must be a long integer", error); }
    }

    public LevelSource createLevelSource() {
        if (file != null) { return new FileLevelSource(file); }
        return new RandomLevelSource(width, height, trees, seed == null ? new Random() : new Random(seed));
    }
}
