# Prompt 19 — properties panel + tooltip + hover toggles

**Depends on:** 13 (selection), 02–05 (model), 17 (ImageStore).

## Objective

Write-through `PropertiesPanel` mirroring the model (§11) and tooltip/hover
features (§3.3).

## Context

- Builds on selection (13), model (02–05), ImageStore (17).

## Tests first (red)

- Panel reads selected object's identity (`id`/`type` RO), geometry (points via
  `getPoint`, transform fields), appearance (`fill`/`stroke`/`strokeWidth`/
  `opacity`), state (`visible`/`locked`/`layerId`/`zOrder`); image fields for
  `ImageShape`.
- Editing a field writes through to the model setter (version bumps) and
  re-reads the panel when the selected object's version changes.
- Multi-select: shared fields editable, differing fields show "mixed"; edit
  applies to all selected.
- No selection: panel shows canvas-level settings (zoom, grid, layer list).
- Tooltip: default provider = `id + " " + type`; `setTooltipTextProvider`
  overrides; `setShowTooltips` toggles.
- `setHighlightOnHover` / `setCursorOnHover` toggles.

## Implement (green)

- `ui/PropertiesPanel` (Pane/SidePanel) bound to `selectedObjectsProperty` and
  re-reading on version change (a listener/pulse comparing versions).
- Tooltip popup in chrome overlay on hover using the provider.
- Hover highlight + cursor via the `hoveredObjectProperty` path (Prompt 11).

## Wire / integrate

Finalizes the §3.3 config surface; demo (Prompt 21) exercises it.

## Acceptance

`gradlew test` green.
