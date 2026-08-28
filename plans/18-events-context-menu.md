# Prompt 18 — events API + context menu

**Depends on:** 12–16 (interactions/chrome), 19 (properties panel), 10 (core).

> **Execution order note:** runs AFTER 19 (properties panel) despite the lower
> filename number — the "Properties" menu item needs the panel to exist.

## Objective

`CanvasElementEvent` gesture handlers (§3.2) and the context menu with built-in
items + consumer extensibility (§10).

## Context

- Builds on interactions/chrome (12–16).

## Tests first (red)

- `setOnObjectClicked` fires with element + screenPt + modifiers on click.
- `setOnObjectDoubleClicked` fires (and, if installed, overrides vertex-edit).
- `setOnContextRequested` fires on right-click; `setOnObjectDragStarted/Dragged/
  Dropped` fire around a drag gesture.
- Context menu: right-click on an object shows built-ins (Delete, Duplicate,
  Bring to Front, Send to Back, Toggle Lock, Properties); Delete removes the
  object (`removeObject` + re-index); Duplicate clones (new id, copied fields);
  Bring to Front / Send to Back reorder `zOrder`; Toggle Lock flips `locked`.
- `setContextMenuItems(List<MenuItem>)` replaces/extends the built-ins.

## Implement (green)

- `events/CanvasElementEvent`:
  ```java
  public final class CanvasElementEvent {
    public List<HybridCanvasElement> elements();
    public javafx.geometry.Point2D screenPt();
    // modifiers
  }
  ```
- Handler fields + setters on `HybridCanvas`; wired into existing gesture code
  (no per-object maps — single handler per gesture, zero per-object cost).
- Context menu: build default `MenuItem`s; expose `setContextMenuItems` so the
  consumer mutates/replaces. Actions mutate the model (version bump) and trigger
  re-index + redraw. The default "Properties" item opens the `PropertiesPanel`
  built in Prompt 19 (must exist before this prompt runs).

## Wire / integrate

Double-click routing reconciles with vertex-edit (Prompt 16).

## Acceptance

`gradlew test` green.
