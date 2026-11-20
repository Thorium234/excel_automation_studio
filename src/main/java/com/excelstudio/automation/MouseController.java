package com.excelstudio.automation;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Performs controlled mouse operations. Contains no spreadsheet-specific logic.
 */
public final class MouseController {
    private static final int DEFAULT_STEP_DELAY_MILLIS = 25;
    private static final int DRAG_STEPS = 12;

    private final RobotBackend robot;
    private final ScreenController screen;
    private final CancellationToken cancellation;
    private final int stepDelayMillis;
    private final Set<Integer> pressedButtons = new LinkedHashSet<>();

    public MouseController(RobotBackend robot, ScreenController screen,
                           CancellationToken cancellation, int stepDelayMillis) {
        this.robot = robot;
        this.screen = screen;
        this.cancellation = cancellation;
        this.stepDelayMillis = Math.max(0, stepDelayMillis);
    }

    public static MouseController createDefault(CancellationToken cancellation) {
        return new MouseController(new AwtRobotBackend(), ScreenController.primaryScreen(),
                cancellation, DEFAULT_STEP_DELAY_MILLIS);
    }

    public ScreenController screen() {
        return screen;
    }

    public Point position() {
        ensureUsable();
        return robot.mousePosition();
    }

    public boolean isWithinBounds(int x, int y) {
        return screen.contains(x, y);
    }

    public void moveTo(int x, int y) {
        ensureUsable();
        screen.requireWithin(x, y);
        robot.mouseMove(x, y);
    }

    public void click(int x, int y) {
        click(x, y, InputEvent.BUTTON1_DOWN_MASK);
    }

    public void rightClick(int x, int y) {
        click(x, y, InputEvent.BUTTON3_DOWN_MASK);
    }

    public void doubleClick(int x, int y) {
        ensureUsable();
        click(x, y);
        pause(stepDelayMillis);
        click(x, y);
    }

    public void click(int x, int y, int buttonMask) {
        moveTo(x, y);
        press(buttonMask);
        try {
            pause(stepDelayMillis);
        } finally {
            release(buttonMask);
        }
    }

    public void drag(int fromX, int fromY, int toX, int toY) {
        ensureUsable();
        screen.requireWithin(fromX, fromY);
        screen.requireWithin(toX, toY);

        robot.mouseMove(fromX, fromY);
        int mask = InputEvent.BUTTON1_DOWN_MASK;
        press(mask);
        try {
            for (int step = 1; step <= DRAG_STEPS; step++) {
                ensureActive();
                int x = fromX + (toX - fromX) * step / DRAG_STEPS;
                int y = fromY + (toY - fromY) * step / DRAG_STEPS;
                robot.mouseMove(x, y);
                pause(stepDelayMillis);
            }
        } finally {
            release(mask);
        }
    }

    public void scroll(int wheelAmount) {
        ensureUsable();
        if (wheelAmount == 0) {
            throw new IllegalArgumentException("Scroll amount must not be zero");
        }
        robot.mouseWheel(wheelAmount);
    }

    /**
     * Releases every button this controller pressed, for example after a failure.
     */
    public void releaseAll() {
        for (Integer mask : Set.copyOf(pressedButtons)) {
            release(mask);
        }
    }

    private void press(int mask) {
        ensureActive();
        robot.mousePress(mask);
        pressedButtons.add(mask);
    }

    private void release(int mask) {
        robot.mouseRelease(mask);
        pressedButtons.remove(mask);
    }

    private void ensureUsable() {
        screen.bounds();
        ensureActive();
    }

    private void ensureActive() {
        if (cancellation.isCancelled()) {
            releaseAll();
            throw new AutomationCancelledException("Mouse operation cancelled");
        }
    }

    private void pause(int millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            releaseAll();
            throw new AutomationException("Mouse operation interrupted", e);
        }
    }
}
