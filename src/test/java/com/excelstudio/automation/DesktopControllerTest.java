package com.excelstudio.automation;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesktopControllerTest {

    @Test
    void closeReleasesHeldInputAndBlocksFurtherWork() {
        FakeRobotBackend backend = new FakeRobotBackend();
        DesktopController desktop = new DesktopController(backend,
                new ScreenController(new Rectangle(0, 0, 200, 100)), 0, 0);

        desktop.keyboard().pressKey(KeyEvent.VK_CONTROL);
        desktop.mouse().click(10, 10);

        desktop.close();

        assertTrue(backend.pressedKeys.isEmpty());
        assertTrue(backend.pressedButtons.isEmpty());
        assertTrue(desktop.isCancelled());
        assertThrows(AutomationException.class, desktop::mouse);
    }

    @Test
    void cancelStopsInputBeforeTheNextAction() {
        FakeRobotBackend backend = new FakeRobotBackend();
        DesktopController desktop = new DesktopController(backend,
                new ScreenController(new Rectangle(0, 0, 200, 100)), 0, 0);

        desktop.cancel();

        assertThrows(AutomationCancelledException.class, () -> desktop.mouse().click(10, 10));
        assertTrue(backend.events.isEmpty());
    }

    @Test
    void cancellationCanBeResetForANewSession() {
        DesktopController desktop = new DesktopController(new FakeRobotBackend(),
                new ScreenController(new Rectangle(0, 0, 200, 100)), 0, 0);

        desktop.cancel();
        desktop.resetCancellation();

        assertFalse(desktop.isCancelled());
    }

    @Test
    void exposesScreenBounds() {
        DesktopController desktop = new DesktopController(new FakeRobotBackend(),
                new ScreenController(new Rectangle(0, 0, 800, 600)), 0, 0);

        assertTrue(desktop.screenBounds().equals(new Rectangle(0, 0, 800, 600)));
    }
}
