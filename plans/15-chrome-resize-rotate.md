# Prompt 15 — chrome: resize (8 handles) + rotate (1 handle), group mode default

**Depends on:** 14 (move/commit), 13 (selection).

## Objective

Selected objects get a shared bounding box with 8 resize + 1 rotate handle
(screen-pixel-fixed size). Group mode default: ≥2 selected share one box and
transform together. Resize scales the bbox; rotate pivots on bbox center.

## Context

- Builds on Prompt 14. §7.1–7.2.

## Tests first (red)

- Single selection shows 8 resize handles + 1 rotate handle at expected screen
  positions (bbox corners/edges + above-center).
- Corner handle drag scales the element (world units) preserving opposite
  corner; edge handle scales one axis.
- Rotate handle drag rotates around bbox center (fixed pivot): rotating 90°
  moves a corner to the expected position.
- Group mode: two selected → ONE bbox + one set of handles; dragging the bbox
  transforms both together (relative layout preserved).
- Handle size is zoom-independent (screen pixels) — at zoom 0.1 and 10 the
  handles are the same pixel size and still hit-testable.

## Implement (green)

- `chrome/HandlesOverlay`: 8 Rectangle/Path handles (4 corners + 4 edges) + 1
  rotate handle, fixed pixel size, undo zoom scale for draw + hit-test.
- Resize: anchor = opposite handle; `scaleX/scaleY` from drag delta in local
  space (reuse `Transform2D` inverse). Rotate: angle from bbox center to
  cursor, applied as `element.rotate` around center.
- Group-mode bbox = union of selected world bounds; write transform deltas into
  each selected element so relative layout holds.
- Commit path identical to move (re-index + redraw).

## Wire / integrate

Consumes selection (13) + commit (14).

## Acceptance

`gradlew test` green.
