# Prompt 10 — view: HybridCanvas core (Canvas + redraw pipeline)

**Depends on:** 07 (store), 08 (traversal), 09 (renderer), 00 (demo stub).

## Objective

`HybridCanvas extends Pane`, owns a `Canvas` (scene objects) + a thin chrome
overlay `Pane`, wraps `ModelStore`, and redraws via traversal + renderer. Full
§3.1 model ops exposed by delegation. Flips the DemoApp Hybrid mode live.

## Context

- `com.hybridcanvas.view`. Builds on Prompts 07–09. Replaces the Prompt 00 stub
  `view/HybridCanvas.java`.

## Tests first (red)

- `HybridCanvasTest` (FxSmoke + headless model-level):
  - construct; `addObject` appears in `getElements()`; default layer exists in
    `getLayers()`.
  - `getChildren()` contains ONLY the canvas + chrome overlay (assert scene
    objects are NOT Nodes); `clear()` empties `getElements()`.
  - render pass after `addObject` does not throw; `resize(width,height)`
    resizes the internal Canvas to match.
- Culling smoke: add 100 shapes spread wide, narrow viewport, assert the render
  pass touches fewer than 100 (expose a counter or use the traversal directly
  in a pure test).

## Implement (green)

- `HybridCanvas extends Pane`:
  ```java
  public class HybridCanvas extends Pane {
    // §3.1 model ops
    public void addObject(HybridCanvasElement e);
    public void removeObject(HybridCanvasElement e);
    public List<HybridCanvasLayer> getLayers();
    public void addLayer(HybridCanvasLayer l);
    public void removeLayer(HybridCanvasLayer l);
    public ObservableList<HybridCanvasElement> getElements();
    public void clear();
    public HybridCanvasElement getObjectAt(javafx.geometry.Point2D screenPt); // stub -> null until Prompt 11
    // §3.3 config setters (no-op flags until wired, plus the new worldSize)
    public void setZoomLimits(double min, double max);
    public void setWorldSize(double width, double height);   // new (build decision C)
    public javafx.geometry.Dimension2D getWorldSize();
  }
  ```
  Fields: `ModelStore store`, `Canvas canvas`, `Pane chrome`, `ShapeRenderer
  renderer`, `RenderTraversal traversal`, `Viewport viewport`, `double
  worldWidth, worldHeight` (default 0,0 until set). No `ImageStore` yet —
  introduced in Prompt 17.
- `markDirty()` → schedule a render via `Platform.runLater` (coalesce);
  `render()`: clear canvas, run traversal with current viewport, draw items.
- `getChildren()` reserved: add canvas + chrome only; document that scene
  objects never become Nodes.

## Wire / integrate

- Ties traversal (08) + renderer (09) + store (07) together.
- DemoApp: replace the empty Hybrid stub usage — Hybrid mode now generates
  `HybridCanvasElement`s and calls `addObject`, so the toggle renders the same
  random objects both ways.

## Acceptance

`gradlew test` green; `gradlew run` renders shapes in Hybrid mode.
