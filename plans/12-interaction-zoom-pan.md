# Prompt 12 — interaction: zoom (anchored wheel) + pan (middle-drag + scrollbars)

**Depends on:** 11 (getObjectAt/hover), 10 (core), 01 (Viewport).

## Objective

Camera controls. Wheel zooms anchored to cursor; middle-drag pans; scrollbars
stay in sync (content grows with zoom, works inside `ScrollPane`).

## Context

- Builds on Prompt 11 (viewport + redraw). `Viewport.setZoomLimits` from
  Prompt 01 wired to `setZoomLimits(config)`.

## Tests first (red)

FxSmoke (Monocle):
- Wheel zoom over a point: the world point under the cursor stays fixed (assert
  via viewport math: record worldAtScreen before, scroll, assert unchanged
  within epsilon).
- Zoom limits: repeated wheel scroll clamps at min/max (and `-1` unbounded).
- Middle-drag: drag changes pan by the screen delta; scrollbar value/hmax
  reflect world size × zoom.

## Implement (green)

- Mouse wheel handler → `viewport.zoomAt(cursorX,cursorY,factor)`; factor from
  event delta. `setZoomLimits(min,max)` configures viewport.
- Middle-button press/drag → `viewport.pan` by screen delta.
- `ScrollPane` support: `HybridCanvas` reports `prefWidth/Height =
  worldSize*zoom` (using `getWorldSize()` from Prompt 10) and maps
  `hvalue/vvalue ↔ pan` (update on both scrollbar events and pan/zoom). Keep
  the internal Canvas sized to the viewport, not the world.
- Mark dirty + redraw on every zoom/pan change.

## Wire / integrate

Camera feeds traversal culling (Prompt 08) already wired in Prompt 10.

## Acceptance

`gradlew test` green (anchoring + clamping assertions).
