package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.util.TileMovement;
import java.util.Objects;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public final class TankView implements ObjectView {
    private final Tank tank;
    private final TextureRegion graphics;
    private final TileMovement tileMovement;
    private final Rectangle rectangle;
    private final GridPoint2 from = new GridPoint2();
    private final GridPoint2 to = new GridPoint2();

    public TankView(Tank tank, TextureRegion graphics, TileMovement tileMovement) {
        this.tank = Objects.requireNonNull(tank);
        this.graphics = Objects.requireNonNull(graphics);
        this.tileMovement = Objects.requireNonNull(tileMovement);
        rectangle = createBoundingRectangle(graphics);
    }

    @Override
    public void prepareFrame() {
        from.set(tank.getMovementStartX(), tank.getMovementStartY());
        to.set(tank.getX(), tank.getY());
        tileMovement.moveRectangleBetweenTileCenters(rectangle, from, to, tank.getMovementProgress());
    }

    @Override
    public void render(Batch batch) {
        drawTextureRegionUnscaled(batch, graphics, rectangle, rotation(tank.getDirection()));
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
}
