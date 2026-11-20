package com.excelstudio.automation;

import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenControllerTest {

    @Test
    void containsUsesExclusiveUpperBoundary() {
        ScreenController screen = new ScreenController(new Rectangle(0, 0, 1920, 1080));

        assertTrue(screen.contains(0, 0));
        assertTrue(screen.contains(1919, 1079));
        assertFalse(screen.contains(1920, 500));
        assertFalse(screen.contains(-1, 500));
    }

    @Test
    void offsetsAreRespected() {
        ScreenController screen = new ScreenController(new Rectangle(100, 50, 200, 100));

        assertTrue(screen.contains(100, 50));
        assertFalse(screen.contains(99, 50));
    }

    @Test
    void rejectsEmptyBounds() {
        assertThrows(IllegalArgumentException.class, () -> new ScreenController(new Rectangle(0, 0, 0, 10)));
    }

    @Test
    void requireWithinExplainsRejectedPoints() {
        ScreenController screen = new ScreenController(new Rectangle(0, 0, 10, 10));

        AutomationException error = assertThrows(AutomationException.class, () -> screen.requireWithin(10, 10));
        assertTrue(error.getMessage().contains("10x10"));
    }
}
