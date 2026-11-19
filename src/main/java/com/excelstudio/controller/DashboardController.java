package com.excelstudio.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {
    @FXML private StackPane contentArea;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("Ready");
    }

    @FXML
    private void showDashboard() {
        statusLabel.setText("Dashboard");
    }

    @FXML
    private void showExcel() {
        statusLabel.setText("Excel module ready for implementation");
    }

    @FXML
    private void showSettings() {
        statusLabel.setText("Settings ready for implementation");
    }
}
