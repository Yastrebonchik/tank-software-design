package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Disposable;
import ru.mipt.bit.platformer.util.TileMovement;
import java.util.ArrayList;
import java.util.List;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

/** The one place that selects concrete objects, assets and default controls. */
public final class DefaultGameFactory {
    public GameCycle create() {
        List<Disposable> resources = new ArrayList<>();
        try {
            SpriteBatch batch = new SpriteBatch();
            resources.add(batch);
            TiledMap level = new TmxMapLoader().load("level.tmx");
            resources.add(level);
            TiledMapTileLayer ground = getSingleLayer(level);
            MapRenderer mapRenderer = createSingleLayerMapRenderer(level, batch);
            resources.add((Disposable) mapRenderer);
            Texture tankTexture = new Texture("images/tank_blue.png");
            resources.add(tankTexture);
            Texture treeTexture = new Texture("images/greenTree.png");
            resources.add(treeTexture);

            GameField field = new GameField(ground.getWidth(), ground.getHeight());
            GameWorld world = new GameWorld(field);
            Tank player = new Tank(1, 1, 0.4f);
            Tree tree = new Tree(1, 3);
            world.addOccupant(player);
            world.addUpdatable(player);
            world.addOccupant(tree);

            GameRenderer renderer = new GameRenderer(batch, mapRenderer, resources);
            renderer.addView(new TankView(player, new TextureRegion(tankTexture),
                    new TileMovement(ground, Interpolation.smooth)));
            renderer.addView(new TreeView(tree, new TextureRegion(treeTexture), ground));

            KeyboardController input = new KeyboardController(key -> Gdx.input.isKeyPressed(key));
            PlayerKeyBindings.install(input, player, field);
            return new GameCycle(input, world, renderer);
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
