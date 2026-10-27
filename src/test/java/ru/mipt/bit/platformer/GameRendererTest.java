package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.utils.Disposable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameRendererTest {
    private final List<String> calls = new ArrayList<>();
    private final Batch batch = recordingProxy(Batch.class, "batch");
    private final MapRenderer map = recordingProxy(MapRenderer.class, "map");
    private final Viewport viewport = new Viewport() {
        { setWorldSize(1280, 1024); setCamera(new OrthographicCamera()); }
        @Override public void apply() { calls.add("viewport:apply"); }
        @Override public void update(int width, int height, boolean centerCamera) {
            assertTrue(centerCamera);
            calls.add("resize:" + width + ":" + height);
        }
    };
    private GL20 previousGl;

    @BeforeEach void setUpGraphicsStub() {
        previousGl = Gdx.gl;
        Gdx.gl = recordingProxy(GL20.class, "gl");
    }

    @AfterEach void restoreGraphics() { Gdx.gl = previousGl; }

    @Test void drawsAnyRegisteredViewWithoutKnowingItsModelType() {
        GameRenderer renderer = new GameRenderer(batch, map, viewport, Collections.emptyList());
        renderer.addView(view("first"));
        renderer.addView(view("second"));
        renderer.prepareFrame();
        renderer.render();
        assertEquals(Arrays.asList("first:prepare", "second:prepare", "gl:glClearColor",
                "gl:glClear", "viewport:apply", "batch:setProjectionMatrix", "map:setView", "map:render", "batch:begin", "first:render", "second:render",
                "batch:end"), calls);
    }

    @Test void endsTheBatchEvenIfAViewFails() {
        GameRenderer renderer = new GameRenderer(batch, map, viewport, Collections.emptyList());
        IllegalStateException failure = new IllegalStateException("view failed");
        renderer.addView(new ObjectView() {
            @Override public void prepareFrame() { }
            @Override public void render(Batch target) { throw failure; }
        });
        assertSame(failure, assertThrows(IllegalStateException.class, renderer::render));
        assertEquals("batch:end", calls.get(calls.size() - 1));
    }

    @Test void disposesSharedResourcesOnceInReverseOrderAndOwnsItsList() {
        Disposable texture = () -> calls.add("texture");
        Disposable other = () -> calls.add("other");
        List<Disposable> resources = new ArrayList<>(Arrays.asList(texture, other, texture));
        GameRenderer renderer = new GameRenderer(batch, map, viewport, resources);
        resources.clear();
        renderer.dispose();
        renderer.dispose();
        assertEquals(Arrays.asList("other", "texture"), calls);
    }

    @Test void rejectsNullViews() {
        GameRenderer renderer = new GameRenderer(batch, map, viewport, Collections.emptyList());
        assertThrows(NullPointerException.class, () -> renderer.addView(null));
    }

    private ObjectView view(String name) {
        return new ObjectView() {
            @Override public void prepareFrame() { calls.add(name + ":prepare"); }
            @Override public void render(Batch target) {
                assertSame(batch, target);
                calls.add(name + ":render");
            }
        };
    }

    private <T> T recordingProxy(Class<T> type, String name) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (proxy, method, args) -> {
                    assertEquals(void.class, method.getReturnType(), "Unexpected graphics call");
                    if (method.getName().equals("setProjectionMatrix") || method.getName().equals("setView")) {
                        assertSame(viewport.getCamera().combined, args[0]);
                    }
                    calls.add(name + ":" + method.getName());
                    return null;
                }));
    }

    @Test void resizesTheViewportButSkipsMinimizedWindows() {
        GameRenderer renderer = new GameRenderer(batch, map, viewport, Collections.emptyList());
        renderer.resize(800, 600);
        renderer.resize(0, 0);
        renderer.resize(800, 0);
        renderer.resize(0, 600);
        assertEquals(Collections.singletonList("resize:800:600"), calls);
    }
}
