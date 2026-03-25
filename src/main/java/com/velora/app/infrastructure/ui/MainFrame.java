package com.velora.app.infrastructure.ui;

import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application frame for Velora Business Management.
 * 
 * <p>
 * Creates the main application shell with:
 * </p>
 * <ul>
 * <li>BorderPane as root layout</li>
 * <li>Sidebar on the left (220px fixed width)</li>
 * <li>Top bar at the top</li>
 * <li>Content area in the center</li>
 * </ul>
 */
public class MainFrame {

    private final Stage stage;
    private final BorderPane rootPane;
    private final AppNavigator navigator;

    /**
     * Creates a new MainFrame.
     * 
     * @param stage the primary stage
     */
    public MainFrame(Stage stage) {
        this.stage = stage;
        this.rootPane = new BorderPane();

        // Base styling (tokens are defined in /styles.css)
        rootPane.getStyleClass().add("surface-container-low");

        // Initialize the navigator which sets up sidebar and top bar
        this.navigator = new AppNavigator(rootPane);

        // Scene must include stylesheet so looked-up colors resolve everywhere
        Scene scene = new Scene(rootPane, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * Shows the main frame.
     */
    public void show() {
        stage.show();
    }

    /**
     * Gets the root BorderPane.
     * 
     * @return the root pane
     */
    public BorderPane getRootPane() {
        return rootPane;
    }

    /**
     * Gets the app navigator.
     * 
     * @return the navigator
     */
    public AppNavigator getNavigator() {
        return navigator;
    }

    /**
     * Gets the primary stage.
     * 
     * @return the stage
     */
    public Stage getStage() {
        return stage;
    }
}
