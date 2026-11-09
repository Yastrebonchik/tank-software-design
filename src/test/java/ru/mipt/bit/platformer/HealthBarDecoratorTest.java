package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static ru.mipt.bit.platformer.HealthBarTestSupport.*;

class HealthBarDecoratorTest {
    private final Recorder recorder = new Recorder();
    private final View wrapped = new View(recorder.events);
    private final Health health = new Health();
    private final HealthBarVisibility visibility = new HealthBarVisibility();
    private final TextureRegion pixel = new TextureRegion();
    private final HealthBarDecorator decorated = new HealthBarDecorator(wrapped, health, visibility, pixel, 400, 300);

    @Test void hiddenDecoratorPreservesTheWrappedViewAndItsBounds() {
        decorated.prepareFrame(); decorated.render(recorder.batch);
        assertEquals(1, wrapped.preparations); assertEquals(1, wrapped.renders);
        assertEquals(Collections.singletonList("view"), recorder.events);
        assertTrue(recorder.drawings.isEmpty());
        assertEquals(wrapped.getX(), decorated.getX()); assertEquals(wrapped.getY(), decorated.getY());
        assertEquals(wrapped.getWidth(), decorated.getWidth()); assertEquals(wrapped.getHeight(), decorated.getHeight());
    }

    @ParameterizedTest @CsvSource({"100,100,1", "75,100,0.75", "25,100,0.25", "40,80,0.5", "0,100,0", "-1,100,0", "120,100,1"})
    void drawsTheCurrentHealthFractionAfterTheUnderlyingView(int current, int max, float expected) {
        health.current = current; health.max = max; visibility.toggle();
        float originalColor = recorder.color;
        decorated.prepareFrame(); decorated.render(recorder.batch);
        assertEquals(expected == 0 ? 1 : 2, recorder.drawings.size());
        Drawing background = recorder.drawings.get(0);
        assertSame(pixel, background.texture);
        assertEquals(64f, background.width); assertEquals(8f, background.height);
        assertEquals(108f, background.x); assertEquals(164f, background.y);
        if (expected > 0) {
            Drawing fill = recorder.drawings.get(1);
            assertSame(pixel, fill.texture);
            assertEquals(background.width * expected, fill.width, 0.0001f);
            assertEquals(background.x, fill.x); assertEquals(background.y, fill.y);
            assertEquals(background.height, fill.height);
            assertNotEquals(background.color, fill.color);
        }
        assertEquals("view", recorder.events.get(0));
        assertEquals(originalColor, recorder.color, "Bar must not tint later objects");
    }

    @Test void damageAndMovementAreReflectedOnTheNextFrameWithoutRecreatingTheDecorator() {
        visibility.toggle(); decorated.prepareFrame(); decorated.render(recorder.batch);
        float originalX = recorder.drawings.get(0).x;
        float originalY = recorder.drawings.get(0).y;
        wrapped.nextX += 20f; wrapped.nextY += 10f; health.current = 50;
        recorder.clear(); decorated.prepareFrame(); decorated.render(recorder.batch);
        assertEquals(originalX + 20f, recorder.drawings.get(0).x);
        assertEquals(originalY + 10f, recorder.drawings.get(0).y);
        assertEquals(32f, recorder.drawings.get(1).width);
        assertEquals(2, wrapped.preparations); assertEquals(2, wrapped.renders);
    }

    @ParameterizedTest @CsvSource({"-30,-70", "390,290", "0,0", "320,240"})
    void barsStayInsideTheWorldAtEveryEdge(float x, float y) {
        wrapped.nextX = x; wrapped.nextY = y; visibility.toggle();
        decorated.prepareFrame(); decorated.render(recorder.batch);
        for (Drawing drawing : recorder.drawings) {
            assertTrue(drawing.x >= 0 && drawing.y >= 0);
            assertTrue(drawing.x + drawing.width <= 400);
            assertTrue(drawing.y + drawing.height <= 300);
        }
    }

    @Test void canWrapAnotherDecoratorThroughTheSameViewContract() {
        HealthBarDecorator outer = new HealthBarDecorator(decorated, health, visibility, pixel, 400, 300);
        visibility.toggle(); outer.prepareFrame(); outer.render(recorder.batch);
        assertEquals(1, wrapped.preparations); assertEquals(1, wrapped.renders);
        assertEquals(Arrays.asList("view", "bar", "bar", "bar", "bar"), recorder.events);
        assertEquals(wrapped.getX(), outer.getX()); assertEquals(wrapped.getWidth(), outer.getWidth());
    }

    @Test void restoresBatchColorEvenWhenDrawingFails() {
        visibility.toggle(); decorated.prepareFrame(); recorder.failOnDraw = true;
        float originalColor = recorder.color;
        assertThrows(IllegalStateException.class, () -> decorated.render(recorder.batch));
        assertEquals(originalColor, recorder.color);
        assertEquals(1, wrapped.renders);
    }

    @ParameterizedTest @ValueSource(floats = {0, -1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void rejectsInvalidWorldDimensions(float dimension) {
        assertThrows(IllegalArgumentException.class, () -> new HealthBarDecorator(wrapped, health, visibility, pixel, dimension, 300));
        assertThrows(IllegalArgumentException.class, () -> new HealthBarDecorator(wrapped, health, visibility, pixel, 400, dimension));
    }

    @Test void rejectsMissingDependencies() {
        assertThrows(NullPointerException.class, () -> new HealthBarDecorator(null, health, visibility, pixel, 400, 300));
        assertThrows(NullPointerException.class, () -> new HealthBarDecorator(wrapped, null, visibility, pixel, 400, 300));
        assertThrows(NullPointerException.class, () -> new HealthBarDecorator(wrapped, health, null, pixel, 400, 300));
        assertThrows(NullPointerException.class, () -> new HealthBarDecorator(wrapped, health, visibility, null, 400, 300));
    }
}
