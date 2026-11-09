package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Interpolation;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.util.TileMovement;
import static org.junit.jupiter.api.Assertions.*;

class TankViewTest {
    @Test void readOnlyBoundsFollowThePreparedSpriteInsteadOfJumpingToTheTargetCell() {
        Tank tank = new Tank(1, 1, 0.4f);
        GameField field = new GameField(4, 3); field.addOccupant(tank);
        TextureRegion graphics = new TextureRegion() {
            @Override public int getRegionWidth() { return 80; }
            @Override public int getRegionHeight() { return 64; }
        };
        PositionedObjectView view = new TankView(tank, graphics,
                new TileMovement(new TiledMapTileLayer(4, 3, 128, 128), Interpolation.smooth));
        view.prepareFrame();
        assertEquals(152f, view.getX()); assertEquals(160f, view.getY());
        assertEquals(80f, view.getWidth()); assertEquals(64f, view.getHeight());
        assertTrue(tank.tryMove(Direction.RIGHT, field));
        assertEquals(2, tank.getX());
        view.prepareFrame(); assertEquals(152f, view.getX());
        tank.update(0.1f); view.prepareFrame();
        assertEquals(172f, view.getX(), 0.0001f); // smooth(0.25) * 128 + 152
        tank.update(0.3f); view.prepareFrame();
        assertEquals(280f, view.getX()); assertEquals(160f, view.getY());
    }
}
