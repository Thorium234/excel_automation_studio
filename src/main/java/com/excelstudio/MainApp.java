package com.excelstudio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.info("Starting Excel Automation Studio");
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/dashboard.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(MainApp.class.getResource("/css/app.css").toExternalForm());

        stage.setTitle("Excel Automation Studio");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Excel Automation Studio");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
