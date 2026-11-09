package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/** Records drawing commands without creating an OpenGL context or textures. */
final class HealthBarTestSupport {
    static final class Drawing {
        final TextureRegion texture;
        final float x, y, width, height, color;
        Drawing(Object[] args, float color) {
            texture = (TextureRegion) args[0];
            x = (Float) args[1]; y = (Float) args[2];
            width = (Float) args[3]; height = (Float) args[4];
            this.color = color;
        }
    }

    static final class Recorder {
        final List<Drawing> drawings = new ArrayList<>();
        final List<String> events = new ArrayList<>();
        float color = Color.toFloatBits(0.4f, 0.5f, 0.6f, 0.7f);
        boolean failOnDraw;
        final Batch batch = (Batch) Proxy.newProxyInstance(Batch.class.getClassLoader(), new Class<?>[] {Batch.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getPackedColor": return color;
                        case "setPackedColor": color = (Float) args[0]; return null;
                        case "setColor":
                            color = Color.toFloatBits((Float) args[0], (Float) args[1], (Float) args[2], (Float) args[3]);
                            return null;
                        case "draw":
                            if (failOnDraw) { throw new IllegalStateException("Drawing failed"); }
                            drawings.add(new Drawing(args, color)); events.add("bar"); return null;
                        case "begin": case "end": case "setProjectionMatrix": return null;
                        default: throw new AssertionError("Unexpected batch call: " + method.getName());
                    }
                });
        void clear() { drawings.clear(); events.clear(); }
    }

    static final class View implements PositionedObjectView {
        final List<String> events;
        int preparations, renders;
        float x = 100, y = 100, width = 80, height = 60;
        float nextX = x, nextY = y;
        View(List<String> events) { this.events = events; }
        public void prepareFrame() { preparations++; x = nextX; y = nextY; }
        public void render(Batch batch) { renders++; events.add("view"); }
        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
    }

    static final class Health implements HealthStatus {
        int current = 100, max = 100;
        public int getHealth() { return current; }
        public int getMaxHealth() { return max; }
    }
}
