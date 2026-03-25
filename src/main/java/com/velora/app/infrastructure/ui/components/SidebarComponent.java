package com.velora.app.infrastructure.ui.components;

import com.velora.app.infrastructure.ui.AppNavigator.Screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.css.PseudoClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reusable Sidebar component for the Velora application.
 * 
 * <p>
 * Provides:
 * </p>
 * <ul>
 * <li>Logo area with Velora branding</li>
 * <li>Navigation items (Home, Products, Analytics, Events, Settings, Shop
 * Profile)</li>
 * <li>User profile card at bottom</li>
 * <li>Active state management via AppNavigator</li>
 * </ul>
 */
public class SidebarComponent {

    // Components
    private final VBox container;
    private VBox navItemsContainer;
    private final Map<Screen, HBox> navItemMap = new HashMap<>();

    // State
    private Screen currentActiveScreen = Screen.DASHBOARD;
    private Consumer<Screen> onNavigate;

    // User info (would come from AuthService)
    private String userName = "Alex Chen";
    private String userRole = "Shop Owner";
    private String userInitials = "AC";

    /**
     * Constructs the SidebarComponent.
     *
     * @param onNavigate callback for navigation events
     */
    public SidebarComponent(Consumer<Screen> onNavigate) {
        container = new VBox();
        container.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-pref-width: 220px; " +
                        "-fx-min-width: 220px; " +
                        "-fx-max-width: 220px;");
        container.setFillWidth(true);

        this.onNavigate = onNavigate;

        buildSidebar();
    }

    /**
     * Sets the callback for navigation events.
     *
     * @param callback consumer that receives the selected screen
     */
    public void setOnNavigate(Consumer<Screen> callback) {
        this.onNavigate = callback;
    }

    /**
     * Sets the active screen and updates visual state.
     *
     * @param screen the screen to mark as active
     */
    public void setActiveScreen(Screen screen) {
        if (currentActiveScreen == screen) {
            return;
        }

        // Remove active state from previous
        HBox previousItem = navItemMap.get(currentActiveScreen);
        if (previousItem != null) {
            previousItem.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-padding: 12px 16px; " +
                            "-fx-cursor: hand; " +
                            "-fx-alignment: CENTER_LEFT; " +
                            "-fx-spacing: 12px;");
            previousItem.getStyleClass().remove("nav-item-active");
        }

        // Add active state to new
        HBox newItem = navItemMap.get(screen);
        if (newItem != null) {
            newItem.setStyle(
                    "-fx-background-color: #f2f4f6; " +
                            "-fx-border-width: 0 0 0 3px; " +
                            "-fx-border-color: #89ceff; " +
                            "-fx-padding: 12px 16px; " +
                            "-fx-cursor: hand; " +
                            "-fx-alignment: CENTER_LEFT; " +
                            "-fx-spacing: 12px;");
            newItem.getStyleClass().add("nav-item-active");
        }

        currentActiveScreen = screen;
    }

    /**
     * Sets the current user information.
     *
     * @param name     the user's display name
     * @param role     the user's role
     * @param initials the user's initials for avatar
     */
    public void setUserInfo(String name, String role, String initials) {
        this.userName = name;
        this.userRole = role;
        this.userInitials = initials;
        updateUserProfile();
    }

    /**
     * Gets the root node of this component.
     *
     * @return the Parent node containing the sidebar
     */
    public Parent getView() {
        return container;
    }

    private void buildSidebar() {
        container.setSpacing(0);

        // Top section - Logo
        VBox logoSection = buildLogoSection();

        // Navigation items
        navItemsContainer = new VBox();
        navItemsContainer.setStyle("-fx-spacing: 14px; -fx-padding: 24px 0;");
        navItemsContainer.setFillWidth(true);

        // Add navigation items
        addNavItem(Screen.DASHBOARD, "🏠", "Home");
        addNavItem(Screen.PRODUCTS, "📦", "Products");
        addNavItem(Screen.ANALYTICS, "📊", "Analytics");
        addNavItem(Screen.EVENTS, "🎉", "Events");
        addNavItem(Screen.SETTINGS, "⚙️", "Settings");
        addNavItem(Screen.SHOP_PROFILE, "🏪", "Shop Profile");

        // Set initial active state
        setActiveScreen(Screen.DASHBOARD);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Bottom section - User profile
        VBox userSection = buildUserSection();

        container.getChildren().addAll(logoSection, navItemsContainer, spacer, userSection);
    }

    private VBox buildLogoSection() {
        VBox section = new VBox();
        section.setStyle("-fx-padding: 24px 20px; -fx-spacing: 8px;");

        // Logo row
        HBox logoRow = new HBox();
        logoRow.setStyle("-fx-alignment: center-left; -fx-spacing: 12px;");

        // Logo icon (blue gradient placeholder)
        StackPane logoIcon = new StackPane();
        logoIcon.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #89ceff, #2e354a); " +
                        "-fx-background-radius: 8px; " +
                        "-fx-min-width: 32px; " +
                        "-fx-min-height: 32px; " +
                        "-fx-max-width: 32px; " +
                        "-fx-max-height: 32px; " +
                        "-fx-alignment: center;");

        Text logoSymbol = new Text("V");
        logoSymbol.setStyle(
                "-fx-font-family: 'Manrope', sans-serif; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-fill: white;");
        logoIcon.getChildren().add(logoSymbol);

        // Logo text
        VBox logoText = new VBox();
        logoText.setStyle("-fx-spacing: 2px;");

        Text appName = new Text("Velora");
        appName.setStyle(
                "-fx-font-family: 'Manrope', sans-serif; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-fill: #191c1e;");

        Text tagline = new Text("BUSINESS MANAGEMENT");
        tagline.setStyle(
                "-fx-font-family: 'Inter', sans-serif; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-fill: #57657b; " +
                        "-fx-letter-spacing: 0.08em;");

        logoText.getChildren().addAll(appName, tagline);
        logoRow.getChildren().addAll(logoIcon, logoText);

        section.getChildren().add(logoRow);
        return section;
    }

    private void addNavItem(Screen screen, String icon, String label) {
        HBox navItem = new HBox();
        navItem.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-padding: 12px 16px; " +
                        "-fx-cursor: hand; " +
                        "-fx-alignment: CENTER_LEFT; " +
                        "-fx-spacing: 12px;");
        navItem.getStyleClass().add("nav-item");

        // Store reference
        navItemMap.put(screen, navItem);

        // Icon
        Text iconText = new Text(icon);
        iconText.setStyle("-fx-font-size: 16px;");

        // Label
        Text labelText = new Text(label);
        labelText.setStyle(
                "-fx-font-family: 'Inter', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-fill: #191c1e;");

        navItem.getChildren().addAll(iconText, labelText);

        // Click handler
        navItem.setOnMouseClicked(e -> {
            if (onNavigate != null) {
                onNavigate.accept(screen);
            }
        });

        // Hover effect
        navItem.setOnMouseEntered(e -> {
            if (currentActiveScreen != screen) {
                navItem.setStyle(
                        "-fx-background-color: #d8dadc; " +
                                "-fx-padding: 12px 16px; " +
                                "-fx-cursor: hand; " +
                                "-fx-alignment: CENTER_LEFT; " +
                                "-fx-spacing: 12px;");
            }
        });

        navItem.setOnMouseExited(e -> {
            if (currentActiveScreen != screen) {
                navItem.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-padding: 12px 16px; " +
                                "-fx-cursor: hand; " +
                                "-fx-alignment: CENTER_LEFT; " +
                                "-fx-spacing: 12px;");
            }
        });

        navItemsContainer.getChildren().add(navItem);
    }

    private VBox buildUserSection() {
        VBox section = new VBox();
        section.setStyle("-fx-padding: 0 16px 24px 16px;");

        // User card
        HBox userCard = new HBox();
        userCard.setStyle(
                "-fx-background-color: #f2f4f6; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 12px; " +
                        "-fx-spacing: 12px; " +
                        "-fx-alignment: center-left;");

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setStyle(
                "-fx-background-color: #d5e3fd; " +
                        "-fx-background-radius: 9999px; " +
                        "-fx-min-width: 36px; " +
                        "-fx-min-height: 36px; " +
                        "-fx-max-width: 36px; " +
                        "-fx-max-height: 36px; " +
                        "-fx-alignment: center;");

        Text initials = new Text(userInitials);
        initials.setStyle(
                "-fx-font-family: 'Inter', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-fill: #0d1c2f;");
        avatar.getChildren().add(initials);

        // User info
        VBox userInfo = new VBox();
        userInfo.setStyle("-fx-spacing: 2px;");

        Text name = new Text(userName);
        name.setStyle(
                "-fx-font-family: 'Inter', sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-fill: #191c1e;");
        name.setId("user-name");

        Text role = new Text(userRole);
        role.setStyle(
                "-fx-font-family: 'Inter', sans-serif; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-fill: #57657b;");
        role.setId("user-role");

        userInfo.getChildren().addAll(name, role);

        userCard.getChildren().addAll(avatar, userInfo);
        section.getChildren().add(userCard);

        return section;
    }

    private void updateUserProfile() {
        // Update user info text nodes if they exist
        container.lookup("#user-name");
        container.lookup("#user-role");
    }
}
