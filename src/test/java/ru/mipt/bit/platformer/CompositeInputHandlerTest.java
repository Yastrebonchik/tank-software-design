package ru.mipt.bit.platformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.badlogic.gdx.Input.Keys.D;

class CompositeInputHandlerTest {
    @Test void preservesOrderAndCopiesTheHandlerList() {
        List<String> calls = new ArrayList<>();
        List<InputHandler> handlers = new ArrayList<>(Arrays.asList(
                () -> calls.add("player"), () -> calls.add("bot1"), () -> calls.add("bot2")));
        CompositeInputHandler input = new CompositeInputHandler(handlers);
        handlers.clear();
        input.processInput(); input.processInput();
        assertEquals(Arrays.asList("player", "bot1", "bot2", "player", "bot1", "bot2"), calls);
        new CompositeInputHandler(Collections.emptyList()).processInput();
    }

    @Test void allControllersRunBeforeUpdateAndFirstCommandWinsContestedCell() {
        GameField field = new GameField(3, 1);
        GameWorld world = new GameWorld(field);
        Tank player = new Tank(0, 0, 0.4f), bot = new Tank(2, 0, 0.4f);
        world.addOccupant(player); world.addUpdatable(player);
        world.addOccupant(bot); world.addUpdatable(bot);
        KeyboardController keyboard = new KeyboardController(key -> key == D);
        PlayerKeyBindings.install(keyboard, player, field);
        GameCommand botMove = new MoveTankCommand(bot, field, Direction.LEFT);
        InputHandler bots = () -> {
            assertEquals(0f, player.getMovementProgress());
            botMove.execute();
        };
        FrameRenderer observer = new FrameRenderer() {
            public void prepareFrame() {
                assertTrue(player.isMoving()); assertFalse(bot.isMoving());
                assertEquals(Direction.LEFT, bot.getDirection());
                assertEquals(0f, player.getMovementProgress());
            }
            public void render() { assertEquals(0.5f, player.getMovementProgress()); }
            public void resize(int width, int height) { }
            public void dispose() { }
        };
        new GameCycle(new CompositeInputHandler(Arrays.asList(keyboard, bots)), world, observer).tick(0.2f);
        assertEquals(1, player.getX()); assertEquals(2, bot.getX());
    }

    @Test void keyboardDoesNotControlOtherTanks() {
        GameField field = new GameField(5, 5);
        Tank player = new Tank(0, 0, 0.4f), bot = new Tank(3, 3, 0.4f);
        field.addOccupant(player); field.addOccupant(bot);
        KeyboardController keyboard = new KeyboardController(key -> key == D);
        PlayerKeyBindings.install(keyboard, player, field);
        keyboard.processInput();
        assertTrue(player.isMoving()); assertFalse(bot.isMoving());
        assertEquals(3, bot.getX()); assertEquals(3, bot.getY());
    }

    @Test void rejectsMissingHandlers() {
        assertThrows(NullPointerException.class, () -> new CompositeInputHandler(null));
        assertThrows(NullPointerException.class, () -> new CompositeInputHandler(Arrays.asList(() -> {}, null)));
    }
}
