# Prompt 20 — optional features: grid + snap, minimap, alignment guides

**Depends on:** 12 (camera), 14–16 (gestures).

## Objective

Activable extras (§12): visible grid + snap-to-grid, minimap with viewport
rectangle + click-to-jump, snap/alignment guides during drag.

## Context

- Builds on camera (12) + gestures (14–16).

## Tests first (red)

- `setGridVisible(true)` draws grid behind objects; `setSnapToGrid(true)` makes
  move/vertex snap to grid unit (assert final position is a multiple).
- `setMinimap(true)` shows inset with whole-canvas + viewport rect; clicking a
  point recenters the viewport there.
- Alignment guides: dragging near another object's edge/center shows a guide;
  snap threshold honored (assert aligned positions equal).

## Implement (green)

- Grid: background layer drawn first in the render pass (lines or dots); snap
  rounds move/vertex deltas to grid unit.
- Minimap: small canvas + click handler → viewport pan to world point.
- Guides: during drag, scan visible objects' edges/centers (R-tree query over
  drag region), draw red/green guide lines, snap within threshold.
- All gated behind `set...Visible/Enabled` flags (off by default).

## Wire / integrate

Optional; hooks the existing render + gesture pipelines.

## Acceptance

`gradlew test` green.
