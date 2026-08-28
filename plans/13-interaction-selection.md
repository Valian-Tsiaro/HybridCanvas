# Prompt 13 — interaction: selection (click, ctrl/shift, rubber-band)

**Depends on:** 11 (getObjectAt), 12 (camera).

## Objective

Single/multi selection and rubber-band, driving `selectedObjectsProperty()`.
Locked objects are non-selectable.

## Context

- Builds on Prompt 11 (getObjectAt, hover).

## Tests first (red)

- Click empty space clears selection.
- Click a shape selects it (`selectedObjectsProperty` size 1).
- Ctrl/Shift-click toggles/adds; click without modifiers replaces.
- Clicking a locked object leaves selection unchanged.
- Rubber-band drag on empty space: rectangle selects shapes whose bounds
  intersect the band; band has a visible rectangle Node in chrome.

## Implement (green)

- `selectedObjectsProperty()` → `ObservableList<HybridCanvasElement>`.
- Mouse press on object → select (respect modifiers); press on empty → start
  rubber-band (record anchor, show a `Rectangle` in chrome); drag → update
  band; release → compute intersecting objects via R-tree box query and select
  them.
- `setMaxSelected(cap)` stored; enforce in a later prompt (16) for group
  switching, but do not overflow the list silently — assert cap behavior there.

## Wire / integrate

Selection feeds chrome (Prompt 14–16) and properties panel (Prompt 19).

## Acceptance

`gradlew test` green.
