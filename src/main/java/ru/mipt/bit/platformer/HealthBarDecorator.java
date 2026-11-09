package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Objects;

/** Adds a health bar to any positioned view without changing its base rendering. */
public final class HealthBarDecorator implements PositionedObjectView {
    private final PositionedObjectView wrapped;
    private final HealthStatus health;
    private final HealthBarVisibility visibility;
    private final TextureRegion pixel;
    private final float worldWidth;
    private final float worldHeight;
    private float barX;
    private float barY;
    private float barWidth;
    private final float barHeight;

    /** The pixel texture is shared and owned by the scene, not by this decorator. */
    public HealthBarDecorator(PositionedObjectView wrapped, HealthStatus health,
                              HealthBarVisibility visibility, TextureRegion pixel,
                              float worldWidth, float worldHeight) {
        this.wrapped = Objects.requireNonNull(wrapped);
        this.health = Objects.requireNonNull(health);
        this.visibility = Objects.requireNonNull(visibility);
        this.pixel = Objects.requireNonNull(pixel);
        if (!Float.isFinite(worldWidth) || worldWidth <= 0f
                || !Float.isFinite(worldHeight) || worldHeight <= 0f) {
            throw new IllegalArgumentException("World dimensions must be finite and positive");
        }
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        barHeight = Math.min(8f, worldHeight);
    }

    @Override public void prepareFrame() {
        wrapped.prepareFrame();
        barWidth = Math.min(getWidth() * 0.8f, worldWidth);
        barX = Math.max(0f, Math.min(getX() + (getWidth() - barWidth) / 2f, worldWidth - barWidth));
        // Keep the bar visible even in the top row of the map.
        barY = Math.max(0f, Math.min(getY() + getHeight() + 4f, worldHeight - barHeight));
    }

    @Override public void render(Batch batch) {
        wrapped.render(batch);
        if (!visibility.isVisible()) { return; }
        float fraction = Math.max(0f, Math.min(1f, health.getHealth() / (float) health.getMaxHealth()));
        float previousColor = batch.getPackedColor();
        try {
            batch.setColor(0.35f, 0.08f, 0.08f, 1f);
            batch.draw(pixel, barX, barY, barWidth, barHeight);
            if (fraction > 0f) {
                batch.setColor(0.2f, 0.85f, 0.25f, 1f);
                batch.draw(pixel, barX, barY, barWidth * fraction, barHeight);
            }
        } finally {
            batch.setPackedColor(previousColor);
        }
    }

    @Override public float getX() { return wrapped.getX(); }
    @Override public float getY() { return wrapped.getY(); }
    @Override public float getWidth() { return wrapped.getWidth(); }
    @Override public float getHeight() { return wrapped.getHeight(); }
}
