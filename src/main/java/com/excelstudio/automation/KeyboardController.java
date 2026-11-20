package com.excelstudio.automation;

import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Performs controlled keyboard operations. Contains no spreadsheet-specific logic.
 */
public final class KeyboardController {
    private static final int DEFAULT_KEY_DELAY_MILLIS = 12;
    private static final String SHIFTED_CHARACTERS = "~!@#$%^&*()_+{}|:\"<>?";

    private final RobotBackend robot;
    private final CancellationToken cancellation;
    private final int keyDelayMillis;
    private final Set<Integer> pressedKeys = new LinkedHashSet<>();

    public KeyboardController(RobotBackend robot, CancellationToken cancellation, int keyDelayMillis) {
        this.robot = robot;
        this.cancellation = cancellation;
        this.keyDelayMillis = Math.max(0, keyDelayMillis);
    }

    public static KeyboardController createDefault(CancellationToken cancellation) {
        return new KeyboardController(new AwtRobotBackend(), cancellation, DEFAULT_KEY_DELAY_MILLIS);
    }

    public synchronized void typeText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        ensureActive();
        for (int index = 0; index < text.length(); index++) {
            ensureActive();
            typeCharacter(text.charAt(index));
            pause(keyDelayMillis);
        }
    }

    public synchronized void pressKey(int keyCode) {
        ensureActive();
        if (pressedKeys.contains(keyCode)) {
            return;
        }
        robot.keyPress(keyCode);
        pressedKeys.add(keyCode);
    }

    public synchronized void releaseKey(int keyCode) {
        robot.keyRelease(keyCode);
        pressedKeys.remove(keyCode);
    }

    public synchronized void shortcut(int... keyCodes) {
        ensureActive();
        if (keyCodes == null || keyCodes.length < 2) {
            throw new IllegalArgumentException("A shortcut needs at least two keys");
        }
        int applied = 0;
        try {
            for (int keyCode : keyCodes) {
                pressKey(keyCode);
                applied++;
            }
        } finally {
            releaseLast(applied, keyCodes);
        }
    }

    public void pressEnter() {
        tap(KeyEvent.VK_ENTER);
    }

    public void pressTab() {
        tap(KeyEvent.VK_TAB);
    }

    public void pressEscape() {
        tap(KeyEvent.VK_ESCAPE);
    }

    public void pressDelete() {
        tap(KeyEvent.VK_DELETE);
    }

    public void pressBackspace() {
        tap(KeyEvent.VK_BACK_SPACE);
    }

    public void pressArrow(int keyCode, int times) {
        if (times < 0) {
            throw new IllegalArgumentException("Repeat count must not be negative");
        }
        for (int i = 0; i < times; i++) {
            tap(keyCode);
        }
    }

    public synchronized void tap(int keyCode) {
        pressKey(keyCode);
        releaseKey(keyCode);
        pause(keyDelayMillis);
    }

    public synchronized boolean hasPressedKeys() {
        return !pressedKeys.isEmpty();
    }

    /**
     * Releases every key this controller pressed, for example after a failure.
     */
    public synchronized void releaseAll() {
        for (Integer keyCode : Set.copyOf(pressedKeys)) {
            robot.keyRelease(keyCode);
        }
        pressedKeys.clear();
    }

    private void typeCharacter(char character) {
        if (character == '\n') {
            tap(KeyEvent.VK_ENTER);
            return;
        }
        if (character == '\t') {
            tap(KeyEvent.VK_TAB);
            return;
        }

        int keyCode = KeyEvent.getExtendedKeyCodeForChar(character);
        if (keyCode == KeyEvent.VK_UNDEFINED || keyCode == 0) {
            releaseAll();
            throw new AutomationException("Unsupported character: '" + character + "'");
        }

        boolean shift = Character.isUpperCase(character) || SHIFTED_CHARACTERS.indexOf(character) >= 0;
        if (shift) {
            pressKey(KeyEvent.VK_SHIFT);
        }
        try {
            tap(keyCode);
        } finally {
            if (shift) {
                releaseKey(KeyEvent.VK_SHIFT);
            }
        }
    }

    private void releaseLast(int applied, int[] keyCodes) {
        for (int index = applied - 1; index >= 0; index--) {
            releaseKey(keyCodes[index]);
        }
    }

    private void ensureActive() {
        if (cancellation.isCancelled()) {
            releaseAll();
            throw new AutomationCancelledException("Keyboard operation cancelled");
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
            throw new AutomationException("Keyboard operation interrupted", e);
        }
    }
}
