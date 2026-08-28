# Prompt 07 — store: ModelStore (layers, default layer, observable getElements)

**Depends on:** 04 (layer), 02–05 (model).

## Objective

Model-side container the view observes. Holds layers, auto-creates the
non-deletable `"default"` layer, exposes a read-only observable element view.
`javafx.base` only.

## Context

- `com.hybridcanvas.store` (new). Builds on Prompts 02–05.

## Tests first (red)

- `ModelStoreTest` — constructed with one `"default"` layer (non-deletable:
  `removeLayer(default)` throws or is refused); `addObject(e)` lands in the
  default layer and `e` appears in `getElements()`; `addObject(layer,e)` honors
  the layer; `removeObject(e)` removes from both layer and `getElements()`;
  `clear()` empties layers' elements but keeps the default layer;
  `getElements()` is observable (listener fires on add/remove); `getElements()`
  reflects across all layers in z-order.

## Implement (green)

- `ModelStore`:
  ```java
  public final class ModelStore {
    public List<HybridCanvasLayer> getLayers();
    public void addLayer(HybridCanvasLayer layer);
    public void removeLayer(HybridCanvasLayer layer);
    public ObservableList<HybridCanvasElement> getElements();
    public void addObject(HybridCanvasElement e);
    public void addObject(HybridCanvasLayer layer, HybridCanvasElement e);
    public void removeObject(HybridCanvasElement e);
    public void clear();
  }
  ```
  Default layer auto-created; `getElements()` is a read-only view built from
  `FXCollections`, kept in sync across layers.
- `// ponytail: single ObservableList mutated on structural ops rather than a
  composed view — recompute order on reorder if needed.`

## Wire / integrate

`HybridCanvas` (Prompt 10) owns a `ModelStore` and exposes the §3.1 API by
delegation; traversal (Prompt 08) reads layers.

## Acceptance

`gradlew test` green.
