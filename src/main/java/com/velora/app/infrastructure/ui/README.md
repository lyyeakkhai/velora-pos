# Velora Business Management - JavaFX UI

A JavaFX desktop application implementing the Velora Business Management UI according to the design specification in [`DESIGN.md`](./DESIGN.md).

## Features

- **Dashboard Overview**: Real-time metrics, sales trajectory charts, quick actions
- **Product Management**: Category management, product cards, inventory tracking
- **Responsive Navigation**: Sidebar with active state management
- **Design System**: Full implementation of color tokens, typography, and spacing

## Architecture

```
src/main/java/com/velora/app/
├── Main.java                          # JavaFX Application entry point
└── infrastructure/ui/
    ├── AppNavigator.java              # Centralized navigation controller
    ├── MainFrame.java                 # Application shell (sidebar + topbar)
    ├── styles.css                     # Design system tokens & component styles
    ├── components/
    │   ├── SidebarComponent.java     # Reusable sidebar navigation
    │   └── TopBarComponent.java      # Top bar with search
    ├── pages/
    │   ├── DashboardController.java  # Dashboard screen with service integration
    │   └── ProductController.java     # Product management screen
    └── DESIGN.md                      # Full design specification
```

## Service Integration

The UI is designed to integrate with the existing modular services:

| UI Component | Service Used | Key Methods |
|-------------|--------------|-------------|
| Dashboard | `AuthService` | User profile data |
| Dashboard | `AdminService` | `generateReport()`, `openLiveDashboard()` |
| Dashboard | `InventoryManagementService` | Recent products |
| Dashboard | `PaymentService` | Revenue data |
| Products | `InventoryManagementService` | `createCategory()`, `createProductAtomic()`, `updateProduct()`, `deleteProduct()` |
| Products | `AuthService` | Permission checks |

## Building

### Prerequisites

- Java 17+
- Maven 3.6+
- JavaFX SDK 21 (managed via Maven)

### Build Commands

```bash
# Compile
mvn clean compile

# Run (requires JavaFX)
mvn exec:java -Dexec.mainClass="com.velora.app.Main"

# Package as JAR
mvn package

# Run packaged JAR
java -jar target/velora_pos-1.0-SNAPSHOT.jar
```

### IDE Setup

**VS Code**:
1. Install "Extension Pack for Java" by Microsoft
2. Install "JavaFX SDK" via Maven (automatic with dependencies)
3. Open the project folder
4. Run `Main.java` using the Run button

**IntelliJ IDEA**:
1. Open as Maven project
2. Add JavaFX library from Maven
3. Create run configuration for `com.velora.app.Main`
4. Add VM options: `--add-modules javafx.controls,javafx.fxml`

**Eclipse**:
1. Import as Maven project
2. Install e(fx)clipse plugin
3. Create JavaFX launch configuration

## Design System

### Color Tokens

| Token | Hex | Usage |
|-------|-----|-------|
| `--color-primary` | #182034 | Primary actions, dark cards |
| `--color-tertiary-fixed-dim` | #89ceff | Accent, chart highlights |
| `--color-surface-container-low` | #f2f4f6 | Main background |
| `--color-surface-container-lowest` | #ffffff | Cards, content areas |
| `--color-on-surface` | #191c1e | Primary text |
| `--color-on-secondary-container` | #57657b | Secondary text |

### Typography

| Style | Font | Size | Weight |
|-------|------|------|--------|
| `display-sm` | Manrope | 36px | Bold |
| `headline-lg` | Manrope | 28px | Bold |
| `headline-sm` | Manrope | 20px | Semibold |
| `body-md` | Inter | 14px | Normal |
| `label-lg` | Inter | 14px | Medium |

### Spacing Scale

- `spacing-4`: 0.9rem (list item gaps)
- `spacing-10`: 2.25rem (column gutters)
- `spacing-16`: 3.5rem (section separation)
- `spacing-20`: 4.5rem (top-level breathing room)

## Screen Structure

### Dashboard Overview
- Hero section with greeting and action buttons
- Quick Actions cards (Create Category, Add Product, Add Event)
- Sales Trajectory bar chart
- Pulse Metric cards (Net Revenue, Active Orders)
- Recent Products list
- Upcoming Events list

### Product Management
- Page header with "New Category" button
- Category sections with product grids/tables
- Product cards with stock badges and progress bars
- Quick Add placeholder cards
- Bottom pulse cards (Total Valuation, Inventory Status, Category Distribution)

## Navigation

- Sidebar: Home, Products, Analytics, Events, Settings, Shop Profile
- Click navigation updates active state
- Search bar triggers global search
- Profile chip shows user menu

## Customization

### Adding New Screens

1. Create controller in `pages/` package:
```java
public class NewScreenController implements AppNavigator.ScreenLifecycle {
    private final AppNavigator navigator;
    
    public NewScreenController(AppNavigator navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public Parent getView() { /* build and return view */ }
    
    @Override
    public void onScreenShown() { /* refresh data */ }
}
```

2. Register in `AppNavigator.createScreen()`:
```java
case NEW_SCREEN:
    NewScreenController controller = new NewScreenController(this);
    return new ScreenContext(controller.getView(), controller);
```

3. Add navigation item in `SidebarComponent`:
```java
addNavItem(Screen.NEW_SCREEN, "🆕", "New Screen");
```

### Adding Services to Controllers

Each controller has service setter methods:

```java
public void setMyService(MyService service) {
    this.myService = service;
    loadData();
}
```

Set services via `MainFrame.setServices()` before showing the UI.

## Troubleshooting

**JavaFX not found**:
- Ensure Java 17+ is installed
- Check Maven resolved JavaFX dependencies
- Try: `mvn dependency:tree | grep javafx`

**Scene not displaying**:
- Check JavaFX module path
- Add VM arguments: `--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED`

**Styles not applied**:
- Ensure `styles.css` is in resources folder
- Check CSS path in `Main.java`

## License

Proprietary - Velora Commerce Platform
