package com.excelstudio.ui;

import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads FXML views once and swaps them inside a container.
 *
 * <p>The dashboard shell is not a view: it stays loaded while the content area changes.
 */
public final class ViewNavigator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewNavigator.class);

    private final Pane container;
    private final Map<View, Parent> cache = new EnumMap<>(View.class);
    private View current;

    public ViewNavigator(Pane container) {
        this.container = container;
    }

    public View current() {
        return current;
    }

    public void show(View view) {
        if (view == current) {
            return;
        }
        Parent root = cache.computeIfAbsent(view, this::load);
        container.getChildren().setAll(root);
        current = view;
        if (root instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
        LOGGER.debug("Showing view {}", view);
    }

    private Parent load(View view) {
        URL resource = ViewNavigator.class.getResource(view.fxmlPath());
        if (resource == null) {
            throw new IllegalStateException("Missing FXML resource: " + view.fxmlPath());
        }
        try {
            return new FXMLLoader(resource).load();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load view " + view, e);
        }
    }
}
