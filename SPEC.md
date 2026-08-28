# Hybrid Canvas — Specification

A zoomable, pannable JavaFX `Canvas` + minimal `Pane` overlay that renders 20,000+ geometric shapes and images at interactive framerates, where only selection chrome lives as real JavaFX Nodes. Drop-in replacement for `Pane` (positional only).

## 1. Goals & Constraints

| Goal | Requirement |
|---|---|
| Scale | 20,000+ objects, scale to 100k+ noted |
| FPS | Smooth interaction during pan/zoom/drag/select on a representative machine |
| Drop-in | `HybridCanvas extends Pane`; sits in `ScrollPane`/`BorderPane` center unchanged |
| Single-thread safe | All scene-graph/Canvas ops on the JavaFX Application Thread |

Non-goals (v1): persistence, undo/redo, keyboard shortcuts, rulers, search/filter, touch/pinch-zoom.

## 2. Architecture — Model / View Separation

```
Consumer  ──addObject(HybridCanvasElement)──>  ModelStore  ──observed by──>  HybridCanvas (view)
                                                    │
                                                    ├──RTree (world bounds)────Hit testing & culling
                                                    └──ImageStore (view-side)──Async image decode + LRU + mipmap
```

- **Model = POJOs in memory** (`HybridCanvasElement` hierarchy + `HybridCanvasLayer`/`HybridCanvasGroup` — see §4). No persistence, no serialization built-in — mirrors how a plain `Pane` holds shapes.
- **View observes model** — any mutation (drag, panel edit, vertex tweak) goes through the model setters, which bump the per-element `version`; the view dirty-checks `version` and redraws/chrome-reflows as a reaction. Single source of truth; no view-side state drift.
- **No parent back-reference, no version bubbling** — elements are self-contained; the render traversal propagates group-transform changes to descendant redraws (§4.8).
- **View-side concerns owned by the view** — the R-tree holds *world* bounds (computed by the render traversal); the `ImageStore` holds all runtime image state (load state, decoded `Image`, mips). The model never touches world coords or decoded images.
- Consumers build/hold the model; the component never owns it.

## 3. Public API (`HybridCanvas extends Pane`)

### 3.1 Model operations
```java
addObject(HybridCanvasElement)                       // add to a layer
removeObject(HybridCanvasElement)
getLayers() : List<HybridCanvasLayer>                 // layer list (one "default" auto-created)
addLayer(HybridCanvasLayer) / removeLayer(...)
getElements() : ObservableList<HybridCanvasElement>   // read-only observable view across layers
clear()
getObjectAt(Point2D screenPt) : HybridCanvasElement    // hit-test (R-tree + containsLocal)
```
`getChildren()` is **reserved for the thin chrome overlay only** — calling it for scene objects is unsupported; migration from `Pane.getChildren().add(node)` requires changing to `addObject`. Scene objects are typed `HybridCanvasElement` subclasses (§4), not generic `Node`s.

### 3.2 Event API (mixed)
**Per-gesture listeners (actions):**
```java
setOnObjectClicked(handler)          // CanvasElementEvent{element(s), screenPt, modifiers}
setOnObjectDoubleClicked(handler)
setOnContextRequested(handler)
setOnObjectDragStarted / Dragged / Dropped(handler)
```
**Observable properties (state):**
```java
selectedObjectsProperty()  : ObservableList<HybridCanvasElement>
hoveredObjectProperty()     : ReadOnlyObjectProperty<HybridCanvasElement>
```
No per-object handler maps (zero per-object cost at 20k).

### 3.3 Configuration
```java
setZoomLimits(double min, double max)        // -1 = infinite on an axis
setMaxSelected(int cap)                      // default e.g. 50
setSeparateSelection(boolean)                // per-object chrome toggle (default false = group)
setAutoSwitchToGroupOnCap(boolean)           // default true
setGridVisible / setSnapToGrid / setAlignGuides / setMinimap(boolean)
setShowTooltips / setHighlightOnHover / setCursorOnHover(boolean)
setContextMenuItems(List<MenuItem>)          // consumer extends/overrides
setTooltipTextProvider(Function<HybridCanvasElement,String>)  // default = id + " " + type (or className)
```

## 4. Data Model

Typed class hierarchy (sealed, Java 21+) replaces the flat `CanvasObject` + `type` enum. No per-object `null` field bloat; dispatch is exhaustive pattern-match.

### 4.1 Class Hierarchy (sealed)

```
HybridCanvasElement (abstract)        ← root; id, transform, visible, locked, opacity, metadata, parentId, version
├── HybridCanvasShape (abstract)      ← fill, stroke, strokeWidth, layerId, zOrder
│   ├── HybridCanvasPolygon             ← double[] points (closed)
│   ├── HybridCanvasPolyline            ← double[] points (open)
│   ├── HybridCanvasRectangle           ← x, y, w, h
│   ├── HybridCanvasEllipse             ← cx, cy, rx, ry
│   ├── HybridCanvasBezier              ← double[] ctrl pts (cubic/quad)
│   └── HybridCanvasImageShape (abstract)  ← imageRef (pure data key)
│       ├── HybridCanvasImageRect
│       ├── HybridCanvasImageInShape
│       └── HybridCanvasImageWithOverlay
└── HybridCanvasGroup                 ← children: List<HybridCanvasElement>, own transform (container, NOT a shape subtype)

HybridCanvasLayer { id, name, visible, opacity, locked, zOrder, elements: List<HybridCanvasElement> }
HybridCanvas holds: List<HybridCanvasLayer>   // one non-deletable "default" layer auto-created
```

Sealed interfaces (`permits …`) give exhaustive `switch` dispatch in the renderer with zero casts and zero visitor boilerplate. **Target: Java 21+.**

### 4.2 `HybridCanvasElement` (abstract base — common to shapes and groups)

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` | stable identity |
| `rotate` | `double` | radians (or deg — pick at build) |
| `scaleX`, `scaleY` | `double` | composed transform fields |
| `translateX`, `translateY` | `double` | composed transform fields |
| `visible` | `boolean` | render + hit-test skip when false |
| `locked` | `boolean` | non-selectable |
| `opacity` | `double` | 0..1 |
| `metadata` | `Map<String,Object>` | user-extensible; drives tooltip + properties panel |
| `parentId` | `UUID` / null | informational only — **not a back-ref**, no parent pointer held (see §4.8) |
| `version` | `volatile long` | bumped by every setter; the only dirty-check signal |

Transform is **composed of separate fields** (not a single `Affine`); the view composes them to an `Affine` on demand. Children store points in the **parent's local space**; world = ancestor-transform-chain ∘ child. Moving/rotating a group transforms all descendants with zero child mutation.

### 4.3 `HybridCanvasShape` (abstract — geometry carriers)

Carries appearance + layer/z fields: `fill (Paint|null)`, `stroke (Paint)`, `strokeWidth (double, WORLD units, scales with zoom)`, `layerId (String)`, `zOrder (int)`.

**Geometry contract (local-space only — model never sees world coords):**
```java
abstract Bounds2D getLocalBounds();        // intrinsic geometry bbox, mutable double minX/minY/maxX/maxY
abstract boolean containsLocal(Point2D pt); // point-in-shape test, LOCAL space
```
- `Bounds2D` is a custom mutable struct (not `javafx.geometry.Bounds`) — avoids per-call object allocation at 20k scale.
- No `contains(worldPt)`, no `getWorldBounds()` on the model — those are view concerns (§4.11 / §8).

**Point storage:** `double[]` flat interleaved `[x0,y0,x1,y1,…]` — memory floor, no per-point objects at rest. Accessors `getPoint(int)`, `setPoint(int,double,double)`, `getPointCount()` return transient `Point2D` views (short-lived, GC-friendly).

### 4.4 Concrete shapes — geometry specifics

| Subclass | Geometry |
|---|---|
| `HybridCanvasPolygon` | `double[]` points, closed (fill + stroke) |
| `HybridCanvasPolyline` | `double[]` points, open (stroke only) |
| `HybridCanvasRectangle` | `x,y,w,h` (2-corner; or treat as 4-point polygon — pick at build) |
| `HybridCanvasEllipse` | `cx,cy,rx,ry`; `containsLocal` uses ellipse equation |
| `HybridCanvasBezier` | `double[]` control points (cubic=4, quad=3) |
| `HybridCanvasImageRect` | image in axis-aligned bbox |
| `HybridCanvasImageInShape` | image clipped to a polygon mask |
| `HybridCanvasImageWithOverlay` | image + separate shape outline |

### 4.5 `HybridCanvasImageShape` (abstract — image-bearing)

Holds **only `imageRef`** (a pure data key: atlas-key string OR unique-image path). **No runtime image state on the model** — load state, decoded `Image`, mipmap levels all live in the view-side `ImageStore` (§4.9). Evicting an image is a store map-removal, not a model mutation.

### 4.6 `HybridCanvasGroup` (container — NOT a shape subtype)

`children: List<HybridCanvasElement>` (recursive — groups hold groups). Own transform (`rotate`/`scale`/`translate` on the base) transforms all descendants. `getLocalBounds()` = union of children's local bounds (transformed by each child's own transform). **Group is transformable and nestable but is not a `Shape`** — it has no fill/stroke/points of its own.

### 4.7 `HybridCanvasLayer`

`{ id, name, visible, opacity, locked, zOrder, elements: List<HybridCanvasElement> }`. Canvas always holds `List<HybridCanvasLayer>`; one non-deletable `"default"` layer is auto-created at construction. "Flat mode" = degenerate single-layer case (one rendering path, no flat-vs-layered branch).

### 4.8 Model → View contract (dirty-checking)

- **Plain POJO fields** — no JavaFX `Property` wrappers (honors the POJO decision; ~0 memory overhead at 20k).
- **Per-element `version` (`volatile long`)** bumped by every setter — the *only* dirty signal.
- **No parent back-reference, no version bubbling.** Elements are self-contained; re-parenting doesn't fix back-refs (there are none).
- **Render-side traversal owns propagation:** when a group's `version` bumps (transform changed), the view's tree walk marks all descendants for redraw. The model's job is "I changed"; the view's job is "what that means for rendering."
- Properties panel writes-through to the model (setters bump `version`) and re-reads the selected object when its `version` changes.

### 4.9 `ImageStore` (view-side — owns all runtime image state)

| Concern | Strategy |
|---|---|
| Load state | `PLACEHOLDER` / `LOADING` / `LOADED` / `FAILED` per `imageRef` |
| Async decode | Dedicated bounded `ExecutorService` (4–8 threads, capped queue) |
| Upload | `Platform.runLater` to push decoded `Image` into the entry |
| Atlas (shared sprites) | Keyed by string; never evicted |
| Unique cache | Hard cap **256 MB** (default, configurable); LRU-evict invisible objects' images first |
| Mipmap LOD | Pre-generate down to 16×16; pick level matching current zoom |
| Eviction | Map removal — no model mutation, no `version` bump, no redraw storm |

Renderer queries `store.get(imageRef)` each draw; store returns placeholder + triggers async load if absent.

### 4.10 Renderer contract (sealed + pattern-match)

```java
sealed interface HybridCanvasShape permits Polygon, Polyline, Rectangle, Ellipse, Bezier, ImageShape { … }
sealed interface HybridCanvasElement permits HybridCanvasShape, HybridCanvasGroup { … }

// view-side, exhaustive switch — no visitor, no cast, no instanceof
void draw(GraphicsContext gc, HybridCanvasElement e, Viewport vp) {
    switch (e) {
        case HybridCanvasGroup g      -> drawGroup(g, vp);
        case HybridCanvasPolygon p    -> drawPolygon(gc, p, vp);
        case HybridCanvasPolyline pl  -> drawPolyline(gc, pl, vp);
        case HybridCanvasRectangle r  -> drawRect(gc, r, vp);
        case HybridCanvasEllipse el   -> drawEllipse(gc, el, vp);
        case HybridCanvasBezier bz    -> drawBezier(gc, bz, vp);
        case HybridCanvasImageRect ir      -> drawImageRect(gc, ir, vp);
        case HybridCanvasImageInShape iis  -> drawImageInShape(gc, iis, vp);
        case HybridCanvasImageWithOverlay io -> drawImageWithOverlay(gc, io, vp);
    }
}
```
Model imports no `javafx.graphics` types. Adding a shape subclass = edit the `permits` list + add a `case` (acceptable for a stable shape taxonomy).

### 4.11 Spatial index contract (R-tree — view-side, world bounds)

- The **R-tree stores world bounds** (computed by the render traversal's transform stack), not local bounds. O(log n) query regardless of object size; handles huge objects without degradation.
- **Incremental update on gesture commit** (§8): remove old world bbox, insert new. During a drag the moved object is dropped from the index (hit-tests skip it); re-inserted on commit.
- **Structural ops (add/remove/reparent subtree):** walk the affected subtree, remove every descendant's old world-bbox entry, insert every descendant's new world-bbox entry. O(subtree) per op — acceptable since structural changes are rare. Keeps the R-tree consistent at all times (no ghost bounds).
- **Bulk-load at startup:** chunked on the FX thread (~500/frame via `AnimationTimer`) for progressive appearance without UI freeze.
- **Hit-test flow:** screen P → world W → R-tree query (world bounds) → candidates by z-order → for each, view converts W→local via cached ancestor transform → `containsLocal(localPt)`. Model never touches world coords.

### 4.12 Java / JavaFX version

- **Java 21+** (sealed + pattern-match `switch`).
- JavaFX version: developer's choice at build (≥ 21 recommended to match).

## 5. Coordinate Space

- **Large bounded canvas** (effectively "big enough" virtual surface).
- **Fixed world coordinates** relative to origin `(0,0)`; object points stored in world units regardless of zoom.
- **Zoom** scales a viewport region; object coords never change on zoom/pan (only the transform/view does).
- **Zoom range**: `-1` = a given axis is unlimited; else `[min,max]` pair. Default: infinite both axes (or sensible floors like 0.01x / 100x if float math starts to break — leave configurable).

## 6. Rendering Pipeline

### 6.1 Canvas (the 20k objects)
- **Strategy: full-clear + redraw visible objects per frame.** Backed by R-tree culling, the visible set stays bounded (the "assume objects may be huge / zoom deep = few visible" assumption makes this sufficient).
- Redraw on: zoom, pan, model mutation, gesture commit.
- Upgrade paths noted, **not built**: dirty-rect with clip region (costs transparency-overlap handling), offscreen buffer + blit for pan (costs memory at high DPI + buffer re-render on zoom).

### 6.2 Image handling
- **Async load + placeholder.** Object renders a placeholder (grey rect / shape outline) immediately; image decodes off-thread, uploads via `Platform.runLater`, fades in when ready. Canvas never blocks on I/O.
- **Mipmap LOD** — pre-generate mips (down to 16x16); pick level matching current zoom to minimize bandwidth at draw time.
- Loading executor: dedicated bounded `ExecutorService` (4-8 threads) with a capped queue so 1000 simultaneous misses don't spawn 1000 tasks.

### 6.3 Shape LOD
- **Douglas-Peucker / pre-built decimations** — when a 200-point polygon is zoomed to a few px, draw a simplified point set. Precompute cost on insertion/geometry-change. *(Flag: adds precompute; only beneficial if profiling shows point count is the bottleneck.)*

### 6.4 Stroke scaling
- `strokeWidth` in world units — scales with zoom (true to world).

### 6.5 Renderer dispatch (sealed + pattern-match)
- The `ShapeRenderer` (view-side) dispatches on the sealed `HybridCanvasElement` / `HybridCanvasShape` hierarchies via exhaustive `switch` — **no visitor, no cast, no `instanceof`**, compiler-checked exhaustiveness (see §4.10). The model imports no `javafx.graphics` types; geometry (`getLocalBounds()`, `containsLocal()`) lives on the model, pixel-drawing lives in the renderer.
- The render traversal maintains the transform stack (ancestor chain) so it can compute each element's **world bounds** for the R-tree and convert hit-test world points to local space per candidate.

## 7. Thin Pane Overlay (the "minimal Pane")

Real JavaFX `Node`s live here. **Fixed screen-pixel size** for all handles (always grabbable regardless of zoom — handles undo the zoom scale for draw + hit-test). Chrome inventory:

| Chrome | Triggers |
|---|---|
| Resize handles (8 = 4 corners + 4 edges) | selection |
| Rotate handle (single, above bbox) | selection |
| Rubber-band selection rectangle | drag on empty space |
| Per-vertex edit handles | exactly 1 selected + vertex-edit mode (dbl-click) |
| Hover highlight | hover (toggleable) |
| Tooltip popup | hover (toggleable) |
| Context menu | right-click |

### 7.1 Selection modes
- **Default = group transform** (>=2 selected): single shared bbox with 8 resize handles + 1 rotate handle; transforms all selected together.
- **Separate mode** (`setSeparateSelection(true)`): each selected object gets its own chrome.
- **Max-selection cap** (`setMaxSelected(N)`): when reached and user adds more, **auto-switch to group mode** by default (keeps chrome count bounded as selection grows to hundreds). Toggleable off via `setAutoSwitchToGroupOnCap(false)`.

### 7.2 Transform specifics
- **Move** — drag body.
- **Resize** — corner/edge handles scale the bbox; shape follows.
- **Rotate** — single rotate handle, **rotation around combined-bbox center** (fixed pivot, no movable pivot).
- **Polygon/polyline transform** uses the **bounding box** (not per-vertex), except in vertex-edit mode where per-vertex handles reshape geometry.
- **Handles are bound to their object** — handles hit-test as part of their owning object.

### 7.3 Promoted-to-Node strategy (the "always-on" decision)
Each *selected* object is a real Node for as long as it's selected (committed back to canvas on deselect). **Caveat baked into spec:** per-object chrome cost = 9 handles x selected count; at large selection counts this re-introduces the very Node-bloat the hybrid model avoids — that's exactly why group mode is default + cap auto-switches. The model stays safe; per-object mode is opt-in for small selections.

`// ponytail: cap of ~50 for per-object chrome; above = group mode. Upgrade path: per-object with incremental chrome if profiling allows.`

## 8. Spatial Index — R-tree (view-side, world bounds)

- **Data structure: R-tree** (correct for mixed small + huge object bboxes at 20k+; O(log n) query regardless of object size). ~150 LOC or one dependency — dev's choice at build time.
- **The R-tree stores world bounds** (not local bounds) — computed by the render traversal's transform stack (§4.11). The model exposes only `getLocalBounds()`; the view computes world bounds for indexing.
- **Huge objects** (pages-worth of screen): R-tree handles without degradation (unlike naive quadtree leaf insertion).
- **Update model: incremental.** On gesture commit (drag/resize/rotate/vertex-edit end): remove the object's old world bbox, insert the new one. **During a drag, the dragged object is dropped from the index** (hit-tests skip it — the thing being dragged shouldn't be a hit-target anyway); re-inserted on commit.
- **Structural ops (add/remove/reparent subtree):** walk the affected subtree, remove every descendant's old world-bbox entry, insert every descendant's new world-bbox entry. O(subtree) per op — acceptable since structural changes are rare. Keeps the R-tree consistent at all times (no ghost bounds for hit-tests).
- **Bulk-load (startup, 20k+):** chunked on the FX thread (e.g., 500/frame via `AnimationTimer`) for progressive scene appearance without freezing.
- **Hit-test flow:** screen P → world W → R-tree query (world bounds) → candidates by z-order → for each, view converts W→local via cached ancestor transform → `element.containsLocal(localPt)`. Model never touches world coords.

## 9. Interaction Bindings

| Action | Binding |
|---|---|
| Zoom | Mouse wheel, anchored to cursor (world point under mouse stays fixed) |
| Pan | Scrollbars (always available, follow zoom/pan) + **middle-button drag** |
| Select single | Left-click |
| Multi-select add/remove | Ctrl/Shift + click |
| Rubber-band | Plain left-drag on empty space (default) |
| Rot. / resz. / move | Drag body / handles |
| Vertex edit | Double-click (overridable via `setOnObjectDoubleClicked` to open properties panel) |
| Context menu | Right-click |
| Hover | Highlight + cursor + tooltip (each toggleable) |

## 10. Context Menu

- **Built-in items:** Delete, Duplicate, Bring to Front, Send to Back, Toggle Lock, Properties.
- **Extensibility:** `setContextMenuItems(List<MenuItem>)` — consumer mutates/extends or overrides entirely. Built-ins provided as defaults the consumer can remove.

## 11. Properties Panel

Side dock (usually right) showing the selected object's attributes as editable fields; edits mutate the model, view re-renders. Mirrors the data model:

- **Identity** — `id` (RO), `type` (RO = concrete subclass name), `metadata` key-value rows (editable; add/remove keys)
- **Geometry** — `points`/coords via `getPoint(i)` accessors (editable table; live-updates shape), `transform` = rotate + scaleX/scaleY + translateX/translateY (separate numeric fields)
- **Appearance** — `fill` picker, `stroke` picker, `strokeWidth` number (world units), `opacity` slider
- **State** — `visible`, `locked` checkboxes, `layerId` dropdown, `zOrder` number
- **Image** (if `HybridCanvasImageShape`) — `imageRef` key/path with browse/replace button (load state shown read-only from the `ImageStore`)

Multi-select: shared fields only, "mixed" indicator on differing fields, edit applies to all selected. No selection: panel empty / shows canvas-level settings (zoom, grid, layer list).

## 12. Optional Features (activable via `set...Visible/Enabled`)

| Feature | Notes |
|---|---|
| Grid + background | visible grid (lines/dots) behind objects; snap-to-grid toggle (separate on/off) |
| Snap-to-objects / alignment guides | smart guides (red/green) when edges/centers align during drag; non-trivial — scan visible objects' edges |
| Minimap / overview | inset showing whole canvas + viewport rectangle; click to jump; big win at 20k scenes |

Touch/pinch-zoom: **deferred**. Keyboard shortcuts, rulers, search/filter: **out of scope** (rulers can be drawn as objects if needed).

## 13. Threading Model

| Concern | Strategy |
|---|---|
| Image decode | Dedicated bounded `ExecutorService` (4-8 threads, capped queue); upload via `Platform.runLater` (owned by view-side `ImageStore`, §4.9) |
| R-tree update (commit) | Synchronous on FX thread (one remove + one insert of world bbox) |
| R-tree structural op | FX thread; walk subtree, remove/insert every descendant's world bbox (§4.11) |
| R-tree bulk-load (startup) | Chunked on FX thread (~500/frame via `AnimationTimer`) |
| Dirty-check | Per-element `volatile long version`; view compares versions during render pass — no listeners, no parent back-ref, no bubbling (§4.8) |
| Group-transform propagation | Render traversal marks descendants for redraw when a group's `version` bumps (§4.8) |
| Canvas redraw (drag) | FX thread every frame; bounded by index culling |

## 14. Performance Targets / Acceptance

Suggested gates:
- **Start-up:** 20k mixed objects (shapes + 1k unique images) opens with visible content within ~500 ms; full index build completes progressively without UI freeze.
- **Interaction:** panning/zooming a representative scene holds ~60 FPS; hit-test (hover) resolves in <1 ms (R-tree O(log n)).
- **Selection:** group-mode selection of 500 objects transforms smoothly; separate-mode is opt-in and the cap auto-switches before Node-bloat.

## 15. Open Items for the Developer (decisions at build time)

1. **Java 21+ required** — sealed types + pattern-match `switch` drive the renderer dispatch (§4.10) and the typed class hierarchy (§4.1). Pick a JavaFX version ≥ 21 to match.
2. **R-tree: hand-rolled ~150 LOC vs. dependency** — pick based on repo style; spec accepts either.
3. **Zoom float-safety floors** — if "infinite" zoom breaks matrix math, fall back to 0.01x / 100x defaults.
4. **Atlas vs unique-cache split** — consumer-driven; the view-side `ImageStore` exposes `registerAtlasImage(key, img)` / `getUniqueImage(ref)` (§4.9).
5. **Dirty-rect & offscreen-blit** — explicitly noted upgrades, not built day one; revisit if redraw profiling shows it's the bottleneck.
6. **Rotate unit** — radians vs degrees for `rotate` field (§4.2); pick at build and stay consistent.
