# Prompt 04 — model: HybridCanvasGroup + HybridCanvasLayer

**Depends on:** 03 (shapes), 02 (element base).

## Objective

Containers. `Group` is transformable/nestable but not a `Shape`; `Layer` holds
zOrder + elements and mirrors the "one default layer" rule.

## Context

- `com.hybridcanvas.model`. Builds on Prompts 02–03.

## Tests first (red)

- `HybridCanvasGroupTest` — children list mutable; `getLocalBounds == union` of
  children's own local bounds (each transformed by that child's transform, then
  unioned); empty group → empty/degenerate bounds; nesting group-in-group
  unions correctly; group is NOT `instanceof HybridCanvasShape`.
- `HybridCanvasLayerTest` — defaults (`visible=true`, `locked=false`,
  `opacity=1.0`, `zOrder=0`, empty elements); elements add/remove.

## Implement (green)

- `HybridCanvasGroup` (`final`, extends `HybridCanvasElement`):
  `List<HybridCanvasElement> children` (mutable). `getLocalBounds(out)` unions
  children's `getLocalBounds`, each run through that child's local transform
  (`translate/rotate/scale` → `Transform2D` from geom).
- `HybridCanvasLayer` (`final`): `String id`, `String name`, `boolean visible`,
  `double opacity`, `boolean locked`, `int zOrder`,
  `List<HybridCanvasElement> elements`.

## Wire / integrate

Used by `ModelStore` (Prompt 07) and traversal (Prompt 08).

## Acceptance

`gradlew test` green.
