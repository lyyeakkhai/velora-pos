package com.velora.app.infrastructure.ui;

import com.velora.app.infrastructure.ui.components.SidebarComponent;
import com.velora.app.infrastructure.ui.components.TopBarComponent;
import com.velora.app.infrastructure.ui.pages.DashboardController;
import com.velora.app.infrastructure.ui.pages.ProductController;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/**
 * Centralized navigation controller for the Velora application.
 * 
 * Manages screen switching between Dashboard and Product Management screens
 * using a single-window, center-swap pattern.
 */
public class AppNavigator {

    /**
     * Enum representing available screens in the application.
     */
    public enum Screen {
        DASHBOARD("Home"),
        PRODUCTS("Products"),
        ANALYTICS("Analytics"),
        EVENTS("Events"),
        SETTINGS("Settings"),
        SHOP_PROFILE("Shop Profile");

        private final String displayName;

        Screen(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final BorderPane rootContainer;
    private final SidebarComponent sidebar;
    private final TopBarComponent topBar;

    // Controllers
    private final DashboardController dashboardController;
    private final ProductController productController;

    private Screen currentScreen = Screen.DASHBOARD;

    /**
     * Creates a new AppNavigator with the given BorderPane root.
     * 
     * @param root the BorderPane container
     */
    public AppNavigator(BorderPane root) {
        this.rootContainer = root;
        this.sidebar = new SidebarComponent(this::navigateTo);
        this.topBar = new TopBarComponent();

        // Initialize controllers (using mock data for now)
        this.dashboardController = new DashboardController();
        this.productController = new ProductController();

        // Build the layout
        buildLayout();
    }

    private void buildLayout() {
        // Set sidebar on the left
        rootContainer.setLeft(sidebar.getView());

        // Set top bar at the top
        rootContainer.setTop(topBar.getView());

        // Load default screen
        navigateTo(Screen.DASHBOARD);
    }

    /**
     * Navigates to the specified screen.
     * 
     * @param screen the screen to navigate to
     */
    public void navigateTo(Screen screen) {
        this.currentScreen = screen;

        // Update sidebar active state
        sidebar.setActiveScreen(screen);

        // Update top bar title
        topBar.setPageTitle(screen.getDisplayName());

        // Load the appropriate screen content
        Node content = loadScreen(screen);
        rootContainer.setCenter(content);
    }

    private Node loadScreen(Screen screen) {
        switch (screen) {
            case DASHBOARD:
                return dashboardController.createView();
            case PRODUCTS:
                return productController.createView();
            case ANALYTICS:
                return createPlaceholderScreen("Analytics", "📊");
            case EVENTS:
                return createPlaceholderScreen("Events", "🎉");
            case SETTINGS:
                return createPlaceholderScreen("Settings", "⚙️");
            case SHOP_PROFILE:
                return createPlaceholderScreen("Shop Profile", "🏪");
            default:
                return dashboardController.createView();
        }
    }

    private Node createPlaceholderScreen(String title, String icon) {
        javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox();
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setSpacing(20);
        placeholder.getStyleClass().add("surface-container-low");
        placeholder.setPrefSize(800, 600);

        javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(icon);
        iconLabel.setStyle("-fx-font-size: 64px;");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.getStyleClass().add("headline-lg");

        placeholder.getChildren().addAll(iconLabel, titleLabel);

        return placeholder;
    }

    /**
     * Gets the current active screen.
     * 
     * @return the current screen
     */
    public Screen getCurrentScreen() {
        return currentScreen;
    }

    /**
     * Gets the sidebar component.
     * 
     * @return the sidebar
     */
    public SidebarComponent getSidebar() {
        return sidebar;
    }

    /**
     * Gets the top bar component.
     * 
     * @return the top bar
     */
    public TopBarComponent getTopBar() {
        return topBar;
    }
}
