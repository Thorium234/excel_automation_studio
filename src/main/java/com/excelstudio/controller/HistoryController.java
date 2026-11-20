package com.excelstudio.controller;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.excelstudio.app.AppServices;
import com.excelstudio.ui.Refreshable;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

/**
 * Reviews previous sessions and their saved Markdown reports.
 */
public class HistoryController implements Refreshable {
    @FXML private ListView<Path> reportList;
    @FXML private TextArea reportPreview;
    @FXML private Label feedback;

    private AppServices services;

    @FXML
    private void initialize() {
        services = AppServices.get();
        reportList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showReport(newValue));
        reportPreview.setText("Select a report to review it.");
        feedback.setText("");
        refresh();
    }

    @FXML
    @Override
    public void refresh() {
        List<Path> reports = services.reports();
        reportList.setItems(FXCollections.observableArrayList(reports));
        if (reports.isEmpty()) {
            feedback.setText("No reports yet. Complete or stop a session to create one.");
            reportPreview.setText("No reports yet.");
        } else {
            feedback.setText(reports.size() + " report(s) in " + services.reportsDirectory());
            reportList.getSelectionModel().selectFirst();
        }
    }

    private void showReport(Path report) {
        if (report == null) {
            return;
        }
        try {
            reportPreview.setText(services.readReport(report));
            feedback.setText(report.toAbsolutePath().toString());
        } catch (Exception e) {
            reportPreview.setText("Unable to read " + report + ": " + e.getMessage());
        }
    }

    @FXML
    private void openReportsFolder() {
        Path directory = services.reportsDirectory();
        try {
            if (!java.nio.file.Files.isDirectory(directory)) {
                java.nio.file.Files.createDirectories(directory);
            }
            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                feedback.setText("This desktop cannot open folders.");
                return;
            }
            Desktop.getDesktop().open(new File(directory.toAbsolutePath().toString()));
        } catch (Exception e) {
            feedback.setText("Unable to open " + directory + ": " + e.getMessage());
        }
    }
}
