package ru.mipt.bit.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.badlogic.gdx.Input.Keys.L;
import static ru.mipt.bit.platformer.HealthBarTestSupport.*;

class HealthBarsIntegrationTest {
    @Test void oneToggleControlsInitialAndNewTanksAndSurvivesPlayerDestruction() {
        Recorder recorder = new Recorder();
        GameWorld world = new GameWorld(new GameField(4, 3));
        GameRenderer renderer = new GameRenderer(recorder.batch, noop(MapRenderer.class), new Viewport() {
            { setWorldSize(400, 300); setCamera(new OrthographicCamera()); }
            @Override public void apply() { }
        }, Collections.emptyList());
        HealthBarVisibility visibility = new HealthBarVisibility();
        ObjectViewFactory factory = new ObjectViewFactory();
        TextureRegion pixel = new TextureRegion();
        factory.register(Tank.class, tank -> new HealthBarDecorator(new View(recorder.events), tank, visibility, pixel, 400, 300));
        factory.register(Tree.class, tree -> new View(recorder.events));
        renderer.addResource(new WorldViewListener(world, renderer, factory));
        Tank player = new Tank(0, 0, 0.4f), bot = new Tank(2, 1, 0.4f);
        world.addObject(player); world.addObject(bot); world.addObject(new Tree(3, 2));
        Set<Integer> pressed = new HashSet<>();
        CommandQueue queue = new CommandQueue();
        KeyboardController keyboard = new KeyboardController(pressed::contains, queue);
        keyboard.bindOnPress(new ToggleHealthBarsCommand(visibility), L);
        GameCycle cycle = new GameCycle(keyboard, queue, world, renderer);
        GL20 previous = Gdx.gl; Gdx.gl = noop(GL20.class);
        try {
            cycle.tick(0f); assertTrue(recorder.drawings.isEmpty());
            pressed.add(L); recorder.clear(); cycle.tick(0f);
            assertEquals(4, recorder.drawings.size()); // Two bars, no bar on the tree.
            Tank addedLater = new Tank(1, 1, 0.4f); world.addObject(addedLater);
            recorder.clear(); cycle.tick(0f);
            assertEquals(6, recorder.drawings.size()); // The held key has not toggled again.
            bot.takeDamage(25); recorder.clear(); cycle.tick(0f);
            assertEquals(48f, recorder.drawings.get(3).width);
            player.takeDamage(100); recorder.clear(); cycle.tick(0f);
            assertFalse(world.containsObject(player)); assertEquals(4, recorder.drawings.size());
            pressed.clear(); cycle.tick(0f); pressed.add(L); recorder.clear(); cycle.tick(0f);
            assertTrue(recorder.drawings.isEmpty()); // L still works without a living player.
            pressed.clear(); cycle.tick(0f); pressed.add(L); recorder.clear(); cycle.tick(0f);
            assertEquals(4, recorder.drawings.size());
            cycle.dispose(); recorder.clear(); renderer.prepareFrame(); renderer.render();
            assertTrue(recorder.events.isEmpty());
        } finally { cycle.dispose(); Gdx.gl = previous; }
    }

    private static <T> T noop(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> null));
    }
}
