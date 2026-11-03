package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import java.util.Objects;

public final class BulletView implements ObjectView {
    private final Bullet bullet;
    private final TextureRegion graphics;
    private final float tileWidth;
    private final float tileHeight;
    private final float size;
    private float x;
    private float y;

    public BulletView(Bullet bullet, TextureRegion graphics, TiledMapTileLayer ground) {
        this.bullet = Objects.requireNonNull(bullet);
        this.graphics = Objects.requireNonNull(graphics);
        Objects.requireNonNull(ground);
        tileWidth = ground.getTileWidth();
        tileHeight = ground.getTileHeight();
        size = Math.min(tileWidth, tileHeight) * 0.14f;
    }

    @Override public void prepareFrame() {
        x = (bullet.getDisplayX() + 0.5f) * tileWidth - size / 2;
        y = (bullet.getDisplayY() + 0.5f) * tileHeight - size / 2;
    }

    @Override public void render(Batch batch) { batch.draw(graphics, x, y, size, size); }
}
