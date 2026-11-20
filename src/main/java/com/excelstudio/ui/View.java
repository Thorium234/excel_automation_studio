package com.excelstudio.ui;

/**
 * Top-level screens available from the dashboard shell.
 */
public enum View {
    HOME("Dashboard", "/fxml/home.fxml"),
    EXCEL("Excel", "/fxml/excel.fxml"),
    HISTORY("History", "/fxml/history.fxml"),
    SETTINGS("Settings", "/fxml/settings.fxml");

    private final String title;
    private final String fxmlPath;

    View(String title, String fxmlPath) {
        this.title = title;
        this.fxmlPath = fxmlPath;
    }

    public String title() {
        return title;
    }

    public String fxmlPath() {
        return fxmlPath;
    }
}
