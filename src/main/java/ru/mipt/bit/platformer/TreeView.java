package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import java.util.Objects;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public final class TreeView implements ObjectView {
    private final TextureRegion graphics;
    private final Rectangle rectangle;

    public TreeView(Tree tree, TextureRegion graphics, TiledMapTileLayer groundLayer) {
        Objects.requireNonNull(tree);
        this.graphics = Objects.requireNonNull(graphics);
        rectangle = createBoundingRectangle(graphics);
        moveRectangleAtTileCenter(groundLayer, rectangle, new GridPoint2(tree.getX(), tree.getY()));
    }

    @Override
    public void prepareFrame() {
        // Trees do not move; their screen position is calculated once.
    }

    @Override
    public void render(Batch batch) {
        drawTextureRegionUnscaled(batch, graphics, rectangle, 0f);
    }
}
