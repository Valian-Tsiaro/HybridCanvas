# Prompt 14 — chrome: move gesture (drag body) with commit

**Depends on:** 13 (selection), 11 (index), 12 (camera).

## Objective

Dragging a selected object's body mutates `translateX/translateY` (version
bump), redraws each frame, drops the object from the R-tree during drag, and
re-inserts its new world bounds on commit (§8).

## Context

- Builds on Prompts 11 (index) + 13 (selection).

## Tests first (red)

- Drag body by `(dx,dy)`: `element.translateX/Y` changed by world→local delta
  (inverse-transform the screen delta through the element's own transform).
- `version` bumped during drag; model is the single source of truth (assert the
  view holds no shadow position).
- During drag the dragged object is not returned by `getObjectAt`; after commit
  it IS returned again with updated bounds (R-tree re-inserted).
- Group move: dragging a selected group moves the group (children unchanged in
  local space); descendants re-render because ancestor transform changed.

## Implement (green)

- SelectionOverlay/gesture controller: on press over an object's body → begin
  move; drag → `setTranslateX/Y` (screen delta → local via `worldTransform`
  inverse); `markDirty` each frame; release → commit: `RTree` re-insert world
  bounds (via traversal recompute), final redraw.
- Respect locked (no move).

## Wire / integrate

Move is the reference gesture; resize/rotate (Prompt 15) reuse the same
commit/re-index path.

## Acceptance

`gradlew test` green.
