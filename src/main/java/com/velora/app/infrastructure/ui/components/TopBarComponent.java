package com.velora.app.infrastructure.ui.components;

import com.velora.app.infrastructure.ui.AppNavigator.Screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

/**
 * Reusable Top Bar component for the Velora application.
 * 
 * <p>
 * Provides:
 * </p>
 * <ul>
 * <li>Page title (left)</li>
 * <li>Search field (center)</li>
 * <li>Optional right-side icons (bell, help, profile)</li>
 * </ul>
 */
public class TopBarComponent {

    // Components
    private final HBox container;
    private TextField searchField;
    private Label pageTitleText;

    // State
    private Consumer<String> onSearch;
    private Consumer<Screen> onNavigate;

    // Current title
    private String currentTitle = "Dashboard";

    /**
     * Constructs the TopBarComponent with default settings (no right icons).
     */
    public TopBarComponent() {
        this(false);
    }

    /**
     * Constructs the TopBarComponent.
     *
     * @param showRightIcons whether to show right-side icons (bell, help, profile)
     */
    public TopBarComponent(boolean showRightIcons) {
        container = new HBox();
        container.setStyle(
                "-fx-background-color: rgba(255,255,255,0.8); " +
                        "-fx-background-radius: 0 0 16px 16px; " +
                        "-fx-pref-height: 64px; " +
                        "-fx-min-height: 64px; " +
                        "-fx-padding: 0 24px;");
        container.setFillHeight(true);
        container.setAlignment(Pos.CENTER_LEFT);

        buildTopBar(showRightIcons);
    }

    private void buildTopBar(boolean showRightIcons) {
        // Page title (left)
        pageTitleText = new Label(currentTitle);
        pageTitleText.setFont(Font.font("Manrope", FontWeight.SEMI_BOLD, 20));
        pageTitleText.setStyle("-fx-text-fill: -color-on-surface;");
        HBox.setMargin(pageTitleText, new Insets(0, 24, 0, 0));

        // Search field (center)
        searchField = createSearchField();
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setMaxWidth(400);
        searchField.setMinWidth(200);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        // Right section (if needed)
        HBox rightSection = new HBox(16);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        if (showRightIcons) {
            Label bellIcon = new Label("🔔");
            bellIcon.setStyle("-fx-cursor: HAND; -fx-font-size: 18px;");

            Label helpIcon = new Label("❓");
            helpIcon.setStyle("-fx-cursor: HAND; -fx-font-size: 18px;");

            Label profileChip = new Label("👤 Alex");
            profileChip.setStyle(
                    "-fx-background-color: -color-secondary-fixed; " +
                            "-fx-text-fill: -color-on-secondary-fixed; " +
                            "-fx-background-radius: 9999; " +
                            "-fx-padding: 6 12 6 12; " +
                            "-fx-font-family: Inter; " +
                            "-fx-font-size: 14px; " +
                            "-fx-cursor: HAND;");

            rightSection.getChildren().addAll(bellIcon, helpIcon, profileChip);
        }

        container.getChildren().addAll(pageTitleText, searchField, spacer, rightSection);
    }

    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("Search products, orders, events...");
        field.setStyle(
                "-fx-background-color: -color-surface-container-highest; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-font-family: Inter; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 8 14 8 36; " +
                        "-fx-prompt-text-fill: -color-outline-variant;");

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                field.setStyle(
                        "-fx-background-color: -color-surface-container-lowest; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-font-family: Inter; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 8 14 8 36; " +
                                "-fx-border-color: rgba(24,32,52,0.2); " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 6px;");
            } else {
                field.setStyle(
                        "-fx-background-color: -color-surface-container-highest; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-font-family: Inter; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 8 14 8 36;");
            }
        });

        // Search action handler
        field.setOnAction(e -> {
            if (onSearch != null) {
                onSearch.accept(field.getText());
            }
        });

        return field;
    }

    /**
     * Gets the root node of this component.
     *
     * @return the Parent node containing the top bar
     */
    public Parent getView() {
        return container;
    }

    /**
     * Sets the page title.
     *
     * @param title the new title
     */
    public void setPageTitle(String title) {
        this.currentTitle = title;
        if (pageTitleText != null) {
            pageTitleText.setText(title);
        }
    }

    /**
     * Sets the search handler.
     *
     * @param handler the consumer to handle search queries
     */
    public void setOnSearch(Consumer<String> handler) {
        this.onSearch = handler;
    }

    /**
     * Gets the search field.
     *
     * @return the search TextField
     */
    public TextField getSearchField() {
        return searchField;
    }

    /**
     * Gets the current page title.
     *
     * @return the current title
     */
    public String getPageTitle() {
        return currentTitle;
    }
}
