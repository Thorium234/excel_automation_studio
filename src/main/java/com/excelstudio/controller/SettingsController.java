package com.excelstudio.controller;

import java.io.IOException;
import java.nio.file.Path;

import com.excelstudio.app.AppServices;
import com.excelstudio.config.AppConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Settings screen backed by a local properties file.
 */
public class SettingsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);

    @FXML private TextField outputDirectory;
    @FXML private TextField spreadsheetApplication;
    @FXML private TextField defaultSessionMinutes;
    @FXML private TextField emergencyStopKey;
    @FXML private Label feedback;

    private AppConfig config;

    @FXML
    private void initialize() {
        config = AppServices.get().config();
        outputDirectory.setText(config.getOutputDirectory().toString());
        spreadsheetApplication.setText(config.getSpreadsheetApplication());
        defaultSessionMinutes.setText(Integer.toString(config.getDefaultSessionMinutes()));
        emergencyStopKey.setText(config.getEmergencyStopKey());
        feedback.setText("");
    }

    @FXML
    private void save() {
        try {
            int minutes = Integer.parseInt(defaultSessionMinutes.getText().trim());
            if (minutes <= 0) {
                throw new IllegalArgumentException("Session duration must be a positive number of minutes.");
            }
            if (emergencyStopKey.getText().isBlank()) {
                throw new IllegalArgumentException("Emergency stop key must not be empty.");
            }

            config.setOutputDirectory(Path.of(outputDirectory.getText().trim()));
            config.setSpreadsheetApplication(spreadsheetApplication.getText());
            config.setDefaultSessionMinutes(minutes);
            config.setEmergencyStopKey(emergencyStopKey.getText());
            config.save();

            feedback.setText("Settings saved to " + config.file());
            LOGGER.info("Settings saved to {}", config.file());
        } catch (NumberFormatException e) {
            feedback.setText("Session duration must be a whole number of minutes.");
        } catch (IllegalArgumentException e) {
            feedback.setText(e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Unable to save settings", e);
            feedback.setText("Unable to save settings: " + e.getMessage());
        }
    }
}
