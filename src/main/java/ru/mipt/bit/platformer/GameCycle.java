package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class GameCycle extends ApplicationAdapter {
    private GameWorld world;
    private KeyboardController controller;
    private GameRenderer renderer;

    @Override
    public void create() {
        renderer = new GameRenderer();
        world = GameWorld.createDefault(renderer.getFieldWidth(), renderer.getFieldHeight());
        controller = KeyboardController.forPlayer(key -> Gdx.input.isKeyPressed(key),
                world.getPlayer(), world.getField());
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        controller.processInput();
        renderer.prepareFrame(world);
        world.update(deltaTime);
        renderer.render(world);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
