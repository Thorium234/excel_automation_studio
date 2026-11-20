package com.excelstudio.config;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void providesSensibleDefaults() {
        AppConfig config = AppConfig.load(tempDir.resolve("missing.properties"));

        assertEquals(Path.of("data/workbooks"), config.getOutputDirectory());
        assertEquals("Excel", config.getSpreadsheetApplication());
        assertEquals(30, config.getDefaultSessionMinutes());
        assertEquals("INFO", config.getLogLevel());
        assertEquals("ESC", config.getEmergencyStopKey());
    }

    @Test
    void roundTripsValues() throws Exception {
        Path file = tempDir.resolve("config.properties");
        AppConfig config = AppConfig.load(file);

        config.setOutputDirectory(tempDir.resolve("out"));
        config.setSpreadsheetApplication("LibreOffice Calc");
        config.setDefaultSessionMinutes(120);
        config.setLogLevel("debug");
        config.setEmergencyStopKey("F12");
        config.save();

        AppConfig reloaded = AppConfig.load(file);
        assertEquals(tempDir.resolve("out"), reloaded.getOutputDirectory());
        assertEquals("LibreOffice Calc", reloaded.getSpreadsheetApplication());
        assertEquals(120, reloaded.getDefaultSessionMinutes());
        assertEquals("DEBUG", reloaded.getLogLevel());
        assertEquals("F12", reloaded.getEmergencyStopKey());
    }

    @Test
    void rejectsInvalidValues() {
        AppConfig config = AppConfig.load(tempDir.resolve("config.properties"));

        assertThrows(IllegalArgumentException.class, () -> config.setDefaultSessionMinutes(0));
        assertThrows(IllegalArgumentException.class, () -> config.setEmergencyStopKey("  "));
        assertThrows(IllegalArgumentException.class, () -> config.setSpreadsheetApplication(null));
    }

    @Test
    void fallsBackWhenStoredDurationIsCorrupt() throws Exception {
        Path file = tempDir.resolve("config.properties");
        AppConfig config = AppConfig.load(file);
        config.setDefaultSessionMinutes(45);
        config.save();

        String contents = java.nio.file.Files.readString(file).replace("45", "not-a-number");
        java.nio.file.Files.writeString(file, contents);

        assertEquals(30, AppConfig.load(file).getDefaultSessionMinutes());
        assertTrue(java.nio.file.Files.isRegularFile(file));
    }
}
