# Prompt 09 — view: ShapeRenderer (FX sealed switch, draw to GraphicsContext)

**Depends on:** 08 (RenderItem), 05 (image shapes), 01 (Transform2D), 00 (FX harness).

## Objective

Exhaustive pattern-match renderer for **non-image leaf shapes**. Pixel-drawing
lives ONLY here; the model stays graphics-free. Smoke-tested headless via
Monocle. Image shapes are placeholder-only at this prompt — real image lookup
arrives at Prompt 17.

## Context

- `com.hybridcanvas.view`. Builds on Prompt 08 (`RenderItem`).

## Tests first (red)

`src/test/java/com/hybridcanvas/view/ShapeRendererTest.java` (ApplicationTest,
Monocle):
- `draw` smoke for each non-image leaf shape: `Polygon`, `Polyline`,
  `Rectangle`, `Ellipse`, `Bezier` — call `renderer.draw(gc, item)` on a real
  `GraphicsContext` and assert no exception; assert a colored pixel exists for
  a solid filled `Rectangle` (read Canvas snapshot `PixelReader`).
- Image-shape render passes through `drawImagePlaceholder(gc, item)` and
  asserts a grey rect / shape outline is drawn for `ImageRect`,
  `ImageInShape`, `ImageWithOverlay`. No `ImageStore` access at this prompt.
- Switch exhaustiveness is compiler-enforced (sealed non-image leaf types
  only — `Group` is NOT in the switch; traversal pre-folds the transform).

## Implement (green)

- `ShapeRenderer`:
  ```java
  public final class ShapeRenderer {
    public void draw(GraphicsContext gc, RenderItem item);
    public void drawImagePlaceholder(GraphicsContext gc, RenderItem item);
  }
  ```
  Sealed `switch` over non-image leaf types:
  - `Polygon` → fill + stroke path (close polyline).
  - `Polyline` → stroke only.
  - `Rectangle` → `gc.fillRect/strokeRect`.
  - `Ellipse` → `gc.fillOval/strokeOval`.
  - `Bezier` → `gc.bezierCurveTo`/`quadraticCurveTo` (cubic vs quad by point
    count).
  Apply element opacity (from `RenderItem.effectiveOpacity`); map
  `HybridCanvasColor.toArgb()` → `Color`; `strokeWidth` in WORLD units (set gc
  transform to world→screen first, then `lineWidth = strokeWidth`).
- `drawImagePlaceholder` renders a grey rect / shape outline for each image
  type. Real image lookup arrives at Prompt 17.
- Map `HybridCanvasColor` → `javafx.scene.paint.Color` here.

## Wire / integrate

`HybridCanvas` (Prompt 10) calls this in its redraw loop. Image shapes degrade
to placeholder until Prompt 17 supplies real images via an `ImageStore`
parameter (signature widens at 17).

## Acceptance

`gradlew test` green (headless FX render smoke for non-image shapes).
