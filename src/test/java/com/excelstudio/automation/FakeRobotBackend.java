package com.excelstudio.automation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Recording {@link RobotBackend} used by the automation tests.
 */
public final class FakeRobotBackend implements RobotBackend {
    public final List<String> events = new ArrayList<>();
    public final Set<Integer> pressedButtons = new LinkedHashSet<>();
    public final Set<Integer> pressedKeys = new LinkedHashSet<>();
    public Point position = new Point(0, 0);

    private int failOnMouseMoveNumber = -1;
    private int mouseMoves;

    public void failOnMouseMove(int number) {
        this.failOnMouseMoveNumber = number;
    }

    @Override
    public void mouseMove(int x, int y) {
        mouseMoves++;
        if (mouseMoves == failOnMouseMoveNumber) {
            throw new AutomationException("simulated mouse failure");
        }
        position = new Point(x, y);
        events.add("move:" + x + "," + y);
    }

    @Override
    public void mousePress(int buttons) {
        pressedButtons.add(buttons);
        events.add("press:" + buttons);
    }

    @Override
    public void mouseRelease(int buttons) {
        pressedButtons.remove(buttons);
        events.add("release:" + buttons);
    }

    @Override
    public void mouseWheel(int wheelAmount) {
        events.add("wheel:" + wheelAmount);
    }

    @Override
    public Point mousePosition() {
        return new Point(position);
    }

    @Override
    public void keyPress(int keyCode) {
        pressedKeys.add(keyCode);
        events.add("keyPress:" + keyCode);
    }

    @Override
    public void keyRelease(int keyCode) {
        pressedKeys.remove(keyCode);
        events.add("keyRelease:" + keyCode);
    }
}
