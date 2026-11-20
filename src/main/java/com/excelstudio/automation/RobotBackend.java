package com.excelstudio.automation;

import java.awt.Point;

/**
 * Minimal operating-system input surface used by the mouse and keyboard controllers.
 *
 * <p>Keeping this as an interface allows the controllers to be unit tested with a
 * recording fake instead of moving the real pointer.
 */
public interface RobotBackend {
    void mouseMove(int x, int y);

    void mousePress(int buttons);

    void mouseRelease(int buttons);

    void mouseWheel(int wheelAmount);

    Point mousePosition();

    void keyPress(int keyCode);

    void keyRelease(int keyCode);
}
