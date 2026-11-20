package com.excelstudio.excel;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

import com.excelstudio.automation.AutomationException;
import com.excelstudio.automation.DesktopController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Genuine spreadsheet interaction through keyboard shortcuts.
 *
 * <p>Every method targets the spreadsheet window that currently has focus.
 * Cell and range references are validated before any key is sent so arbitrary text
 * can never be typed into the application by accident.
 */
public final class ExcelDesktopAutomation implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelDesktopAutomation.class);
    private static final Pattern REFERENCE =
            Pattern.compile("^[A-Za-z]{1,3}[1-9][0-9]{0,6}(:[A-Za-z]{1,3}[1-9][0-9]{0,6})?$");

    private final DesktopController desktop;

    public ExcelDesktopAutomation(DesktopController desktop) {
        this.desktop = desktop;
    }

    public void openWorkbook(Path workbook) throws IOException {
        if (!Files.isRegularFile(workbook)) {
            throw new IOException("Workbook does not exist: " + workbook);
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            throw new IOException("This desktop cannot open files in a spreadsheet application");
        }
        LOGGER.info("Opening workbook {}", workbook);
        Desktop.getDesktop().open(workbook.toFile());
    }

    public void goTo(String reference) {
        String normalized = requireReference(reference);
        desktop.keyboard().shortcut(KeyEvent.VK_CONTROL, KeyEvent.VK_G);
        desktop.keyboard().typeText(normalized);
        desktop.keyboard().pressEnter();
    }

    public void selectRange(String reference) {
        goTo(reference);
    }

    public void typeValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        desktop.keyboard().typeText(value);
        desktop.keyboard().pressEnter();
    }

    public void clearCells() {
        desktop.keyboard().pressDelete();
    }

    public void bold() {
        desktop.keyboard().shortcut(KeyEvent.VK_CONTROL, KeyEvent.VK_B);
    }

    public void insertRowAbove() {
        desktop.keyboard().shortcut(KeyEvent.VK_CONTROL, KeyEvent.VK_PLUS);
    }

    public void deleteRow() {
        desktop.keyboard().shortcut(KeyEvent.VK_CONTROL, KeyEvent.VK_MINUS);
    }

    public void setColumnWidth(int characters) {
        requirePositive(characters, "Column width");
        desktop.keyboard().shortcut(KeyEvent.VK_ALT, KeyEvent.VK_H);
        desktop.keyboard().tap(KeyEvent.VK_O);
        desktop.keyboard().tap(KeyEvent.VK_W);
        desktop.keyboard().typeText(Integer.toString(characters));
        desktop.keyboard().pressEnter();
    }

    public void setRowHeight(int points) {
        requirePositive(points, "Row height");
        desktop.keyboard().shortcut(KeyEvent.VK_ALT, KeyEvent.VK_H);
        desktop.keyboard().tap(KeyEvent.VK_O);
        desktop.keyboard().tap(KeyEvent.VK_H);
        desktop.keyboard().typeText(Integer.toString(points));
        desktop.keyboard().pressEnter();
    }

    public void save() {
        desktop.keyboard().shortcut(KeyEvent.VK_CONTROL, KeyEvent.VK_S);
    }

    public void cancel() {
        desktop.cancel();
    }

    @Override
    public void close() {
        desktop.close();
    }

    private static String requireReference(String reference) {
        if (reference == null || !REFERENCE.matcher(reference.trim()).matches()) {
            throw new AutomationException("Not a valid cell or range reference: " + reference);
        }
        return reference.trim().toUpperCase(Locale.ROOT);
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
