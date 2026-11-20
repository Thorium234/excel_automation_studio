package com.excelstudio.controller;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import com.excelstudio.app.AppServices;
import com.excelstudio.excel.ExcelTask;
import com.excelstudio.excel.PoiTaskValidator;
import com.excelstudio.model.TaskCategory;
import com.excelstudio.model.TaskDifficulty;
import com.excelstudio.model.TaskResult;
import com.excelstudio.model.ValidationResult;
import com.excelstudio.session.SessionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

/**
 * Excel practice screen: choose a category, generate a task, run it in a practice
 * workbook on a background thread and show the independent validation result.
 *
 * <p>All workbook work happens off the JavaFX thread. The screen never claims a task
 * passed: it displays the verdict produced by {@link PoiTaskValidator}.
 */
public class ExcelController {
    @FXML private ComboBox<TaskCategory> categoryCombo;
    @FXML private ComboBox<TaskDifficulty> difficultyCombo;
    @FXML private Button generateButton;
    @FXML private Button runButton;
    @FXML private Button openButton;
    @FXML private Button validateButton;
    @FXML private Label taskName;
    @FXML private Label taskDescription;
    @FXML private Label taskMeta;
    @FXML private Label executionStatus;
    @FXML private Label workbookPath;
    @FXML private Label sessionState;
    @FXML private Label feedback;
    @FXML private ListView<String> stepsList;
    @FXML private TextArea validationOutput;

    private AppServices services;
    private ExcelTask currentTask;
    private Path currentWorkbook;
    private boolean running;
    private boolean validating;
    private Timeline timer;

    @FXML
    private void initialize() {
        services = AppServices.get();

        categoryCombo.setItems(FXCollections.observableArrayList(TaskCategory.values()));
        categoryCombo.setConverter(converter(TaskCategory.values(), TaskCategory::displayName));
        categoryCombo.getSelectionModel().selectFirst();

        difficultyCombo.setItems(FXCollections.observableArrayList(TaskDifficulty.values()));
        difficultyCombo.setConverter(converter(TaskDifficulty.values(), TaskDifficulty::displayName));
        difficultyCombo.getSelectionModel().select(TaskDifficulty.BASIC);

        stepsList.setItems(FXCollections.observableArrayList());
        executionStatus.setText("No task has been run.");
        validationOutput.setText("Validation results appear here after a run.");
        workbookPath.setText("No workbook yet.");
        feedback.setText("");
        updateSessionState();
        updateButtons();

        timer = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> updateSessionState()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    @FXML
    private void generateTask() {
        if (running) {
            return;
        }
        TaskCategory category = categoryCombo.getValue();
        TaskDifficulty difficulty = difficultyCombo.getValue();
        if (category == null || difficulty == null) {
            feedback.setText("Choose a category and a difficulty first.");
            return;
        }

        currentTask = services.generator().generate(category, difficulty);
        currentWorkbook = null;
        services.setCurrentTask(currentTask.name());
        services.setCurrentWorkbook(null);

        taskName.setText(currentTask.name());
        taskDescription.setText(currentTask.description());
        currentTask.dataset().ifPresent(dataset -> taskMeta.setText(
                dataset.rowCount() + " rows x " + dataset.columnCount()
                        + " columns, worksheet '" + currentTask.sheetName() + "'"));
        stepsList.setItems(FXCollections.observableArrayList(currentTask.steps()));
        executionStatus.setText("Task generated. Run it to produce and validate a practice workbook.");
        validationOutput.setText("No validation has run yet.");
        workbookPath.setText("No workbook yet.");
        feedback.setText("");
        services.log().add("Generated task " + currentTask.name());
        updateButtons();
    }

    @FXML
    private void runTask() {
        if (currentTask == null) {
            generateTask();
            if (currentTask == null) {
                return;
            }
        }
        SessionManager session = services.session();
        if (session == null || !session.isRunning()) {
            feedback.setText("Start a session on the Dashboard before running a task.");
            return;
        }

        ExcelTask task = currentTask;
        running = true;
        runButton.setDisable(true);
        generateButton.setDisable(true);
        categoryCombo.setDisable(true);
        difficultyCombo.setDisable(true);
        executionStatus.setText("Running " + task.name() + "...");
        validationOutput.setText("Waiting for the workbook to be written.");

        services.workers().submit(() -> {
            Path workbook = services.workbooks()
                    .uniquePath(services.outputDirectory(), task.workbookFileName());
            TaskResult result = services.engine().run(task, workbook);
            services.record(result);
            services.setCurrentWorkbook(result.workbook());
            Platform.runLater(() -> showResult(task, result));
        });
    }

    private void showResult(ExcelTask task, TaskResult result) {
        running = false;
        currentWorkbook = result.workbook();
        executionStatus.setText(result.succeeded()
                ? "PASSED - every validation rule was satisfied."
                : "FAILED - the workbook did not satisfy every rule.");
        validationOutput.setText(result.validation() == null
                ? String.valueOf(result.error())
                : result.validation().summary());
        workbookPath.setText(result.workbook() == null ? "No workbook."
                : result.workbook().toAbsolutePath().toString());
        feedback.setText(result.error() == null ? "" : result.error());
        runButton.setDisable(false);
        generateButton.setDisable(false);
        categoryCombo.setDisable(false);
        difficultyCombo.setDisable(false);
        updateButtons();
    }

    @FXML
    private void openWorkbook() {
        if (currentWorkbook == null) {
            feedback.setText("Run a task first so a workbook exists.");
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            feedback.setText("This desktop cannot open files in a spreadsheet application.");
            return;
        }
        try {
            Desktop.getDesktop().open(new File(currentWorkbook.toAbsolutePath().toString()));
            feedback.setText("Opened " + currentWorkbook);
        } catch (Exception e) {
            feedback.setText("Unable to open the workbook: " + e.getMessage());
        }
    }

    @FXML
    private void validateWorkbook() {
        if (currentTask == null || currentWorkbook == null) {
            feedback.setText("Run a task first so there is a saved workbook to validate.");
            return;
        }
        ExcelTask task = currentTask;
        Path workbook = currentWorkbook;
        validating = true;
        validateButton.setDisable(true);
        validationOutput.setText("Re-validating " + workbook.getFileName() + "...");

        services.workers().submit(() -> {
            ValidationResult result = new PoiTaskValidator(services.workbooks()).validate(task, workbook);
            Platform.runLater(() -> {
                validating = false;
                validationOutput.setText(result.summary());
                executionStatus.setText(result.isPassed()
                        ? "PASSED - every validation rule was satisfied."
                        : "FAILED - the saved workbook no longer satisfies every rule.");
                updateButtons();
            });
        });
    }

    private void updateSessionState() {
        SessionManager session = services.session();
        if (session == null) {
            sessionState.setText("IDLE - start a session on the Dashboard");
        } else {
            sessionState.setText(session.state() + " - " + session.stats().elapsedText()
                    + " elapsed, " + session.stats().remainingText() + " remaining");
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean busy = running || validating;
        openButton.setDisable(currentWorkbook == null || busy);
        validateButton.setDisable(currentWorkbook == null || busy);
        runButton.setDisable(currentTask == null || busy);
        generateButton.setDisable(busy);
        categoryCombo.setDisable(running);
        difficultyCombo.setDisable(running);
    }

    private static <T> StringConverter<T> converter(T[] values,
                                                    java.util.function.Function<T, String> display) {
        return new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : display.apply(value);
            }

            @Override
            public T fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                String wanted = text.trim().toLowerCase(Locale.ROOT);
                for (T value : values) {
                    if (display.apply(value).toLowerCase(Locale.ROOT).equals(wanted)) {
                        return value;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Exposed for tests: the task currently displayed, if any.
     */
    ExcelTask currentTask() {
        return currentTask;
    }

    /**
     * Exposed for tests: the workbook produced by the last run, if any.
     */
    Path currentWorkbook() {
        return currentWorkbook;
    }

    List<String> steps() {
        return stepsList.getItems();
    }
}
