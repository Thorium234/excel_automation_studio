package com.excelstudio.excel;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import com.excelstudio.automation.AutomationException;
import com.excelstudio.automation.DesktopController;
import com.excelstudio.automation.FakeRobotBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelDesktopAutomationTest {
    private FakeRobotBackend backend;
    private ExcelDesktopAutomation automation;

    @BeforeEach
    void setUp() {
        backend = new FakeRobotBackend();
        DesktopController desktop = new DesktopController(backend,
                new com.excelstudio.automation.ScreenController(new Rectangle(0, 0, 1920, 1080)), 0, 0);
        automation = new ExcelDesktopAutomation(desktop);
    }

    @Test
    void navigatesWithTheNameBox() {
        automation.goTo("d5");

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_CONTROL));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_G));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_D));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_5));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ENTER));
        assertTrue(backend.pressedKeys.isEmpty());
    }

    @Test
    void acceptsRangeReferences() {
        automation.selectRange("A1:F10");

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_A));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_SHIFT));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ENTER));
    }

    @Test
    void rejectsArbitraryTextBeforeSendingKeys() {
        assertThrows(AutomationException.class, () -> automation.goTo("D5; shutdown /s"));
        assertThrows(AutomationException.class, () -> automation.goTo(""));
        assertThrows(AutomationException.class, () -> automation.goTo(null));
        assertTrue(backend.events.isEmpty());
    }

    @Test
    void typesAValueAndConfirmsWithEnter() {
        automation.typeValue("Ready 42");

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ENTER));
        assertTrue(backend.pressedKeys.isEmpty());
    }

    @Test
    void appliesBoldAndSaves() {
        automation.bold();
        automation.save();

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_B));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_S));
    }

    @Test
    void resizesColumnsThroughTheRibbonSequence() {
        automation.setColumnWidth(22);

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_ALT));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_H));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_O));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_W));
        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_2));
        assertTrue(backend.pressedKeys.isEmpty());
    }

    @Test
    void rejectsNonPositiveSizes() {
        assertThrows(IllegalArgumentException.class, () -> automation.setColumnWidth(0));
        assertThrows(IllegalArgumentException.class, () -> automation.setRowHeight(-1));
    }

    @Test
    void clearingCellsSendsDelete() {
        automation.clearCells();

        assertTrue(backend.events.contains("keyPress:" + KeyEvent.VK_DELETE));
    }
}
