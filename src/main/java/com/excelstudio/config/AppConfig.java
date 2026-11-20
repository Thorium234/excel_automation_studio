package com.excelstudio.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * User-editable application settings stored in a local properties file.
 */
public final class AppConfig {
    public static final String OUTPUT_DIRECTORY = "output.directory";
    public static final String SPREADSHEET_APPLICATION = "spreadsheet.application";
    public static final String DEFAULT_SESSION_MINUTES = "session.defaultMinutes";
    public static final String LOG_LEVEL = "logging.level";
    public static final String EMERGENCY_STOP_KEY = "automation.emergencyStopKey";

    private static final int DEFAULT_SESSION = 30;

    private final Path file;
    private final Properties properties = new Properties();

    private AppConfig(Path file) {
        this.file = file;
    }

    public static AppConfig loadDefault() {
        return load(defaultFile());
    }

    public static AppConfig load(Path file) {
        AppConfig config = new AppConfig(file);
        if (Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                config.properties.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read configuration " + file, e);
            }
        }
        return config;
    }

    public static Path defaultFile() {
        return Path.of(System.getProperty("user.home"), ".excel-automation-studio", "config.properties");
    }

    public Path getOutputDirectory() {
        return Path.of(get(OUTPUT_DIRECTORY, "data/workbooks"));
    }

    public Path getReportsDirectory() {
        Path output = getOutputDirectory().toAbsolutePath().normalize();
        Path parent = output.getParent();
        return parent == null ? output.resolve("reports") : parent.resolve("reports");
    }

    public void setOutputDirectory(Path directory) {
        set(OUTPUT_DIRECTORY, Objects.requireNonNull(directory, "directory").toString());
    }

    public String getSpreadsheetApplication() {
        return get(SPREADSHEET_APPLICATION, "Excel");
    }

    public void setSpreadsheetApplication(String application) {
        set(SPREADSHEET_APPLICATION, requireText(application, "application"));
    }

    public int getDefaultSessionMinutes() {
        String raw = get(DEFAULT_SESSION_MINUTES, Integer.toString(DEFAULT_SESSION));
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_SESSION;
        }
    }

    public void setDefaultSessionMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Session minutes must be positive");
        }
        set(DEFAULT_SESSION_MINUTES, Integer.toString(minutes));
    }

    public String getLogLevel() {
        return get(LOG_LEVEL, "INFO");
    }

    public void setLogLevel(String level) {
        set(LOG_LEVEL, requireText(level, "level").toUpperCase());
    }

    public String getEmergencyStopKey() {
        return get(EMERGENCY_STOP_KEY, "ESC");
    }

    public void setEmergencyStopKey(String key) {
        set(EMERGENCY_STOP_KEY, requireText(key, "key"));
    }

    public Path file() {
        return file;
    }

    public void save() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream out = Files.newOutputStream(file)) {
            properties.store(out, "Excel Automation Studio settings");
        }
    }

    private String get(String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private void set(String key, String value) {
        properties.setProperty(key, value);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
