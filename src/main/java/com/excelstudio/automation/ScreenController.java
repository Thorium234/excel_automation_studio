package com.excelstudio.automation;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * Knows the usable screen area and rejects coordinates outside it.
 */
public final class ScreenController {
    private final Rectangle bounds;

    public ScreenController(Rectangle bounds) {
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
            throw new IllegalArgumentException("Screen bounds must be positive");
        }
        this.bounds = new Rectangle(bounds);
    }

    public static ScreenController primaryScreen() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new AutomationException("No graphical desktop is available");
        }
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        return new ScreenController(new Rectangle(0, 0, size.width, size.height));
    }

    public Rectangle bounds() {
        return new Rectangle(bounds);
    }

    public boolean contains(int x, int y) {
        return x >= bounds.x && y >= bounds.y
                && x < bounds.x + bounds.width
                && y < bounds.y + bounds.height;
    }

    public void requireWithin(int x, int y) {
        if (!contains(x, y)) {
            throw new AutomationException(
                    "Pointer position (" + x + ", " + y + ") is outside the screen " + describe());
        }
    }

    public String describe() {
        return bounds.width + "x" + bounds.height + " at (" + bounds.x + ", " + bounds.y + ")";
    }
}
