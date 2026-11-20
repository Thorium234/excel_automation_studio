package com.excelstudio;

import java.net.URL;
import java.util.Objects;

import com.excelstudio.app.AppServices;
import com.excelstudio.config.AppConfig;
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
        AppServices.initialize(AppConfig.loadDefault());

        URL fxml = requireResource("/fxml/dashboard.fxml");
        URL stylesheet = requireResource("/css/app.css");

        Scene scene = new Scene(new FXMLLoader(fxml).load(), 1100, 700);
        scene.getStylesheets().add(stylesheet.toExternalForm());

        stage.setTitle("Excel Automation Studio");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Excel Automation Studio");
        AppServices.get().shutdown();
    }

    private static URL requireResource(String path) {
        URL resource = Objects.requireNonNull(
                MainApp.class.getResource(path),
                () -> "Missing application resource: " + path);
        return resource;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
