# Agent Prompt: Build Velora Business Management JavaFX GUI

## Your Task
You are building a **JavaFX desktop application** called **Velora Business Management** that faithfully replicates the provided UI design. The app has two main screens: a **Dashboard Overview** and a **Product Management** screen. You must implement every visual detail, layout structure, color token, and interaction behavior described below.

---

## Project Setup

- **Language:** Java 17+
- **Framework:** JavaFX 21
- **Build Tool:** Maven or Gradle (your choice)
- **Required Dependencies:**
  - JavaFX Controls, FXML, Graphics
  - A charting library: use **JavaFX built-in `BarChart`** styled with CSS
- **Fonts:** Embed `Manrope` (display/headlines) and `Inter` (body/labels) as resources. Load via `Font.loadFont()`. Fall back to `"Segoe UI"` if unavailable.

---

## Design System Tokens

Apply these **strictly** throughout. Never use hardcoded hex strings in Java code — define them all as JavaFX CSS variables in a central `styles.css`.

### Color Palette
```
--color-primary:                  #182034
--color-primary-container:        #2e354a
--color-secondary-container:      #d5e3fd
--color-on-secondary-container:   #57657b
--color-tertiary:                 #002336
--color-tertiary-fixed-dim:       #89ceff   /* accent for charts/interactive */
--color-surface:                  #f7f9fb
--color-surface-dim:              #d8dadc   /* hover state */
--color-surface-container-lowest: #ffffff
--color-surface-container-low:    #f2f4f6
--color-surface-container:        #eceef0
--color-surface-container-high:   #e6e8ea
--color-surface-container-highest:#e0e3e5
--color-on-surface:               #191c1e
--color-on-background:            #191c1e   /* NEVER use pure #000000 */
--color-outline-variant:          #c4c6cf
--color-secondary-fixed:          #d5e3fd
--color-on-secondary-fixed:       #0d1c2f
```

### Typography Scale
```
display-lg:   Manrope,  56px, weight 700
display-sm:   Manrope,  36px, weight 700
headline-lg:  Manrope,  28px, weight 700
headline-sm:  Manrope,  20px, weight 600
body-lg:      Inter,    16px, weight 400
body-md:      Inter,    14px, weight 400
label-lg:     Inter,    14px, weight 500
label-md:     Inter,    12px, weight 500
label-sm:     Inter,    11px, weight 500
```

### Spacing
```
spacing-4:  0.9rem  (list item vertical gap)
spacing-10: 2.25rem (gutter between columns)
spacing-16: 3.5rem  (major section separation)
spacing-20: 4.5rem  (top-level section breathing room)
```

### Roundedness
```
radius-sm:   6px   (inputs, chips)
radius-md:   8px   (buttons, cards)
radius-full: 9999px (pill chips/tags)
```

---

## The "No-Line" Rule (CRITICAL)
> **Never use visible borders to divide sections.** All boundaries must be defined purely by background color transitions.

- Main app background → `surface-container-low` (#f2f4f6)
- Content cards/panels → `surface-container-lowest` (#ffffff)
- The color shift IS the border — no `border` or `stroke` properties on containers.
- **Ghost Border exception:** Only in high-density data tables, use `outline-variant` (#c4c6cf) at **15% opacity** as a row separator. It must be barely visible.

---

## Application Layout (Shell)

### Window
- Minimum size: **1280 × 800px**
- Use a `BorderPane` as the root.
- Background: `surface-container-low` (#f2f4f6)

### Left Sidebar (`VBox`, fixed width 220px)
- Background: `surface-container-lowest` (#ffffff)
- **No right border** — the color contrast with the main area IS the separator.
- **Top section:** Logo area
  - Small square icon (blue gradient placeholder, 32×32px, radius-md)
  - Text: "Velora" in `headline-sm` (Manrope, bold, `on-surface`)
  - Subtext: "BUSINESS MANAGEMENT" in `label-sm` (Inter, `on-secondary-container`, letter-spacing: 0.08em)
- **Navigation items** (`VBox`, `spacing-4` between items):
  Each nav item is an `HBox` (full width, padding 12px 16px):
  - Icon (16×16 `ImageView` or SVG-style shape)
  - Label in `label-lg` (Inter, 14px)
  - **Active state:** left accent bar (3px wide, `tertiary-fixed-dim` #89ceff, full height), background `surface-container-low`
  - **Hover state:** background `surface-dim` (#d8dadc), cursor `HAND`
  - Nav items: Home, Products, Analytics, Events, Settings, Shop Profile
- **Bottom section:** User profile card
  - `HBox` with avatar (`Circle` clip, 36×36px), name in `label-lg`, role in `label-sm` (`on-secondary-container`)
  - Background: `surface-container-low`, radius-md, padding 12px

### Top Bar (`HBox`, full width, height 64px)
- Background: `surface-container-lowest` (#ffffff) at 80% opacity (glassmorphism)
- Left: Page title in `headline-sm` Manrope
- Center: Search bar (`TextField`)
  - Background: `surface-container-highest` (#e0e3e5), radius-sm
  - Placeholder: search hint text in `label-md`, color `outline-variant`
  - On focus: background → `surface-container-lowest`, 2px `primary` outline at 20% opacity
  - Prefix: magnifying glass icon
- Right (Product screen): Bell icon, Question mark icon, Profile chip

---

## Screen 1: Dashboard Overview

### Hero Section (`VBox`)
- Eyebrow: "MORNING, ALEX" — `label-sm`, uppercase, letter-spacing 0.1em, `on-secondary-container`
- Headline: "Your shop is performing **above target** today."
  - Use a `TextFlow` with multiple `Text` nodes
  - "above target" colored `tertiary-fixed-dim` (#89ceff)
  - Rest of text: `headline-lg` Manrope, `on-surface`
- Right: Two buttons
  - "Download Reports" — Secondary style
  - "Live View" ⚡ — Primary gradient style

### Quick Actions Section
- Title: "Quick Actions" — `headline-sm` Manrope
- Three equal cards in `HBox` (`spacing-10` gap):
  - Background `surface-container-lowest`, radius-md, padding 24px
  - Icon box: 40×40, `surface-container-low` bg, radius-md
  - Title: `label-lg` bold; Description: `body-md`, `on-secondary-container`
  - Cards: "Create Category", "Add Product", "Add Event"
  - Hover: background → `surface-dim`

### Analytics Row (`HBox`, full width)

#### Sales Trajectory Card (left, ~65% width)
- Background `surface-container-lowest`, radius-md, padding 24px
- Header: "Sales Trajectory" (`headline-sm`) + "+12.4%" pill chip (`secondary-fixed` bg)
- Subtext: `label-md`, `on-secondary-container`
- **Bar Chart** (JavaFX `BarChart`):
  - X-axis: MON, TUE, WED, THU, FRI, SAT
  - Bar fill: `tertiary-fixed-dim` (#89ceff) at 60% opacity; highlighted bar (SAT) at 100%
  - No grid lines or axis borders — only x-axis labels
  - Tooltip on active bar: dark pill showing value

#### Pulse Metric Cards (right, ~35% width, stacked)
**NET REVENUE Card:**
- Background `surface-container-lowest`, radius-md, padding 20px
- Asymmetric Pulse layout (diagonal visual flow):
  - Top-left: "NET REVENUE" label (`label-sm`, uppercase, `on-secondary-container`)
  - Top-right: icon (28×28, `surface-container-low`)
  - Middle: "$12,480" — `display-sm` Manrope bold
  - Bottom-right: "+4.2%" chip (`secondary-fixed` bg, radius-full)

**ACTIVE ORDERS Card:** Same layout, value "84", trend "-2.1%"

### Bottom Row (`HBox`)

#### Recent Products (left ~50%)
- Header: "Recent Products" + "View All" link (`primary` color)
- List (`VBox`, `spacing-4`): thumbnail (48×48, radius-sm) + name + category + price + stock count chip
- **No dividers** — vertical spacing only

#### Upcoming Events (right ~50%)
- Header: "Upcoming Events" + "Calendar View" link
- Each event: date block (month `label-sm` + day `headline-sm`) + event details + status chip
- Status chips: "Active Soon" (`secondary-fixed` bg), "Planning" (`surface-container-high` bg)

---

## Screen 2: Product Management

### Page Header
- Title: "Product Management" — `headline-lg` Manrope
- Subtitle: `body-md`, `on-secondary-container`
- Right: "＋ New Category" — Primary gradient button

### Category Section (two categories shown)
Container: `VBox`, background `surface-container-lowest`, radius-md, padding 24px

**Category Header (`HBox`):**
- Icon: 36×36, `secondary-fixed` bg, radius-md
- Name (`headline-sm` bold) + count (`label-sm`, uppercase, `on-secondary-container`)
- "＋ Add Product" — Secondary button

**Premium Collection — Card Grid (3 columns `HBox`):**
Each product card (`VBox`, ambient shadow: `0 12px 32px rgba(25,28,30,0.06)`):
- Product image: full width, ~180px tall, radius-md clip at top
- "IN STOCK" badge: pill chip top-right (`secondary-fixed` bg, `on-secondary-fixed` text)
- Name + price: `label-lg` bold
- Progress bar: 4px tall, `tertiary-fixed-dim` fill, radius-full, + units label

**"Quick Add Product" placeholder card:**
- Dashed border (`outline-variant` at 40% opacity), `surface-container-low` bg
- Centered icon + title + description
- Hover: background → `surface-dim`

**Lifestyle Essentials — Table/List View:**
- Headers: PRODUCT DETAILS, STATUS, PRICE, ACTIONS — `label-sm` uppercase, `on-secondary-container`
- Each row (`HBox`, hover bg `surface-container-low`):
  - Thumbnail 40×40, radius-sm
  - Name (`label-lg` bold) + "Category • Variant" (`label-sm`)
  - Status chips: ACTIVE (green), LOW STOCK (orange), DRAFT (grey) — radius-full
  - Price: `label-lg` bold
  - Edit + Delete icon buttons
  - Row separator: Ghost Border (`outline-variant` at 15% opacity)

### Bottom Pulse Cards Row (3 equal cards)
1. **Total Valuation** — light bg, "$142,500" (`display-sm`), "+12.4%" chip
2. **Inventory Status** — light bg, "1,204" (`display-sm`), "Optimal" green chip
3. **Category Distribution** — **dark card** (`primary` #182034 bg), white text, "06" (`display-sm` white), "Manage Hierarchies" link (`tertiary-fixed-dim` color)

---

## Component CSS (define in `styles.css`)

### Buttons
```css
.btn-primary {
    -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #182034, #2e354a);
    -fx-text-fill: white;
    -fx-background-radius: 8;
    -fx-padding: 10 20 10 20;
    -fx-font-family: "Inter";
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-cursor: hand;
}
.btn-primary:hover {
    -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2e354a, #3d4560);
}
.btn-secondary {
    -fx-background-color: #f2f4f6;
    -fx-text-fill: #191c1e;
    -fx-background-radius: 8;
    -fx-padding: 10 20 10 20;
    -fx-font-family: "Inter";
    -fx-font-size: 14px;
    -fx-cursor: hand;
}
.btn-secondary:hover { -fx-background-color: #d8dadc; }
```

### Chips
```css
.chip-blue   { -fx-background-color: #d5e3fd; -fx-text-fill: #0d1c2f; -fx-background-radius: 9999; -fx-padding: 3 10 3 10; -fx-font-size: 11px; }
.chip-green  { -fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 9999; -fx-padding: 3 10 3 10; }
.chip-orange { -fx-background-color: #fff3e0; -fx-text-fill: #e65100; -fx-background-radius: 9999; -fx-padding: 3 10 3 10; }
.chip-grey   { -fx-background-color: #e6e8ea; -fx-text-fill: #57657b; -fx-background-radius: 9999; -fx-padding: 3 10 3 10; }
```

### Cards
```css
.card {
    -fx-background-color: #ffffff;
    -fx-background-radius: 8;
    -fx-effect: dropshadow(gaussian, rgba(25,28,30,0.06), 32, 0, 0, 12);
}
.card:hover { -fx-background-color: #f2f4f6; }
```

### Input Fields
```css
.search-field {
    -fx-background-color: #e0e3e5;
    -fx-background-radius: 6;
    -fx-font-family: "Inter";
    -fx-font-size: 13px;
    -fx-padding: 8 14 8 36;
}
.search-field:focused {
    -fx-background-color: #ffffff;
    -fx-border-color: rgba(24,32,52,0.2);
    -fx-border-width: 2;
    -fx-border-radius: 6;
}
```

---

## Navigation Between Screens
- Clicking "Products" in the sidebar → loads Product Management into `BorderPane` center
- Clicking "Home" → loads Dashboard Overview
- Use a `StackPane` or center-swap pattern — **do NOT open new windows**
- Active sidebar item must update highlight state on navigation

---

## Do's and Don'ts

### DO
- Use `spacing-16` (56px) and `spacing-20` (72px) between major sections — white space is intentional.
- Use `tertiary-fixed-dim` (#89ceff) for chart accents, interactive highlights, and links.
- Always use `on-background` (#191c1e) for text — never pure `#000000`.
- Use `surface-dim` (#d8dadc) for all hover states on interactive surfaces.

### DON'T
- Don't add visible borders around cards or sidebar.
- Don't use standard drop shadows on cards — use the ambient shadow spec only.
- Don't use rigid grids — allow generous gutters so content breathes.
- Don't use bold weight on body/label text unless specified above.

---

## Deliverables
1. Full Maven/Gradle project structure
2. `Main.java` — JavaFX entry point
3. `DashboardController.java` + layout (FXML or pure code)
4. `ProductController.java` + layout (FXML or pure code)
5. `styles.css` — all tokens and component classes
6. `AppNavigator.java` — screen switching logic
7. Hardcoded sample data matching the design screenshots
8. `README.md` — build and run instructions