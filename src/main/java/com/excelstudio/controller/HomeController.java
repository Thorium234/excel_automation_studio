package com.excelstudio.controller;

import java.time.Duration;
import java.util.List;

import com.excelstudio.app.AppServices;
import com.excelstudio.session.SessionConfig;
import com.excelstudio.session.SessionManager;
import com.excelstudio.session.SessionState;
import com.excelstudio.session.SessionStats;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

/**
 * Session overview and controls shown on the dashboard.
 *
 * <p>The timer only reads state; all lifecycle changes go through
 * {@link SessionManager} so the UI can never invent progress.
 */
public class HomeController {
    @FXML private ComboBox<String> durationPreset;
    @FXML private TextField customHours;
    @FXML private TextField customMinutes;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button stopButton;
    @FXML private Button completeButton;
    @FXML private Label durationValue;
    @FXML private Label taskValue;
    @FXML private Label completedValue;
    @FXML private Label stateValue;
    @FXML private Label elapsedValue;
    @FXML private Label remainingValue;
    @FXML private Label feedback;
    @FXML private ProgressBar sessionProgress;
    @FXML private ListView<String> logList;

    private static final String CUSTOM = "Custom";

    private AppServices services;
    private Timeline timer;

    @FXML
    private void initialize() {
        services = AppServices.get();

        durationPreset.setItems(FXCollections.observableArrayList(labels()));
        String initialLabel = defaultLabel();
        durationPreset.getSelectionModel().select(initialLabel);
        durationPreset.valueProperty().addListener((observable, oldValue, newValue) -> updateCustomFields());

        int defaultMinutes = services.config().getDefaultSessionMinutes();
        if (CUSTOM.equals(initialLabel)) {
            customHours.setText(Long.toString(defaultMinutes / 60));
            customMinutes.setText(Long.toString(defaultMinutes % 60));
        } else {
            customHours.setText("0");
            customMinutes.setText("30");
        }
        updateCustomFields();
        updateCustomFieldState();

        refresh();
        timer = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> refresh()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    @FXML
    private void startSession() {
        try {
            SessionConfig config = selectedConfig();
            services.startSession(config);
            feedback.setText("Session started for " + config.display() + ".");
        } catch (RuntimeException e) {
            feedback.setText(e.getMessage());
        }
        refresh();
    }

    @FXML
    private void pauseSession() {
        runSessionAction("Session paused.", SessionManager::pause);
    }

    @FXML
    private void resumeSession() {
        runSessionAction("Session resumed.", SessionManager::resume);
    }

    @FXML
    private void stopSession() {
        finishSession(false);
    }

    @FXML
    private void completeSession() {
        finishSession(true);
    }

    private void finishSession(boolean complete) {
        try {
            var report = services.finishSession(complete);
            feedback.setText("Session saved. Report: " + report);
        } catch (IllegalStateException e) {
            feedback.setText("No session is running.");
        } catch (Exception e) {
            feedback.setText("Session could not be saved: " + e.getMessage());
        }
        refresh();
    }

    private interface SessionAction {
        void run(SessionManager session);
    }

    private void runSessionAction(String message, SessionAction action) {
        SessionManager session = services.session();
        if (session == null) {
            feedback.setText("No session has been started.");
            return;
        }
        try {
            action.run(session);
            feedback.setText(message);
        } catch (IllegalStateException e) {
            feedback.setText(e.getMessage());
        }
        refresh();
    }

    private void refresh() {
        SessionManager session = services.session();
        if (session == null) {
            durationValue.setText("Not configured");
            taskValue.setText(services.currentTask());
            completedValue.setText("0");
            stateValue.setText(SessionState.IDLE.name());
            elapsedValue.setText("00:00:00");
            remainingValue.setText("00:00:00");
            sessionProgress.setProgress(0);
        } else {
            SessionStats stats = session.stats();
            durationValue.setText(session.config().display());
            taskValue.setText(services.currentTask());
            completedValue.setText(Integer.toString(stats.completed()));
            stateValue.setText(stats.state().name());
            elapsedValue.setText(stats.elapsedText());
            remainingValue.setText(stats.remainingText());
            sessionProgress.setProgress(stats.progress(session.config().duration()));
        }

        SessionState state = session == null ? SessionState.IDLE : session.state();
        boolean active = state == SessionState.RUNNING || state == SessionState.PAUSED;
        startButton.setDisable(active);
        pauseButton.setDisable(state != SessionState.RUNNING);
        resumeButton.setDisable(state != SessionState.PAUSED);
        stopButton.setDisable(!active);
        completeButton.setDisable(!active);
        durationPreset.setDisable(active);
        customHours.setDisable(active || !isCustom());
        customMinutes.setDisable(active || !isCustom());

        List<String> entries = new java.util.ArrayList<>(services.log().entries());
        java.util.Collections.reverse(entries);
        logList.setItems(FXCollections.observableArrayList(entries));
    }

    private SessionConfig selectedConfig() {
        String selected = durationPreset.getValue();
        if (CUSTOM.equals(selected)) {
            return SessionConfig.custom(parse(customHours.getText(), "hours"),
                    parse(customMinutes.getText(), "minutes"));
        }
        if (selected == null) {
            return SessionConfig.Preset.THIRTY_MINUTES.config();
        }
        for (SessionConfig.Preset preset : SessionConfig.Preset.values()) {
            if (preset.label().equals(selected)) {
                return preset.config();
            }
        }
        return SessionConfig.Preset.THIRTY_MINUTES.config();
    }

    private static int parse(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Custom " + field + " must be a whole number.");
        }
    }

    private String defaultLabel() {
        int minutes = services.config().getDefaultSessionMinutes();
        if (minutes == 30) {
            return SessionConfig.Preset.THIRTY_MINUTES.label();
        }
        for (SessionConfig.Preset preset : SessionConfig.Preset.values()) {
            if (preset.config().duration().toMinutes() == minutes) {
                return preset.label();
            }
        }
        return CUSTOM;
    }

    private static List<String> labels() {
        List<String> labels = new java.util.ArrayList<>();
        for (SessionConfig.Preset preset : SessionConfig.Preset.values()) {
            labels.add(preset.label());
        }
        labels.add(CUSTOM);
        return labels;
    }

    private boolean isCustom() {
        return CUSTOM.equals(durationPreset.getValue());
    }

    private void updateCustomFields() {
        if (!isCustom()) {
            SessionConfig config = selectedConfig();
            Duration duration = config.duration();
            customHours.setText(Long.toString(duration.toHours()));
            customMinutes.setText(Long.toString(duration.toMinutes() % 60));
        }
        updateCustomFieldState();
    }

    private void updateCustomFieldState() {
        boolean custom = !isCustom();
        customHours.setDisable(custom);
        customMinutes.setDisable(custom);
    }

    @FXML
    private void refreshLog() {
        refresh();
    }

    /**
     * Kept for callers that want to push a snapshot explicitly.
     */
    public void showSession(String duration, String currentTask, int completedTasks, String state) {
        durationValue.setText(duration);
        taskValue.setText(currentTask);
        completedValue.setText(Integer.toString(completedTasks));
        stateValue.setText(state);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }
}
