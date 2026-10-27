package ru.mipt.bit.platformer;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class GameDesktopLauncher {
    public static void main(String[] args) {
        LaunchOptions options = LaunchOptions.parse(args);
        DefaultGameFactory factory = new DefaultGameFactory(options.createLevelSource(), options.createAiRandom());
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280, 1024);
        new Lwjgl3Application(new GdxGameApplication(factory::create), config);
    }
}
