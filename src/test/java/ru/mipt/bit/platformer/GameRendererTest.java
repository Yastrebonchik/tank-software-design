package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
    private GL20 previousGl;

    @BeforeEach void setUpGraphicsStub() {
        previousGl = Gdx.gl;
        Gdx.gl = recordingProxy(GL20.class, "gl");
    }

    @AfterEach void restoreGraphics() { Gdx.gl = previousGl; }

    @Test void drawsAnyRegisteredViewWithoutKnowingItsModelType() {
        GameRenderer renderer = new GameRenderer(batch, map, Collections.emptyList());
        renderer.addView(view("first"));
        renderer.addView(view("second"));
        renderer.prepareFrame();
        renderer.render();
        assertEquals(Arrays.asList("first:prepare", "second:prepare", "gl:glClearColor",
                "gl:glClear", "map:render", "batch:begin", "first:render", "second:render",
                "batch:end"), calls);
    }

    @Test void endsTheBatchEvenIfAViewFails() {
        GameRenderer renderer = new GameRenderer(batch, map, Collections.emptyList());
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
        GameRenderer renderer = new GameRenderer(batch, map, resources);
        resources.clear();
        renderer.dispose();
        renderer.dispose();
        assertEquals(Arrays.asList("other", "texture"), calls);
    }

    @Test void rejectsNullViews() {
        GameRenderer renderer = new GameRenderer(batch, map, Collections.emptyList());
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
                    calls.add(name + ":" + method.getName());
                    return null;
                }));
    }
}
