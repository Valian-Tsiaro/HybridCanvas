# Prompt 08 — view: RenderTraversal (world bounds + culling, headless)

**Depends on:** 07 (store), 01 (geom), 02–05 (model).

## Objective

The model→view walk that composes the ancestor transform stack, computes each
element's WORLD bounds, culls against the viewport, and emits `RenderItem`s —
without touching `GraphicsContext` (pure, headless-testable).

## Context

- `com.hybridcanvas.view` (new). Uses model + geom + store.

## Tests first (red)

`src/test/java/com/hybridcanvas/view/RenderTraversalTest.java`:
- A shape at `(0,0)` with `translateX=100` yields a `RenderItem` whose
  `worldBounds` is shifted by `+100`.
- A child inside a translated+scaled group gets `worldBounds` reflecting the
  full ancestor chain (compute expected by hand).
- **Leaves only**: sink receives no `Group` items; groups are walked for
  transform-stack propagation only.
- Group opacity composed into emitted leaf's effective opacity
  (`group.opacity * leaf.opacity`), stored on the `RenderItem`.
- `visible=false` elements are skipped from the render list (`locked` elements
  still render — locked only gates selection).
- Culling: with viewport `zoom=1` showing only a corner, only intersecting
  elements are emitted (assert emitted set vs. brute force).
- Group transform change does NOT require child version bumps — children with
  unchanged version still re-emitted because the ancestor transform changed
  (tests the "propagation owned by traversal" rule, §4.8).

## Implement (green)

- `RenderItem` — `element` + world `Transform2D` + world `Bounds2D` + effective
  `opacity` (reused buffer).
- `RenderTraversal`:
  ```java
  public final class RenderTraversal {
    public void walk(ModelStore store, Viewport vp, RenderSink sink);
  }
  ```
  Maintains a transform stack (push element local transform, concat to world)
  AND an opacity stack (multiply ancestor opacities). Walks groups recursively
  for transform/opacity propagation but only emits `RenderItem`s for leaves
  (non-`Group` shapes). Culls each leaf against the viewport's world-space
  AABB before emitting. Only leaf shapes are emitted and indexed; groups are
  containers and receive no R-tree entry (§4.11 structural ops walk leaf
  descendants only).
- `RenderSink` — minimal functional interface; a test sink collects items.

## Wire / integrate

`HybridCanvas` (Prompt 10) uses this to render and to rebuild R-tree world
bounds; hit-test (Prompt 11) reuses world bounds.

## Acceptance

`gradlew test` green.
