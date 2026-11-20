package com.excelstudio.automation;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates mouse and keyboard automation and owns its cancellation state.
 *
 * <p>This class is deliberately free of Excel and JavaFX knowledge so it can be
 * used and tested on its own.
 */
public final class DesktopController implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopController.class);

    private final CancellationFlag cancellation = new CancellationFlag();
    private final ScreenController screen;
    private final MouseController mouse;
    private final KeyboardController keyboard;
    private volatile boolean closed;

    public DesktopController() {
        this(new AwtRobotBackend(), ScreenController.primaryScreen(), 25, 12);
    }

    public DesktopController(RobotBackend backend, ScreenController screen,
                             int mouseDelayMillis, int keyDelayMillis) {
        this.screen = screen;
        this.mouse = new MouseController(backend, screen, cancellation, mouseDelayMillis);
        this.keyboard = new KeyboardController(backend, cancellation, keyDelayMillis);
    }

    public static boolean isDesktopAvailable() {
        return !GraphicsEnvironment.isHeadless();
    }

    public boolean isAvailable() {
        return !closed && isDesktopAvailable();
    }

    public void requireAvailable() {
        if (!isAvailable()) {
            throw new AutomationException("Desktop automation is not available");
        }
    }

    public MouseController mouse() {
        requireAvailable();
        return mouse;
    }

    public KeyboardController keyboard() {
        requireAvailable();
        return keyboard;
    }

    public ScreenController screen() {
        return screen;
    }

    public Rectangle screenBounds() {
        return screen.bounds();
    }

    public boolean isCancelled() {
        return cancellation.isCancelled();
    }

    public void cancel() {
        LOGGER.info("Desktop automation cancelled");
        cancellation.cancel();
        releaseInput();
    }

    public void resetCancellation() {
        cancellation.reset();
    }

    /**
     * Releases all pressed keys and mouse buttons and blocks further operations.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        cancellation.cancel();
        releaseInput();
    }

    private void releaseInput() {
        try {
            keyboard.releaseAll();
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to release pressed keys", e);
        }
        try {
            mouse.releaseAll();
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to release mouse buttons", e);
        }
    }
}
