package ru.mipt.bit.platformer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import ru.mipt.bit.platformer.level.FileLevelSource;
import ru.mipt.bit.platformer.level.LevelSource;
import ru.mipt.bit.platformer.level.RandomLevelSource;
import ru.mipt.bit.platformer.level.RandomTankLevelSource;

/** Command-line configuration is kept outside the game loop and input dispatch. */
public final class LaunchOptions {
    private final Path file;
    private final int width;
    private final int height;
    private final int trees;
    private final Long seed;
    private final int bots;

    private LaunchOptions(Path file, int width, int height, int trees, int bots, Long seed) {
        this.file = file;
        this.width = width;
        this.height = height;
        this.trees = trees;
        this.seed = seed;
        this.bots = bots;
    }

    public static LaunchOptions parse(String... args) {
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String option = args[i];
            if (values.containsKey(option)) { throw new IllegalArgumentException("Duplicate option: " + option); }
            switch (option) {
                case "--random": values.put(option, "true"); break;
                case "--level": case "--width": case "--height": case "--trees": case "--seed": case "--bots":
                    if (i + 1 == args.length || args[i + 1].startsWith("--") || args[i + 1].isEmpty()) {
                        throw new IllegalArgumentException("Missing value for " + option);
                    }
                    values.put(option, args[++i]);
                    break;
                default: throw new IllegalArgumentException("Unknown option: " + option);
            }
        }
        int bots = integer(values, "--bots", 3);
        if (bots < 0) { throw new IllegalArgumentException("--bots must be non-negative"); }
        Long seed = values.containsKey("--seed") ? seed(values.get("--seed")) : null;
        if (values.containsKey("--level")) {
            if (values.containsKey("--random") || values.containsKey("--width")
                    || values.containsKey("--height") || values.containsKey("--trees")) {
                throw new IllegalArgumentException("--level cannot be combined with random level options");
            }
            return new LaunchOptions(Paths.get(values.get("--level")), 0, 0, 0, bots, seed);
        }
        return new LaunchOptions(null, integer(values, "--width", 10), integer(values, "--height", 8),
                integer(values, "--trees", 12), bots, seed);
    }

    private static int integer(Map<String, String> values, String name, int fallback) {
        try { return Integer.parseInt(values.getOrDefault(name, Integer.toString(fallback))); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(name + " must be an integer", error); }
    }

    private static long seed(String value) {
        try { return Long.parseLong(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("--seed must be a long integer", error); }
    }

    /** Independent stream: AI decisions never consume the layout generator's randomness. */
    public Random createAiRandom() { return random(0xB07A1L); }

    private Random random(long salt) { return seed == null ? new Random() : new Random(seed ^ salt); }

    public LevelSource createLevelSource() {
        LevelSource terrain = file != null ? new FileLevelSource(file)
                : new RandomLevelSource(width, height, trees, random(0L));
        return new RandomTankLevelSource(terrain, bots, random(0x5DEECE66DL));
    }
}
