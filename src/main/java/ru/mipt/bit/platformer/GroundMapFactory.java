package ru.mipt.bit.platformer;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import static ru.mipt.bit.platformer.util.GdxGameUtils.getSingleLayer;

/** Repeats the existing grass pattern. The logical level supplies the dimensions. */
public final class GroundMapFactory {
    public TiledMap create(int width, int height) {
        if (width <= 0 || height <= 0) { throw new IllegalArgumentException("Ground dimensions must be positive"); }
        TiledMap map = new TmxMapLoader().load("level.tmx");
        try {
            TiledMapTileLayer template = getSingleLayer(map);
            TiledMapTileLayer ground = new TiledMapTileLayer(width, height, template.getTileWidth(), template.getTileHeight());
            ground.setName("Ground");
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    ground.setCell(x, y, new TiledMapTileLayer.Cell().setTile(
                            template.getCell(x % template.getWidth(), y % template.getHeight()).getTile()));
                }
            }
            map.getLayers().remove(template);
            map.getLayers().add(ground);
            map.getProperties().put("width", width);
            map.getProperties().put("height", height);
            // The original TiledMap retains ownership of the shared tileset textures.
            return map;
        } catch (RuntimeException | Error failure) {
            map.dispose();
            throw failure;
        }
    }
}
