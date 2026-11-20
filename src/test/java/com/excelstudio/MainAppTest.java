package com.excelstudio;

import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainAppTest {
    @Test
    void applicationClassLoads() {
        assertDoesNotThrow(() -> Class.forName("com.excelstudio.MainApp"));
    }

    @Test
    void applicationResourcesArePresent() {
        assertNotNull(MainApp.class.getResource("/fxml/dashboard.fxml"));
        assertNotNull(MainApp.class.getResource("/css/app.css"));
        assertNotNull(MainApp.class.getResource("/logback.xml"));
    }

    @Test
    void stylesheetCanBeOpened() throws Exception {
        URL stylesheet = MainApp.class.getResource("/css/app.css");
        assertNotNull(stylesheet);
        try (var in = stylesheet.openStream()) {
            assertDoesNotThrow(() -> in.readAllBytes());
        }
    }
}
