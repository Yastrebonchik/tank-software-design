package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import ru.mipt.bit.platformer.level.LevelDefinition;
import ru.mipt.bit.platformer.level.LevelSource;
import ru.mipt.bit.platformer.util.TileMovement;

import static ru.mipt.bit.platformer.util.GdxGameUtils.*;
import static com.badlogic.gdx.Input.Keys.L;

/** The one place that selects concrete objects, assets and default controls. */
public final class DefaultGameFactory {
    private final LevelSource levelSource;
    private final Random aiRandom;

    public DefaultGameFactory(LevelSource levelSource) {
        this(levelSource, new Random());
    }

    public DefaultGameFactory(LevelSource levelSource, Random aiRandom) {
        this.levelSource = Objects.requireNonNull(levelSource);
        this.aiRandom = Objects.requireNonNull(aiRandom);
    }

    public GameCycle create() {
        final LevelDefinition definition;
        try {
            definition = Objects.requireNonNull(levelSource.load());
        } catch (IOException error) {
            throw new UncheckedIOException("Cannot read level: " + error.getMessage(), error);
        }
        List<Disposable> resources = new ArrayList<>();
        try {
            SpriteBatch batch = new SpriteBatch();
            resources.add(batch);
            TiledMap level = new GroundMapFactory().create(definition.getWidth(), definition.getHeight());
            resources.add(level);
            TiledMapTileLayer ground = getSingleLayer(level);
            MapRenderer mapRenderer = createSingleLayerMapRenderer(level, batch);
            resources.add((Disposable) mapRenderer);
            Texture tankTexture = new Texture("images/tank_blue.png");
            resources.add(tankTexture);
            Texture treeTexture = new Texture("images/greenTree.png");
            resources.add(treeTexture);

            Texture bulletTexture = createBulletTexture();
            resources.add(bulletTexture);
            Texture healthBarTexture = createHealthBarTexture();
            resources.add(healthBarTexture);
            GameField field = new GameField(definition.getWidth(), definition.getHeight());
            GameWorld world = new GameWorld(field);
            CommandQueue commands = new CommandQueue();
            GameRenderer renderer = new GameRenderer(batch, mapRenderer,
                    new FitViewport((float) ground.getWidth() * ground.getTileWidth(),
                            (float) ground.getHeight() * ground.getTileHeight()), resources);

            TextureRegion tankGraphics = new TextureRegion(tankTexture);
            TextureRegion treeGraphics = new TextureRegion(treeTexture);
            TextureRegion bulletGraphics = new TextureRegion(bulletTexture);
            ObjectViewFactory views = new ObjectViewFactory();
            HealthBarVisibility healthBars = new HealthBarVisibility();
            TextureRegion healthBarGraphics = new TextureRegion(healthBarTexture);
            views.register(Tank.class, tank -> new HealthBarDecorator(
                    new TankView(tank, tankGraphics, new TileMovement(ground, Interpolation.smooth)),
                    tank, healthBars, healthBarGraphics,
                    (float) ground.getWidth() * ground.getTileWidth(),
                    (float) ground.getHeight() * ground.getTileHeight()));
            views.register(Tree.class, tree -> new TreeView(tree, treeGraphics, ground));
            views.register(Bullet.class, bullet -> new BulletView(bullet, bulletGraphics, ground));
            WorldViewListener graphics = new WorldViewListener(world, renderer, views);
            renderer.addResource(graphics);
            resources.add(graphics);

            // Subscribe before adding any models, including the initial scene.
            Tank player = new Tank(definition.getPlayerStart().getX(), definition.getPlayerStart().getY(), 0.4f);
            world.addObject(player);
            for (LevelDefinition.Position position : definition.getTreePositions()) {
                world.addObject(new Tree(position.getX(), position.getY()));
            }
            KeyboardController input = new KeyboardController(key -> Gdx.input.isKeyPressed(key), commands);
            PlayerKeyBindings.install(input, player, world);
            input.bindOnPress(new ToggleHealthBarsCommand(healthBars), L);
            List<InputHandler> controllers = new ArrayList<>();
            controllers.add(input);
            for (LevelDefinition.Position position : definition.getBotStarts()) {
                Tank bot = new Tank(position.getX(), position.getY(), 0.4f);
                world.addObject(bot);
                List<GameCommand> actions = new ArrayList<>();
                for (Direction direction : Direction.values()) { actions.add(new MoveTankCommand(bot, field, direction)); }
                actions.add(new ShootTankCommand(bot, world));
                controllers.add(new RandomTankController(bot, actions, new Random(aiRandom.nextLong()), commands));
            }
            return new GameCycle(new CompositeInputHandler(controllers), commands, world, renderer);
        } catch (RuntimeException | Error failure) {
            for (int i = resources.size() - 1; i >= 0; i--) {
                try {
                    resources.get(i).dispose();
                } catch (RuntimeException | Error cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            throw failure;
        }
    }

    private Texture createHealthBarTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        try {
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            return new Texture(pixmap);
        } finally { pixmap.dispose(); }
    }

    private Texture createBulletTexture() {
        Pixmap pixmap = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        try {
            pixmap.setColor(1f, 0.85f, 0.2f, 1f);
            pixmap.fillCircle(4, 4, 3);
            return new Texture(pixmap);
        } finally { pixmap.dispose(); }
    }
}
