package com.excelstudio.automation;

import java.awt.event.KeyEvent;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyboardControllerTest {
    private FakeRobotBackend backend;
    private CancellationFlag cancellation;
    private KeyboardController keyboard;

    @BeforeEach
    void setUp() {
        backend = new FakeRobotBackend();
        cancellation = new CancellationFlag();
        keyboard = new KeyboardController(backend, cancellation, 0);
    }

    @Test
    void typingLeavesNoKeysPressed() {
        keyboard.typeText("Hello 123");

        assertFalse(keyboard.hasPressedKeys());
        assertTrue(backend.pressedKeys.isEmpty());
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_SHIFT));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ENTER) == false);
    }

    @Test
    void typingUsesShiftForUpperAndShiftedCharacters() {
        keyboard.typeText("A@");

        List<String> events = backend.events;
        int shiftPresses = (int) events.stream().filter("keyPress:16"::equals).count();
        assertEquals(2, shiftPresses);
        assertFalse(keyboard.hasPressedKeys());
    }

    @Test
    void typingNewlineSendsEnter() {
        keyboard.typeText("a\nb");

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ENTER));
    }

    @Test
    void unsupportedCharacterFailsWithoutLeavingKeysDown() {
        assertThrows(AutomationException.class, () -> keyboard.typeText("\u0001"));

        assertFalse(keyboard.hasPressedKeys());
        assertTrue(backend.pressedKeys.isEmpty());
    }

    @Test
    void shortcutPressesAndReleasesInReverseOrder() {
        keyboard.shortcut(KeyEvent.VK_CONTROL, KeyEvent.VK_C);

        assertEquals(4, backend.events.size());
        assertEquals("keyPress:" + KeyEvent.VK_CONTROL, backend.events.get(0));
        assertEquals("keyPress:" + KeyEvent.VK_C, backend.events.get(1));
        assertEquals("keyRelease:" + KeyEvent.VK_C, backend.events.get(2));
        assertEquals("keyRelease:" + KeyEvent.VK_CONTROL, backend.events.get(3));
        assertFalse(keyboard.hasPressedKeys());
    }

    @Test
    void shortcutRequiresTwoKeys() {
        assertThrows(IllegalArgumentException.class, () -> keyboard.shortcut(KeyEvent.VK_CONTROL));
    }

    @Test
    void releaseAllReleasesHeldKeys() {
        keyboard.pressKey(KeyEvent.VK_SHIFT);
        keyboard.pressKey(KeyEvent.VK_A);

        keyboard.releaseAll();

        assertFalse(keyboard.hasPressedKeys());
        assertTrue(backend.pressedKeys.isEmpty());
    }

    @Test
    void cancelledControllerPerformsNoAction() {
        cancellation.cancel();

        assertThrows(AutomationCancelledException.class, () -> keyboard.typeText("a"));
        assertTrue(backend.events.isEmpty());
    }

    @Test
    void navigationHelpersUseExpectedKeys() {
        keyboard.pressEnter();
        keyboard.pressTab();
        keyboard.pressEscape();
        keyboard.pressDelete();
        keyboard.pressBackspace();

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ENTER));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_TAB));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ESCAPE));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_DELETE));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_BACK_SPACE));
    }

    @Test
    void arrowKeyRepeatValidatesCount() {
        assertThrows(IllegalArgumentException.class, () -> keyboard.pressArrow(KeyEvent.VK_DOWN, -1));

        keyboard.pressArrow(KeyEvent.VK_DOWN, 3);
        assertEquals(3, backend.events.stream().filter("keyPress:40"::equals).count());
    }
}
