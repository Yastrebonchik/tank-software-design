package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GdxGameApplicationTest {
    @Test void createsTheSceneOnlyInCreateAndPassesFrameTimeToTheCycle() {
        List<String> calls = new ArrayList<>();
        GdxGameApplication application = new GdxGameApplication(() -> {
            calls.add("create");
            return new GameCycle(() -> calls.add("input"), dt -> calls.add("update:" + dt),
                    new FrameRenderer() {
                        @Override public void prepareFrame() { calls.add("prepare"); }
                        @Override public void render() { calls.add("render"); }
                        @Override public void dispose() { calls.add("dispose"); }
                    });
        });
        assertTrue(calls.isEmpty());
        application.dispose(); // Startup can fail before scene creation.
        assertTrue(calls.isEmpty());
        Graphics previous = Gdx.graphics;
        Gdx.graphics = (Graphics) Proxy.newProxyInstance(Graphics.class.getClassLoader(),
                new Class<?>[] { Graphics.class }, (proxy, method, args) -> {
                    assertEquals("getDeltaTime", method.getName());
                    return 0.125f;
                });
        try {
            application.create();
            application.render();
            application.dispose();
            application.dispose();
        } finally {
            Gdx.graphics = previous;
        }
        assertEquals(Arrays.asList("create", "input", "prepare", "update:0.125", "render", "dispose"), calls);
    }
}
