# Prompt 16 — chrome: separate mode + selection cap + auto-switch + vertex-edit

**Depends on:** 15 (handles), 13 (selection).

## Objective

`setSeparateSelection(true)` gives each object its own chrome;
`setMaxSelected(N)` cap auto-switches to group mode on overflow (unless
disabled); double-click enters vertex-edit for polygon/polyline.

## Context

- Builds on Prompt 15. §7.1–7.2, §7.3.

## Tests first (red)

- Separate mode: N selected → N bounding boxes (per-object chrome).
- Cap: `setMaxSelected(3)`; selecting a 4th auto-switches to group mode; with
  `setAutoSwitchToGroupOnCap(false)` it does NOT switch (behavior defined:
  reject or overflow — pick and test).
- Default is group mode (`separate=false`) and `auto-switch=true`.
- Vertex edit: double-click a polygon/polyline → per-vertex handles; dragging
  a vertex calls `setPoint(i,x,y)` (version bump) and reshapes geometry;
  single selected only (≥2 → no vertex mode). **v1 deliberately limits
  vertex-edit to polygon/polyline** — bezier/polygon points remain editable
  via the Properties panel (Prompt 19).

## Implement (green)

- `setSeparateSelection(boolean)`, `setMaxSelected(int)`,
  `setAutoSwitchToGroupOnCap(boolean)` — config flags wired into the
  selection/chrome controller.
- Vertex-edit mode: show per-vertex handles (undo zoom, fixed pixel); drag
  updates model points; on exit recompute bounds + re-index.
- Double-click routing honors `setOnObjectDoubleClicked` (Prompt 18) as an
  override — if the consumer installs a handler it can replace vertex-edit.

## Wire / integrate

Finalizes chrome; properties panel (Prompt 19) reuses the same selected set.

## Acceptance

`gradlew test` green.
