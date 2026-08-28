# Architecture

Condensed from [SPEC.md](SPEC.md) — read that for full detail. Package layout and phase mapping: [plans/README.md](plans/README.md).

## Overview

```
Consumer ──addObject(HybridCanvasElement)──> ModelStore ──observed by──> HybridCanvas (view)
                                                   │
                                                   ├──RTree (world bounds)────Hit testing & culling
                                                   └──ImageStore (view-side)──Async image decode + LRU + mipmap
```

The hybrid split: **20k+ objects render on a single `Canvas`** (full-clear + redraw visible set, R-tree culled); **only selection chrome** (handles, rubber-band, tooltips, context menu) are real `Node`s in the thin `Pane` overlay — `HybridCanvas extends Pane` so it drops into a `ScrollPane`/`BorderPane` unchanged.

## Layers

| Layer | Package | Responsibility | Never touches |
|---|---|---|---|
| Geometry | `geom` | `Bounds2D`, `Transform2D`, `Viewport` — pure double math | JavaFX entirely |
| Model | `model` | Sealed `HybridCanvasElement` hierarchy, `Group`, `Layer`, `HybridCanvasColor` (int ARGB) | `javafx.graphics`, world coords, decoded images |
| Index | `index` | Hand-rolled R-tree over world bounds (insert/remove/query) | Rendering, model mutation |
| Store | `model` persistence via `store` | `ModelStore` — holds layers/elements, `javafx.base` `ObservableList` | Rendering |
| View | `view` | `HybridCanvas`, `RenderTraversal`, `ShapeRenderer`, `ImageStore`, chrome/* | Owning/mutating the model (writes go through model setters) |
| UI | `ui` | `PropertiesPanel` | Rendering internals |
| Demo | `demo` | `DemoApp` harness + benchmark | Internal APIs |

## Model → View contract

- Model = POJOs in memory, mirroring how a plain `Pane` holds shapes. Consumer owns it; the component never does.
- Every setter bumps per-element `volatile long version` — the **only** dirty signal. View compares versions during the render pass: no listeners, no parent back-refs, no bubbling.
- Transform is composed fields (`translate/rotate/scale`), never a stored `Affine`. Children store points in **parent's local space**; world = ancestor-transform-chain ∘ child. Moving a group transforms descendants with zero child mutation.
- Render traversal propagates group changes: a bumped group version marks all descendants for redraw and recomputes their world bounds for the R-tree.

## Coordinate spaces

| Space | Owned by | Notes |
|---|---|---|
| Local | Model | `getLocalBounds()`, `containsLocal()` — the model's only geometry contract |
| World | View (render traversal) | Fixed origin `(0,0)`, large bounded canvas; object coords never change on pan/zoom |
| Screen | View (`Viewport`) | `zoom/panX/panY`; `zoomAt` keeps the cursor's world point fixed |

Hit-test flow: screen P → world W → R-tree query → candidates by z-order → view converts W→local via cached ancestor transform → `containsLocal(localPt)`.

## Rendering pipeline

- **Full-clear + redraw visible objects per frame**, bounded by R-tree culling. Redraw on zoom, pan, model mutation, gesture commit.
- Renderer dispatch = exhaustive pattern-match `switch` over the sealed hierarchy (compiler-checked, no visitor/cast/instanceof).
- Images: async load + placeholder → decode on bounded executor → `Platform.runLater` upload → fade in. Mipmap LOD picked per zoom; unique cache capped (256 MB default, LRU); atlas keys never evicted. Eviction is a store map-removal — no model mutation, no redraw storm.

## R-tree update model

| Event | Update |
|---|---|
| Gesture commit (drag/resize/rotate/vertex-edit) | Remove old world bbox, insert new (synchronous, FX thread) |
| During drag | Moved object dropped from index; hit-tests skip it; re-inserted on commit |
| Structural op (add/remove/reparent) | Walk subtree: remove every descendant's old bbox, insert every new one — O(subtree), rare |
| Startup bulk-load (20k+) | Chunked on FX thread (~500/frame via `AnimationTimer`) — progressive, no freeze |

## Threading

| Concern | Strategy |
|---|---|
| Image decode | Bounded `ExecutorService` (4–8 threads, capped queue); upload via `Platform.runLater` |
| R-tree updates | FX thread (commit: one remove+insert; structural: subtree walk) |
| Dirty-check | Per-element `volatile long version` compared during render pass |
| Canvas redraw | FX thread every frame during gestures; bounded by culling |

Everything else — model mutation, rendering, chrome, hit-testing — lives on the FX Application Thread.

## Selection chrome

- Default: **group mode** — one shared bbox, 8 resize handles + 1 rotate handle (rotation around combined-bbox center).
- `setSeparateSelection(true)`: per-object chrome, opt-in, capped (`setMaxSelected`, default ~50, auto-switch to group on cap).
- Vertex-edit handles: exactly 1 selected + double-click.
- Promoted-to-Node strategy: selected object is a real `Node` while selected, committed back on deselect.

## Performance targets (SPEC §14)

- 20k mixed objects + 1k images: visible content < ~500 ms, progressive index build without freeze
- Pan/zoom at ~60 FPS on a representative machine; hover hit-test < 1 ms
- Group-mode selection of 500 transforms smoothly
