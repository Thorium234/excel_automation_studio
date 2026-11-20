package com.excelstudio.controller;

import com.excelstudio.ui.View;
import com.excelstudio.ui.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Controls the application shell: navigation and the shared status bar.
 */
public class DashboardController {
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Label statusLabel;

    private ViewNavigator navigator;

    @FXML
    private void initialize() {
        navigator = new ViewNavigator(contentArea);
        showHome();
    }

    @FXML
    private void showHome() {
        open(View.HOME);
    }

    @FXML
    private void showExcel() {
        open(View.EXCEL);
    }

    @FXML
    private void showSettings() {
        open(View.SETTINGS);
    }

    @FXML
    private void showHistory() {
        open(View.HISTORY);
    }

    void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void open(View view) {
        navigator.show(view);
        pageTitle.setText(view.title());
        if (view == View.HOME) {
            setStatus("Ready");
        } else {
            setStatus(view.title() + " screen");
        }
    }
}
