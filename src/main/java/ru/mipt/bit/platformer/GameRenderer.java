package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.IdentityHashMap;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public final class GameRenderer implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final TiledMap level = new TmxMapLoader().load("level.tmx");
    private final TiledMapTileLayer groundLayer = getSingleLayer(level);
    private final MapRenderer levelRenderer = createSingleLayerMapRenderer(level, batch);
    private final TileMovement tileMovement = new TileMovement(groundLayer, Interpolation.smooth);
    private final Texture tankTexture = new Texture("images/tank_blue.png");
    private final Texture treeTexture = new Texture("images/greenTree.png");
    private final TextureRegion tankGraphics = new TextureRegion(tankTexture);
    private final TextureRegion treeGraphics = new TextureRegion(treeTexture);
    private final Map<Tank, Rectangle> tankRectangles = new IdentityHashMap<>();

    public int getFieldWidth() { return groundLayer.getWidth(); }
    public int getFieldHeight() { return groundLayer.getHeight(); }

    /** Capture screen positions before the model advances, as in the original game. */
    public void prepareFrame(GameWorld world) {
        tankRectangles.keySet().retainAll(world.getTanks());
        for (Tank tank : world.getTanks()) {
            Rectangle rectangle = tankRectangles.computeIfAbsent(tank,
                    ignored -> createBoundingRectangle(tankGraphics));
            tileMovement.moveRectangleBetweenTileCenters(rectangle,
                    new GridPoint2(tank.getMovementStartX(), tank.getMovementStartY()),
                    new GridPoint2(tank.getX(), tank.getY()), tank.getMovementProgress());
        }
    }

    public void render(GameWorld world) {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
        levelRenderer.render();
        batch.begin();
        for (Tank tank : world.getTanks()) {
            drawTextureRegionUnscaled(batch, tankGraphics, tankRectangles.get(tank),
                    rotation(tank.getDirection()));
        }
        for (Tree tree : world.getTrees()) {
            Rectangle rectangle = createBoundingRectangle(treeGraphics);
            moveRectangleAtTileCenter(groundLayer, rectangle, new GridPoint2(tree.getX(), tree.getY()));
            drawTextureRegionUnscaled(batch, treeGraphics, rectangle, 0f);
        }
        batch.end();
    }

    private float rotation(Tank.MoveDirection direction) {
        switch (direction) {
            case UP: return 90f;
            case LEFT: return -180f;
            case DOWN: return -90f;
            case RIGHT: return 0f;
            default: throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    @Override
    public void dispose() {
        ((Disposable) levelRenderer).dispose();
        treeTexture.dispose();
        tankTexture.dispose();
        level.dispose();
        batch.dispose();
    }
}
