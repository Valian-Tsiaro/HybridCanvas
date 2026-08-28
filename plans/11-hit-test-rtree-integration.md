# Prompt 11 — HybridCanvas: R-tree integration + hit-test (getObjectAt, hover)

**Depends on:** 10 (core), 06 (RTree), 08 (traversal).

## Objective

Keep the R-tree consistent with world bounds and implement `getObjectAt` via
screen→world→R-tree query→`containsLocal`, plus `hoveredObject`.

## Context

- Builds on Prompt 10 + Prompt 06 (`RTree`).

## Tests first (red)

- R-tree integration (pure, via traversal): after structural add/remove, every
  element's R-tree entry equals its traversal-computed world bounds (assert no
  stale bounds). Move a shape's transform, recompute, assert bounds updated
  (structural re-index).
- `getObjectAt` — with two overlapping shapes, top z-order wins; unlocked
  shapes hit, locked are skipped; click outside all → null; a shape inside a
  translated group is hit correctly (world→local conversion uses ancestor
  transform); invisible shape not hit.
- `hoveredObjectProperty` — moving the "mouse" over a shape updates the
  property; leaving sets null.

## Implement (green)

- View-side index holder: `Map<UUID, Bounds2D>` rebuilt from traversal world
  bounds, pushed into `RTree`. Structural ops (`addObject`/`removeObject`/
  `clear`/reparent) walk the subtree and remove/reinsert descendants' world
  bounds (§4.11). Bulk-load: chunk ~500/frame via `AnimationTimer` on first
  render.
- `getObjectAt(Point2D screenPt)` — screen→world (viewport),
  `RTree.search(world bounds inflated by max(2.0 / viewport.zoom, 0) world
  units — a 2 screen-pixel-equivalent epsilon)`, candidates sorted by z-order,
  for each compute world→local via cached ancestor transform, call
  `element.containsLocal(localX,localY)`. Return first hit (top z-order).
- `hoveredObjectProperty()` — `ReadOnlyObjectProperty<HybridCanvasElement>`,
  updated on mouse-move via `getObjectAt`.

## Wire / integrate

Selection (Prompt 13) and gestures (Prompt 14) depend on `getObjectAt`.

## Acceptance

`gradlew test` green (hit-test precision + index consistency).
