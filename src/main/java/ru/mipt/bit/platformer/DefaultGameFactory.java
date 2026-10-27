package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
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

            GameField field = new GameField(definition.getWidth(), definition.getHeight());
            GameWorld world = new GameWorld(field);
            Tank player = new Tank(definition.getPlayerStart().getX(), definition.getPlayerStart().getY(), 0.4f);
            world.addOccupant(player);
            world.addUpdatable(player);

            GameRenderer renderer = new GameRenderer(batch, mapRenderer,
                    new FitViewport((float) ground.getWidth() * ground.getTileWidth(),
                            (float) ground.getHeight() * ground.getTileHeight()), resources);
            TextureRegion tankGraphics = new TextureRegion(tankTexture);
            renderer.addView(new TankView(player, tankGraphics,
                    new TileMovement(ground, Interpolation.smooth)));
            TextureRegion treeGraphics = new TextureRegion(treeTexture);
            for (LevelDefinition.Position position : definition.getTreePositions()) {
                Tree tree = new Tree(position.getX(), position.getY());
                world.addOccupant(tree);
                renderer.addView(new TreeView(tree, treeGraphics, ground));
            }

            KeyboardController input = new KeyboardController(key -> Gdx.input.isKeyPressed(key));
            PlayerKeyBindings.install(input, player, field);
            List<InputHandler> controllers = new ArrayList<>();
            controllers.add(input);
            for (LevelDefinition.Position position : definition.getBotStarts()) {
                Tank bot = new Tank(position.getX(), position.getY(), 0.4f);
                world.addOccupant(bot);
                world.addUpdatable(bot);
                renderer.addView(new TankView(bot, tankGraphics,
                        new TileMovement(ground, Interpolation.smooth)));
                controllers.add(new RandomTankController(bot, field, new Random(aiRandom.nextLong())));
            }
            return new GameCycle(new CompositeInputHandler(controllers), world, renderer);
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
}
