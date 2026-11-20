package com.excelstudio.automation;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MouseControllerTest {
    private FakeRobotBackend backend;
    private CancellationFlag cancellation;
    private MouseController mouse;

    @BeforeEach
    void setUp() {
        backend = new FakeRobotBackend();
        cancellation = new CancellationFlag();
        mouse = new MouseController(backend, new ScreenController(new Rectangle(0, 0, 100, 50)),
                cancellation, 0);
    }

    @Test
    void movesPointerWithinScreen() {
        mouse.moveTo(10, 20);

        assertEquals(new Point(10, 20), backend.position);
    }

    @Test
    void rejectsCoordinatesOutsideScreen() {
        assertThrows(AutomationException.class, () -> mouse.moveTo(100, 20));
        assertThrows(AutomationException.class, () -> mouse.moveTo(10, -1));
        assertTrue(backend.events.isEmpty());
    }

    @Test
    void singleClickPressesAndReleasesLeftButton() {
        mouse.click(5, 5);

        assertEquals("move:5,5", backend.events.get(0));
        assertEquals("press:" + InputEvent.BUTTON1_DOWN_MASK, backend.events.get(1));
        assertEquals("release:" + InputEvent.BUTTON1_DOWN_MASK, backend.events.get(2));
        assertTrue(backend.pressedButtons.isEmpty());
    }

    @Test
    void doubleClickClicksTwice() {
        mouse.doubleClick(5, 5);

        long clicks = backend.events.stream().filter(e -> e.startsWith("press:")).count();
        assertEquals(2, clicks);
        assertTrue(backend.pressedButtons.isEmpty());
    }

    @Test
    void rightClickUsesRightButton() {
        mouse.rightClick(5, 5);

        assertTrue(backend.events.contains("press:" + InputEvent.BUTTON3_DOWN_MASK));
        assertTrue(backend.events.contains("release:" + InputEvent.BUTTON3_DOWN_MASK));
    }

    @Test
    void dragEndsAtTargetAndReleasesButton() {
        mouse.drag(0, 0, 30, 30);

        assertEquals(new Point(30, 30), backend.position);
        assertTrue(backend.pressedButtons.isEmpty());
        assertEquals("press:" + InputEvent.BUTTON1_DOWN_MASK, backend.events.get(1));
    }

    @Test
    void dragReleasesButtonWhenTheBackendFails() {
        backend.failOnMouseMove(2);

        assertThrows(AutomationException.class, () -> mouse.drag(0, 0, 30, 30));
        assertTrue(backend.pressedButtons.isEmpty());
    }

    @Test
    void cancelledControllerPerformsNoAction() {
        cancellation.cancel();

        assertThrows(AutomationCancelledException.class, () -> mouse.click(5, 5));
        assertTrue(backend.events.isEmpty());
    }

    @Test
    void scrollingRequiresNonZeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> mouse.scroll(0));

        mouse.scroll(3);
        assertTrue(backend.events.contains("wheel:3"));
    }

    @Test
    void reportsWhetherCoordinatesAreUsable() {
        assertTrue(mouse.isWithinBounds(0, 0));
        assertTrue(mouse.isWithinBounds(99, 49));
        assertFalse(mouse.isWithinBounds(100, 49));
    }
}
