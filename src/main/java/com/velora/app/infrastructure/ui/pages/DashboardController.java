package com.velora.app.infrastructure.ui.pages;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Dashboard Overview Controller for Velora Business Management.
 * 
 * Displays: Hero section, Quick Actions, Sales Trajectory chart,
 * Pulse metric cards, Recent Products, and Upcoming Events.
 */
public class DashboardController {

    // Styling constants from DESIGN.md
    private static final String COLOR_PRIMARY = "-color-primary";
    private static final String COLOR_TERTIARY_FIXED_DIM = "-color-tertiary-fixed-dim";
    private static final String COLOR_ON_SECONDARY_CONTAINER = "-color-on-secondary-container";
    private static final String COLOR_ON_SURFACE = "-color-on-surface";
    private static final String COLOR_SECONDARY_FIXED = "-color-secondary-fixed";
    private static final String COLOR_SURFACE_CONTAINER_LOWEST = "-color-surface-container-lowest";
    private static final String COLOR_SURFACE_CONTAINER_LOW = "-color-surface-container-low";
    private static final String COLOR_SURFACE_DIM = "-color-surface-dim";

    @FXML
    private VBox contentArea;

    /**
     * Creates and returns the dashboard root container.
     */
    public VBox createView() {
        VBox root = new VBox(56); // spacing-16 between sections
        root.setPadding(new Insets(36, 48, 48, 48));
        root.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        root.getStyleClass().add("surface-container-low");

        // Hero Section
        root.getChildren().add(createHeroSection());

        // Quick Actions Section
        root.getChildren().add(createQuickActionsSection());

        // Analytics Row
        root.getChildren().add(createAnalyticsRow());

        // Bottom Row
        root.getChildren().add(createBottomRow());

        return root;
    }

    private VBox createHeroSection() {
        VBox hero = new VBox(16);

        // Eyebrow text
        Label eyebrow = new Label("MORNING, ALEX");
        eyebrow.getStyleClass().add("label-sm");
        eyebrow.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + "; " +
                "-fx-letter-spacing: 0.1em;");

        // Headline with TextFlow
        TextFlow headlineFlow = new TextFlow();

        Text normalText = new Text("Your shop is performing ");
        normalText.setFont(Font.font("Manrope", FontWeight.BOLD, 28));
        normalText.setStyle("-fx-fill: " + COLOR_ON_SURFACE + ";");

        Text highlightedText = new Text("above target");
        highlightedText.setFont(Font.font("Manrope", FontWeight.BOLD, 28));
        highlightedText.setStyle("-fx-fill: " + COLOR_TERTIARY_FIXED_DIM + ";");

        Text endText = new Text(" today.");
        endText.setFont(Font.font("Manrope", FontWeight.BOLD, 28));
        endText.setStyle("-fx-fill: " + COLOR_ON_SURFACE + ";");

        headlineFlow.getChildren().addAll(normalText, highlightedText, endText);

        // Buttons (right side)
        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Label downloadBtn = new Label("Download Reports");
        downloadBtn.getStyleClass().add("btn-secondary");

        Label liveViewBtn = new Label("Live View ⚡");
        liveViewBtn.getStyleClass().add("btn-primary");

        buttons.getChildren().addAll(downloadBtn, liveViewBtn);

        // Layout: text left, buttons right
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(headlineFlow, Priority.ALWAYS);
        HBox.setHgrow(buttons, Priority.ALWAYS);
        topRow.getChildren().addAll(headlineFlow, buttons);

        hero.getChildren().addAll(eyebrow, topRow);
        return hero;
    }

    private VBox createQuickActionsSection() {
        VBox section = new VBox(16);

        Label title = new Label("Quick Actions");
        title.getStyleClass().add("headline-sm");
        title.setFont(Font.font("Manrope", FontWeight.BOLD, 20));

        HBox cards = new HBox(36); // spacing-10
        cards.setAlignment(Pos.CENTER_LEFT);

        // Card 1: Create Category
        VBox card1 = createQuickActionCard("🏷️", "Create Category", "Add new product categories");
        cards.getChildren().add(card1);

        // Card 2: Add Product
        VBox card2 = createQuickActionCard("📦", "Add Product", "List new inventory items");
        cards.getChildren().add(card2);

        // Card 3: Add Event
        VBox card3 = createQuickActionCard("📅", "Add Event", "Create promotional events");
        cards.getChildren().add(card3);

        section.getChildren().addAll(title, cards);
        return section;
    }

    private VBox createQuickActionCard(String icon, String title, String description) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
        card.setMinWidth(220);
        card.setAlignment(Pos.TOP_LEFT);

        // Icon box
        Label iconBox = new Label(icon);
        iconBox.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + "; " +
                "-fx-background-radius: 8px; " +
                "-fx-font-size: 20px; " +
                "-fx-padding: 10;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("label-lg");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("body-md");
        descLabel.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        card.getChildren().addAll(iconBox, titleLabel, descLabel);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + COLOR_SURFACE_DIM + "; " +
                "-fx-background-radius: 8; -fx-cursor: HAND;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " +
                COLOR_SURFACE_CONTAINER_LOWEST + "; -fx-background-radius: 8;"));

        return card;
    }

    private HBox createAnalyticsRow() {
        HBox row = new HBox(36);
        row.setAlignment(Pos.CENTER_LEFT);

        // Sales Trajectory Card (65% width)
        VBox salesCard = createSalesTrajectoryCard();
        HBox.setHgrow(salesCard, Priority.ALWAYS);
        salesCard.setMaxWidth(Double.MAX_VALUE);

        // Pulse Metric Cards (35% width, stacked)
        VBox metricsStack = new VBox(20);
        metricsStack.getChildren().addAll(
                createNetRevenueCard(),
                createActiveOrdersCard());
        metricsStack.setPrefWidth(320);

        row.getChildren().addAll(salesCard, metricsStack);
        return row;
    }

    private VBox createSalesTrajectoryCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        // Header with chip
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Sales Trajectory");
        title.getStyleClass().add("headline-sm");
        title.setFont(Font.font("Manrope", FontWeight.BOLD, 20));

        Label chip = new Label("+12.4%");
        chip.getStyleClass().add("chip-blue");

        header.getChildren().addAll(title, chip);

        // Subtext
        Label subtext = new Label("Weekly comparison");
        subtext.getStyleClass().add("label-md");
        subtext.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        // Bar Chart
        BarChart<String, Number> chart = createSalesChart();

        card.getChildren().addAll(header, subtext, chart);
        return card;
    }

    private BarChart<String, Number> createSalesChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(javafx.collections.FXCollections.observableArrayList(
                "MON", "TUE", "WED", "THU", "FRI", "SAT"));
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(false);
        xAxis.setGapStartAndEnd(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setUpperBound(100);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setBarGap(8);
        chart.setCategoryGap(16);
        chart.setPrefHeight(200);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("MON", 45));
        series.getData().add(new XYChart.Data<>("TUE", 62));
        series.getData().add(new XYChart.Data<>("WED", 58));
        series.getData().add(new XYChart.Data<>("THU", 71));
        series.getData().add(new XYChart.Data<>("FRI", 85));
        series.getData().add(new XYChart.Data<>("SAT", 100));

        chart.getData().add(series);
        chart.getStyleClass().add("sales-chart");

        return chart;
    }

    private VBox createNetRevenueCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        // Top row: label and icon
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("NET REVENUE");
        label.getStyleClass().add("label-sm");
        label.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + "; " +
                "-fx-letter-spacing: 0.05em;");

        Label icon = new Label("💰");
        icon.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + "; " +
                "-fx-background-radius: 8; -fx-padding: 4 8;");

        HBox.setHgrow(label, Priority.ALWAYS);
        top.getChildren().addAll(label, icon);

        // Value
        Label value = new Label("$12,480");
        value.setFont(Font.font("Manrope", FontWeight.BOLD, 36));

        // Trend chip
        HBox bottom = new HBox();
        bottom.setAlignment(Pos.CENTER_RIGHT);

        Label trend = new Label("+4.2%");
        trend.getStyleClass().add("chip-blue");
        trend.setAlignment(Pos.CENTER_RIGHT);

        VBox valueContainer = new VBox();
        VBox.setVgrow(value, Priority.ALWAYS);
        valueContainer.getChildren().add(value);

        card.getChildren().addAll(top, value, bottom);
        return card;
    }

    private VBox createActiveOrdersCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        // Top row: label and icon
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("ACTIVE ORDERS");
        label.getStyleClass().add("label-sm");
        label.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + "; " +
                "-fx-letter-spacing: 0.05em;");

        Label icon = new Label("📋");
        icon.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + "; " +
                "-fx-background-radius: 8; -fx-padding: 4 8;");

        HBox.setHgrow(label, Priority.ALWAYS);
        top.getChildren().addAll(label, icon);

        // Value
        Label value = new Label("84");
        value.setFont(Font.font("Manrope", FontWeight.BOLD, 36));

        // Trend chip
        HBox bottom = new HBox();
        bottom.setAlignment(Pos.CENTER_RIGHT);

        Label trend = new Label("-2.1%");
        trend.getStyleClass().add("chip-orange");
        trend.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(top, value, bottom);
        return card;
    }

    private HBox createBottomRow() {
        HBox row = new HBox(36);
        row.setAlignment(Pos.CENTER_LEFT);

        // Recent Products (50%)
        VBox recentProducts = createRecentProductsSection();
        recentProducts.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(recentProducts, Priority.ALWAYS);

        // Upcoming Events (50%)
        VBox upcomingEvents = createUpcomingEventsSection();
        upcomingEvents.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(upcomingEvents, Priority.ALWAYS);

        row.getChildren().addAll(recentProducts, upcomingEvents);
        return row;
    }

    private VBox createRecentProductsSection() {
        VBox section = new VBox(16);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Products");
        title.getStyleClass().add("headline-sm");
        title.setFont(Font.font("Manrope", FontWeight.BOLD, 20));

        Label viewAll = new Label("View All");
        viewAll.setStyle("-fx-text-fill: " + COLOR_PRIMARY + "; " +
                "-fx-cursor: HAND; -fx-padding: 0 0 0 16;");
        HBox.setHgrow(title, Priority.ALWAYS);

        header.getChildren().addAll(title, viewAll);

        // Product list
        VBox productList = new VBox(12); // spacing-4
        productList.getChildren().addAll(
                createProductListItem("Premium Watch", "Electronics", "$299.99", "In Stock"),
                createProductListItem("Wireless Headphones", "Electronics", "$149.99", "Low Stock"),
                createProductListItem("Leather Wallet", "Accessories", "$79.99", "In Stock"),
                createProductListItem("Smartphone Case", "Accessories", "$24.99", "In Stock"));

        section.getChildren().addAll(header, productList);
        return section;
    }

    private HBox createProductListItem(String name, String category, String price, String stockStatus) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 0, 8, 0));

        // Thumbnail placeholder
        Label thumbnail = new Label("📦");
        thumbnail.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + "; " +
                "-fx-background-radius: 6; -fx-padding: 12;");

        // Name and category
        VBox info = new VBox(4);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("label-lg");
        nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));

        Label categoryLabel = new Label(category);
        categoryLabel.getStyleClass().add("label-sm");
        categoryLabel.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        info.getChildren().addAll(nameLabel, categoryLabel);

        // Price
        Label priceLabel = new Label(price);
        priceLabel.getStyleClass().add("label-lg");
        priceLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));

        // Stock chip
        Label stockChip = new Label(stockStatus);
        stockChip.getStyleClass().add("chip-green");
        stockChip.setMinWidth(80);
        stockChip.setAlignment(Pos.CENTER);

        HBox.setHgrow(info, Priority.ALWAYS);
        item.getChildren().addAll(thumbnail, info, priceLabel, stockChip);

        return item;
    }

    private VBox createUpcomingEventsSection() {
        VBox section = new VBox(16);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Upcoming Events");
        title.getStyleClass().add("headline-sm");
        title.setFont(Font.font("Manrope", FontWeight.BOLD, 20));

        Label calendarView = new Label("Calendar View");
        calendarView.setStyle("-fx-text-fill: " + COLOR_PRIMARY + "; " +
                "-fx-cursor: HAND; -fx-padding: 0 0 0 16;");
        HBox.setHgrow(title, Priority.ALWAYS);

        header.getChildren().addAll(title, calendarView);

        // Events list
        VBox eventsList = new VBox(16);
        eventsList.getChildren().addAll(
                createEventItem("Summer Sale", LocalDate.of(2026, 4, 15), "Active Soon"),
                createEventItem("Flash Deal Week", LocalDate.of(2026, 4, 22), "Planning"));

        section.getChildren().addAll(header, eventsList);
        return section;
    }

    private HBox createEventItem(String eventName, LocalDate date, String status) {
        HBox event = new HBox(16);
        event.setAlignment(Pos.CENTER_LEFT);

        // Date block
        VBox dateBlock = new VBox(4);
        dateBlock.setAlignment(Pos.CENTER);
        dateBlock.setPadding(new Insets(12, 16, 12, 16));
        dateBlock.setStyle("-fx-background-color: " + COLOR_SECONDARY_FIXED + "; " +
                "-fx-background-radius: 8;");

        Label month = new Label(date.getMonth().toString().substring(0, 3).toUpperCase());
        month.getStyleClass().add("label-sm");
        month.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        Label day = new Label(String.valueOf(date.getDayOfMonth()));
        day.getStyleClass().add("headline-sm");
        day.setFont(Font.font("Manrope", FontWeight.BOLD, 20));

        dateBlock.getChildren().addAll(month, day);

        // Event details
        VBox details = new VBox(4);
        Label nameLabel = new Label(eventName);
        nameLabel.getStyleClass().add("label-lg");
        nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));

        Label dateLabel = new Label(date.toString());
        dateLabel.getStyleClass().add("label-sm");
        dateLabel.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        details.getChildren().addAll(nameLabel, dateLabel);

        // Status chip
        Label statusChip = new Label(status);
        if ("Active Soon".equals(status)) {
            statusChip.getStyleClass().add("chip-blue");
        } else {
            statusChip.getStyleClass().add("chip-grey");
        }

        HBox.setHgrow(details, Priority.ALWAYS);
        event.getChildren().addAll(dateBlock, details, statusChip);

        return event;
    }

    /**
     * Initialize the controller.
     * Called after FXML injection.
     */
    @FXML
    public void initialize() {
        // FXML-based initialization placeholder
    }
}
