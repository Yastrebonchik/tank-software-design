package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldViewListenerTest {
    private final GameWorld world = new GameWorld(new GameField(5, 5));
    private final GameRenderer renderer = new GameRenderer(unused(Batch.class), unused(MapRenderer.class),
            new Viewport() {}, Collections.emptyList());
    private final ObjectViewFactory factory = new ObjectViewFactory();
    private final List<CellOccupant> prepared = new ArrayList<>();

    @Test void createsInitialAndDynamicViewsThroughEventsAndRemovesExactlyTheRightOne() {
        factory.register(Tree.class, this::view);
        factory.register(Tank.class, this::view);
        factory.register(Bullet.class, this::view);
        WorldViewListener listener = new WorldViewListener(world, renderer, factory);
        renderer.addResource(listener);
        Tree tree = new Tree(4, 4);
        Tank tank = new Tank(0, 0, 0.4f);
        world.addObject(tree); world.addObject(tank); world.addObject(tank);
        renderer.prepareFrame();
        assertEquals(world.getObjects(), prepared);
        tank.tryShoot(world);
        prepared.clear(); renderer.prepareFrame();
        assertEquals(world.getObjects(), prepared);
        Bullet bullet = (Bullet) world.getObjects().get(2);
        world.removeObject(bullet); world.removeObject(tree); world.removeObject(tree);
        prepared.clear(); renderer.prepareFrame();
        assertEquals(Collections.singletonList(tank), prepared);
        renderer.dispose(); renderer.dispose();
        prepared.clear(); renderer.prepareFrame();
        assertTrue(prepared.isEmpty());
        world.addObject(new Tree(3, 3)); // The disposed graphics adapter is unsubscribed.
        renderer.prepareFrame();
        assertTrue(prepared.isEmpty());
    }

    @Test void newModelTypesOnlyNeedARegisteredViewFactory() {
        class Rock implements CellOccupant {
            public boolean tileOccupied(int x, int y) { return x == 2 && y == 2; }
        }
        factory.register(Rock.class, this::view);
        WorldViewListener listener = new WorldViewListener(world, renderer, factory);
        Rock rock = new Rock(); world.addObject(rock);
        renderer.prepareFrame();
        assertEquals(Collections.singletonList(rock), prepared);
        listener.dispose();
    }

    @Test void registryRejectsMissingDuplicateOrUnknownMappings() {
        assertThrows(NullPointerException.class, () -> factory.register(null, this::view));
        assertThrows(NullPointerException.class, () -> factory.register(Tree.class, null));
        assertThrows(IllegalArgumentException.class, () -> factory.create(new Tree(0, 0)));
        assertThrows(NullPointerException.class, () -> factory.create(null));
        factory.register(Tree.class, tree -> null);
        assertThrows(IllegalArgumentException.class, () -> factory.register(Tree.class, this::view));
        assertThrows(NullPointerException.class, () -> factory.create(new Tree(0, 0)));
    }

    @Test void resourcesAddedLaterAreDisposedOnceEvenIfRegisteredTwice() {
        AtomicInteger disposals = new AtomicInteger();
        com.badlogic.gdx.utils.Disposable resource = disposals::incrementAndGet;
        renderer.addResource(resource); renderer.addResource(resource);
        renderer.dispose(); renderer.dispose();
        assertEquals(1, disposals.get());
        assertThrows(NullPointerException.class, () -> renderer.addResource(null));
    }

    private ObjectView view(CellOccupant object) {
        return new ObjectView() {
            public void prepareFrame() { prepared.add(object); }
            public void render(Batch batch) { }
        };
    }

    private static <T> T unused(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, args) -> { throw new AssertionError("No graphics context needed"); }));
    }
}
