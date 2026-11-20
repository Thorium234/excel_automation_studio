package com.excelstudio.automation;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

/**
 * {@link RobotBackend} backed by {@link java.awt.Robot}.
 */
public final class AwtRobotBackend implements RobotBackend {
    private static final int AUTO_DELAY_MILLIS = 15;

    private final Robot robot;

    public AwtRobotBackend() {
        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(AUTO_DELAY_MILLIS);
        } catch (AWTException | IllegalArgumentException e) {
            throw new AutomationException("Unable to create the desktop robot", e);
        }
    }

    @Override
    public void mouseMove(int x, int y) {
        robot.mouseMove(x, y);
    }

    @Override
    public void mousePress(int buttons) {
        robot.mousePress(buttons);
    }

    @Override
    public void mouseRelease(int buttons) {
        robot.mouseRelease(buttons);
    }

    @Override
    public void mouseWheel(int wheelAmount) {
        robot.mouseWheel(wheelAmount);
    }

    @Override
    public Point mousePosition() {
        Point location = MouseInfo.getPointerInfo() == null ? null : MouseInfo.getPointerInfo().getLocation();
        return location == null ? new Point(0, 0) : location;
    }

    @Override
    public void keyPress(int keyCode) {
        robot.keyPress(keyCode);
    }

    @Override
    public void keyRelease(int keyCode) {
        robot.keyRelease(keyCode);
    }
}
