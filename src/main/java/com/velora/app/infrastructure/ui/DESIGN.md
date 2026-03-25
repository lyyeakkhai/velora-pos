# Design System Strategy: The Architectural Intelligence

## 1. Overview & Creative North Star
This design system moves away from the "generic SaaS dashboard" and toward **The Architectural Intelligence**. The goal is to create a digital environment that feels like a high-end, bespoke office—quiet, authoritative, and impeccably organized.

We achieve a premium feel by rejecting standard UI tropes like heavy borders and flat grey containers. Instead, we lean into **Editorial Layering**. By using high-contrast typography scales (Manrope for displays and Inter for utility) and a monochromatic depth strategy, we create a system that doesn't just display data—it curates it. The layout utilizes intentional asymmetry and vast "breathing room" (white space) to signal that the user is in a space of high-value decision-making, not just task-ticking.

---

## 2. Color & Tonal Depth
Our palette is rooted in `primary` (#182034) and `tertiary` (#002336), creating a deep, "Midnight Blue" foundation that communicates absolute trust.

### The "No-Line" Rule
Standard 1px borders are strictly prohibited for sectioning. They clutter the interface and feel "cheap." Instead, boundaries must be defined solely through background color shifts.
*   **The Transition:** Use `surface_container_low` (#f2f4f6) for the main background and `surface_container_lowest` (#ffffff) for primary content areas. The change in hex value is the border.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers. Use the `surface_container` tiers to define "importance" through elevation:
*   **Base:** `surface` (#f7f9fb)
*   **Sectioning:** `surface_container_low` (#f2f4f6)
*   **Active Content/Cards:** `surface_container_lowest` (#ffffff)
*   **Popovers/Modals:** `surface_container_high` (#e6e8ea)

### The Glass & Gradient Rule
To prevent the deep blues from feeling "heavy," apply glassmorphism to floating navigation or utility panels. Use `surface_container_lowest` at 80% opacity with a `backdrop-blur` of 20px. 
*   **Signature Textures:** For high-priority Call to Actions (CTAs), apply a subtle linear gradient from `primary` (#182034) to `primary_container` (#2e354a) at a 135-degree angle. This adds a "silk" sheen that flat colors cannot replicate.

---

## 3. Typography: Editorial Authority
We utilize a dual-font strategy to balance character with utility.

*   **Display & Headlines (Manrope):** Chosen for its geometric precision and modern "tech-executive" feel. Use `display-lg` (3.5rem) sparingly for high-level data summaries to create a focal point.
*   **Body & Labels (Inter):** Chosen for its unrivaled legibility in data-dense environments. 
*   **The Hierarchy Rule:** Always pair a `headline-sm` in Manrope with a `label-md` in Inter to create an immediate "Editorial" contrast. This helps the user distinguish between "Status" (Label) and "Subject" (Headline).

---

## 4. Elevation & Depth
In this system, depth is "felt" rather than "seen."

*   **The Layering Principle:** Stacking is our primary tool. A `surface_container_lowest` card sitting on a `surface_container_low` background creates a natural lift.
*   **Ambient Shadows:** For floating elements (Modals/Dropdowns), use a shadow color derived from `on_surface` (#191c1e). 
    *   *Spec:* `0 12px 32px rgba(25, 28, 30, 0.06)`. It should look like a soft atmospheric glow, not a dark smudge.
*   **The "Ghost Border" Fallback:** If a divider is functionally required for high-density data, use the `outline_variant` (#c4c6cf) token at 15% opacity. It should be barely visible—a "whisper" of a line.

---

## 5. Components

### Buttons
*   **Primary:** Gradient of `primary` to `primary_container`. `md` (0.375rem) roundedness.
*   **Secondary:** Solid `secondary_container` (#d5e3fd) with `on_secondary_container` (#57657b) text. No border.
*   **Tertiary:** Text-only using `primary`. Use for low-emphasis actions like "Cancel."

### Input Fields
*   **Structure:** Background must be `surface_container_highest` (#e0e3e5). 
*   **Interaction:** On focus, transition the background to `surface_container_lowest` and add a 2px `primary` "Ghost Border" at 20% opacity. 

### Cards & Lists
*   **Forbid Dividers:** Do not use lines between list items. Use a vertical spacing of `spacing.4` (0.9rem) and a slight hover state change to `surface_container_low`.
*   **Data Chips:** Use `secondary_fixed` (#d5e3fd) backgrounds with `on_secondary_fixed` (#0d1c2f) text. Keep `roundedness.full` for a modern, pill-shaped look.

### The "Pulse" Dashboard Card (Custom Component)
For business management, use a "Pulse" card: A `surface_container_lowest` container with an asymmetrical header. Place the metric (`display-sm`) in the top-left and the trend indicator (`label-sm`) in the bottom-right, creating a diagonal visual flow that breaks the standard "top-to-bottom" grid.

---

## 6. Do’s and Don’ts

### Do
*   **Do** use `spacing.16` (3.5rem) and `spacing.20` (4.5rem) to separate major sections. Air is a luxury—use it.
*   **Do** use `tertiary_fixed_dim` (#89ceff) as an accent color for interactive data points (charts/graphs) against the deep blue backgrounds.
*   **Do** ensure all interactive elements have a clear `surface_dim` (#d8dadc) state on hover.

### Don't
*   **Don't** use pure black (#000000). Always use `on_background` (#191c1e) to maintain a soft, premium feel.
*   **Don't** use "Drop Shadows" on cards. Use tonal layering (`surface_container` shifts) to define the card's edge.
*   **Don't** use standard 12-column grids strictly. Allow for wide gutters (e.g., `spacing.10`) to allow data to breathe.