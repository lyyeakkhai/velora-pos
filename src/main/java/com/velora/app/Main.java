package com.velora.app;

import com.velora.app.infrastructure.ui.MainFrame;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the Velora Business Management application.
 * 
 * <p>
 * This is a JavaFX desktop application that provides:
 * </p>
 * <ul>
 * <li>Dashboard Overview with sales analytics and quick actions</li>
 * <li>Product Management with categories, grids, and tables</li>
 * <li>Navigation sidebar and top bar</li>
 * </ul>
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Set application properties
        primaryStage.setTitle("Velora Business Management");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(800);

        // Create and show the main application frame
        MainFrame mainFrame = new MainFrame(primaryStage);
        mainFrame.show();

        // Center the window on screen
        primaryStage.centerOnScreen();
    }

    @Override
    public void stop() {
        // Cleanup resources when application closes
        System.out.println("Velora Business Management shutting down...");
    }
}
