package com.velora.app.infrastructure.ui.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Product Management Controller for Velora Business Management.
 * 
 * Displays: Product categories with grid and table views,
 * product cards with images and stock progress, and pulse metric cards.
 */
public class ProductController {

    // Styling constants from DESIGN.md
    private static final String COLOR_PRIMARY = "-color-primary";
    private static final String COLOR_TERTIARY_FIXED_DIM = "-color-tertiary-fixed-dim";
    private static final String COLOR_ON_SECONDARY_CONTAINER = "-color-on-secondary-container";
    private static final String COLOR_ON_SURFACE = "-color-on-surface";
    private static final String COLOR_SECONDARY_FIXED = "-color-secondary-fixed";
    private static final String COLOR_SURFACE_CONTAINER_LOWEST = "-color-surface-container-lowest";
    private static final String COLOR_SURFACE_CONTAINER_LOW = "-color-surface-container-low";
    private static final String COLOR_SURFACE_DIM = "-color-surface-dim";
    private static final String COLOR_SURFACE_CONTAINER_HIGH = "-color-surface-container-high";
    private static final String COLOR_ON_SECONDARY_FIXED = "-color-on-secondary-fixed";

    // Mock data for products
    private static final List<MockProduct> PREMIUM_COLLECTION = Arrays.asList(
            new MockProduct("Luxury Watch Pro", "$599.99", 85, 100),
            new MockProduct("Premium Headphones", "$349.99", 45, 50),
            new MockProduct("Designer Sunglasses", "$199.99", 100, 100));

    private static final List<MockProduct> LIFESTYLE_ESSENTIALS = Arrays.asList(
            new MockProduct("Leather Wallet", "Accessories", "Standard", "$79.99", "ACTIVE"),
            new MockProduct("Smartphone Case", "Accessories", "Standard", "$24.99", "LOW STOCK"),
            new MockProduct("Wireless Earbuds", "Electronics", "Pro", "$149.99", "ACTIVE"),
            new MockProduct("USB-C Cable", "Accessories", "Standard", "$12.99", "DRAFT"));

    /**
     * Creates and returns the product management root container.
     */
    public VBox createView() {
        VBox root = new VBox(40); // spacing between major sections
        root.setPadding(new Insets(36, 48, 48, 48));
        root.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        root.getStyleClass().add("surface-container-low");

        // Page Header
        root.getChildren().add(createPageHeader());

        // Premium Collection Category
        root.getChildren().add(createCategorySection(
                "🏆", "Premium Collection", "12 products",
                PREMIUM_COLLECTION, true));

        // Lifestyle Essentials Category
        root.getChildren().add(createCategorySection(
                "✨", "Lifestyle Essentials", "28 products",
                LIFESTYLE_ESSENTIALS, false));

        // Bottom Pulse Cards Row
        root.getChildren().add(createBottomPulseCards());

        return root;
    }

    private HBox createPageHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        // Left: Title and subtitle
        VBox leftContent = new VBox(8);
        Label title = new Label("Product Management");
        title.getStyleClass().add("headline-lg");
        title.setFont(Font.font("Manrope", FontWeight.BOLD, 28));

        Label subtitle = new Label("Manage your product inventory, categories, and pricing");
        subtitle.getStyleClass().add("body-md");
        subtitle.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        leftContent.getChildren().addAll(title, subtitle);

        // Right: New Category button
        Label newCategoryBtn = new Label("＋ New Category");
        newCategoryBtn.getStyleClass().add("btn-primary");

        HBox.setHgrow(leftContent, Priority.ALWAYS);
        header.getChildren().addAll(leftContent, newCategoryBtn);

        return header;
    }

    private VBox createCategorySection(String icon, String name, String count,
            List<MockProduct> products, boolean isGridView) {
        VBox section = new VBox(20);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(24));

        // Category Header
        HBox categoryHeader = new HBox(16);
        categoryHeader.setAlignment(Pos.CENTER_LEFT);

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-background-color: " + COLOR_SECONDARY_FIXED + "; " +
                "-fx-background-radius: 8; -fx-font-size: 18px; -fx-padding: 8;");

        // Name and count
        VBox nameContainer = new VBox(4);
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("headline-sm");
        nameLabel.setFont(Font.font("Manrope", FontWeight.BOLD, 20));

        Label countLabel = new Label(count + " PRODUCTS");
        countLabel.getStyleClass().add("label-sm");
        countLabel.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        nameContainer.getChildren().addAll(nameLabel, countLabel);

        // Add Product button
        Label addProductBtn = new Label("＋ Add Product");
        addProductBtn.getStyleClass().add("btn-secondary");

        HBox.setHgrow(nameContainer, Priority.ALWAYS);
        categoryHeader.getChildren().addAll(iconLabel, nameContainer, addProductBtn);

        section.getChildren().add(categoryHeader);

        // Products display (grid or table)
        if (isGridView) {
            section.getChildren().add(createProductGrid(products));
        } else {
            section.getChildren().add(createProductTable(products));
        }

        return section;
    }

    private HBox createProductGrid(List<MockProduct> products) {
        HBox grid = new HBox(24);
        grid.setAlignment(Pos.CENTER_LEFT);

        for (MockProduct product : products) {
            VBox card = createProductCard(product);
            card.setPrefWidth(200);
            grid.getChildren().add(card);
        }

        // Quick Add placeholder
        VBox quickAddCard = createQuickAddCard();
        quickAddCard.setPrefWidth(200);
        grid.getChildren().add(quickAddCard);

        return grid;
    }

    private VBox createProductCard(MockProduct product) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setPadding(new Insets(0));

        // Product image area
        VBox imageArea = new VBox();
        imageArea.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e6e8ea, #f2f4f6); " +
                "-fx-min-height: 180px; -fx-max-height: 180px;");
        imageArea.setAlignment(Pos.CENTER);
        imageArea.setPadding(new Insets(40));

        Label imageIcon = new Label("📦");
        imageIcon.setStyle("-fx-font-size: 48px;");
        imageArea.getChildren().add(imageIcon);

        // In Stock badge
        Label badge = new Label("IN STOCK");
        badge.getStyleClass().add("chip-blue");
        badge.setAlignment(Pos.TOP_RIGHT);
        badge.setPadding(new Insets(8, 12, 0, 0));

        // Product info
        VBox info = new VBox(12);
        info.setPadding(new Insets(16));
        info.setAlignment(Pos.TOP_LEFT);

        Label productName = new Label(product.name);
        productName.getStyleClass().add("label-lg");
        productName.setFont(Font.font("Inter", FontWeight.BOLD, 14));

        Label productPrice = new Label(product.price);
        productPrice.getStyleClass().add("label-lg");
        productPrice.setFont(Font.font("Inter", FontWeight.BOLD, 16));

        // Progress bar
        VBox progressContainer = new VBox(4);

        HBox progressBar = new HBox();
        double progressPercent = (double) product.stock / product.maxStock * 100;
        Label progress = new Label();
        progress.setStyle("-fx-background-color: " + COLOR_TERTIARY_FIXED_DIM + "; " +
                "-fx-background-radius: 9999; " +
                "-fx-pref-width: " + progressPercent + "px; " +
                "-fx-pref-height: 4px;");
        Label emptyBar = new Label();
        emptyBar.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_HIGH + "; " +
                "-fx-background-radius: 9999; " +
                "-fx-pref-width: " + (100 - progressPercent) + "px; " +
                "-fx-pref-height: 4px;");
        progressBar.getChildren().addAll(progress, emptyBar);

        Label stockLabel = new Label(product.stock + "/" + product.maxStock + " units");
        stockLabel.getStyleClass().add("label-sm");
        stockLabel.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        progressContainer.getChildren().addAll(progressBar, stockLabel);

        // Stack image and badge
        VBox imageWrapper = new VBox();
        imageWrapper.getChildren().add(imageArea);
        imageWrapper.setAlignment(Pos.TOP_RIGHT);
        VBox badgeWrapper = new VBox();
        badgeWrapper.setAlignment(Pos.TOP_RIGHT);
        badgeWrapper.setPadding(new Insets(8));
        badgeWrapper.getChildren().add(badge);
        imageWrapper.getChildren().add(badgeWrapper);

        info.getChildren().addAll(productName, productPrice, progressContainer);
        card.getChildren().addAll(imageWrapper, info);

        return card;
    }

    private VBox createQuickAddCard() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 20, 40, 20));
        card.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + "; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(196, 198, 207, 0.4); " +
                "-fx-border-width: 2; " +
                "-fx-border-style: dashed; " +
                "-fx-border-radius: 8;");

        Label icon = new Label("➕");
        icon.setStyle("-fx-font-size: 32px; -fx-opacity: 0.5;");

        Label title = new Label("Quick Add Product");
        title.getStyleClass().add("label-lg");
        title.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        Label desc = new Label("Add a new product quickly");
        desc.getStyleClass().add("body-md");
        desc.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        card.getChildren().addAll(icon, title, desc);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + COLOR_SURFACE_DIM + "; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(196, 198, 207, 0.4); " +
                "-fx-border-width: 2; " +
                "-fx-border-style: dashed; " +
                "-fx-border-radius: 8;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + "; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(196, 198, 207, 0.4); " +
                "-fx-border-width: 2; " +
                "-fx-border-style: dashed; " +
                "-fx-border-radius: 8;"));

        return card;
    }

    private VBox createProductTable(List<MockProduct> products) {
        VBox table = new VBox(0);

        // Table header
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(12, 16, 12, 16));
        tableHeader.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + ";");

        addTableCell(tableHeader, "PRODUCT DETAILS", 300, true);
        addTableCell(tableHeader, "STATUS", 120, true);
        addTableCell(tableHeader, "PRICE", 100, true);
        addTableCell(tableHeader, "ACTIONS", 100, true);

        table.getChildren().add(tableHeader);

        // Table rows
        for (int i = 0; i < products.size(); i++) {
            MockProduct product = products.get(i);
            HBox row = createProductRow(product);

            // Add ghost border separator (except for last row)
            if (i < products.size() - 1) {
                VBox rowContainer = new VBox();
                rowContainer.getChildren().add(row);

                Label separator = new Label();
                separator.setStyle("-fx-background-color: rgba(196, 198, 207, 0.15); " +
                        "-fx-pref-height: 1px;");
                rowContainer.getChildren().add(separator);

                table.getChildren().add(rowContainer);
            } else {
                table.getChildren().add(row);
            }
        }

        return table;
    }

    private HBox createProductRow(MockProduct product) {
        HBox row = new HBox(16);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: transparent;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " +
                COLOR_SURFACE_CONTAINER_LOW + ";"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent;"));

        // Product details
        HBox detailsCell = new HBox(12);
        detailsCell.setAlignment(Pos.CENTER_LEFT);
        detailsCell.setPrefWidth(300);

        Label thumbnail = new Label("📦");
        thumbnail.setStyle("-fx-background-color: " + COLOR_SURFACE_CONTAINER_LOW + "; " +
                "-fx-background-radius: 6; -fx-padding: 8;");

        VBox nameInfo = new VBox(4);
        Label nameLabel = new Label(product.name);
        nameLabel.getStyleClass().add("label-lg");
        nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));

        Label variantLabel = new Label(product.category + " • " + product.variant);
        variantLabel.getStyleClass().add("label-sm");
        variantLabel.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + ";");

        nameInfo.getChildren().addAll(nameLabel, variantLabel);
        detailsCell.getChildren().addAll(thumbnail, nameInfo);

        // Status cell
        Label statusChip = new Label(product.status);
        if ("ACTIVE".equals(product.status)) {
            statusChip.getStyleClass().add("chip-green");
        } else if ("LOW STOCK".equals(product.status)) {
            statusChip.getStyleClass().add("chip-orange");
        } else {
            statusChip.getStyleClass().add("chip-grey");
        }
        statusChip.setPrefWidth(100);
        statusChip.setAlignment(Pos.CENTER);

        // Price cell
        Label priceLabel = new Label(product.price);
        priceLabel.getStyleClass().add("label-lg");
        priceLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        priceLabel.setPrefWidth(100);

        // Actions cell
        HBox actionsCell = new HBox(8);
        actionsCell.setPrefWidth(100);
        actionsCell.setAlignment(Pos.CENTER_LEFT);

        Label editBtn = new Label("✏️");
        editBtn.setStyle("-fx-cursor: HAND; -fx-padding: 4;");

        Label deleteBtn = new Label("🗑️");
        deleteBtn.setStyle("-fx-cursor: HAND; -fx-padding: 4;");

        actionsCell.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(detailsCell, statusChip, priceLabel, actionsCell);

        return row;
    }

    private void addTableCell(HBox container, String text, double width, boolean isHeader) {
        Label cell = new Label(text);
        cell.setPrefWidth(width);
        if (isHeader) {
            cell.getStyleClass().add("label-sm");
            cell.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + "; " +
                    "-fx-letter-spacing: 0.05em;");
        }
        container.getChildren().add(cell);
    }

    private HBox createBottomPulseCards() {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);

        // Card 1: Total Valuation
        VBox valuationCard = createPulseCard(
                "TOTAL VALUATION",
                "$142,500",
                "+12.4%",
                "chip-blue",
                false);
        valuationCard.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(valuationCard, Priority.ALWAYS);

        // Card 2: Inventory Status
        VBox inventoryCard = createPulseCard(
                "INVENTORY STATUS",
                "1,204",
                "Optimal",
                "chip-green",
                false);
        inventoryCard.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(inventoryCard, Priority.ALWAYS);

        // Card 3: Category Distribution (dark card)
        VBox categoryCard = createCategoryDistributionCard();
        categoryCard.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(categoryCard, Priority.ALWAYS);

        row.getChildren().addAll(valuationCard, inventoryCard, categoryCard);
        return row;
    }

    private VBox createPulseCard(String label, String value, String trend,
            String chipStyle, boolean isDark) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        if (isDark) {
            card.setStyle("-fx-background-color: " + COLOR_PRIMARY + ";");
        }

        // Label
        Label labelText = new Label(label);
        labelText.getStyleClass().add("label-sm");
        if (isDark) {
            labelText.setStyle("-fx-text-fill: #ffffff; opacity: 0.7; letter-spacing: 0.05em;");
        } else {
            labelText.setStyle("-fx-text-fill: " + COLOR_ON_SECONDARY_CONTAINER + "; " +
                    "letter-spacing: 0.05em;");
        }

        // Value
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Manrope", FontWeight.BOLD, 36));
        if (isDark) {
            valueText.setStyle("-fx-text-fill: #ffffff;");
        }

        // Trend chip
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_RIGHT);

        Label trendChip = new Label(trend);
        trendChip.getStyleClass().add(chipStyle);

        bottomRow.getChildren().add(trendChip);

        card.getChildren().addAll(labelText, valueText, bottomRow);
        return card;
    }

    private VBox createCategoryDistributionCard() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; " +
                "-fx-background-radius: 8;");
        card.setPadding(new Insets(24));

        // Label
        Label labelText = new Label("CATEGORY DISTRIBUTION");
        labelText.setStyle("-fx-text-fill: #ffffff; opacity: 0.7; " +
                "-fx-font-family: Inter; -fx-font-size: 12px; " +
                "-fx-font-weight: 500; letter-spacing: 0.05em;");

        // Value
        Label valueText = new Label("06");
        valueText.setFont(Font.font("Manrope", FontWeight.BOLD, 36));
        valueText.setStyle("-fx-text-fill: #ffffff;");

        // Link
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_RIGHT);

        Label link = new Label("Manage Hierarchies");
        link.setStyle("-fx-text-fill: " + COLOR_TERTIARY_FIXED_DIM + "; " +
                "-fx-cursor: HAND; -fx-font-family: Inter; -fx-font-size: 14px;");

        bottomRow.getChildren().add(link);

        card.getChildren().addAll(labelText, valueText, bottomRow);
        return card;
    }

    /**
     * Simple mock product class for UI demonstration.
     */
    private static class MockProduct {
        String name;
        String category;
        String variant;
        String price;
        String status;
        int stock;
        int maxStock;

        MockProduct(String name, String price, int stock, int maxStock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.maxStock = maxStock;
            this.category = "General";
            this.variant = "Standard";
            this.status = "ACTIVE";
        }

        MockProduct(String name, String category, String variant, String price, String status) {
            this.name = name;
            this.category = category;
            this.variant = variant;
            this.price = price;
            this.status = status;
            this.stock = 50;
            this.maxStock = 100;
        }
    }

    /**
     * Initialize the controller.
     */
    public void initialize() {
        // FXML-based initialization placeholder
    }
}
