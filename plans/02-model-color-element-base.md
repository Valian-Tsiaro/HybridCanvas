# Prompt 02 — model: HybridCanvasColor + HybridCanvasElement base + version bumping

**Depends on:** 01 (geom), 00 (harness).

## Objective

Root of the sealed model hierarchy. Fields, setters that bump a `volatile long
version`, and the ARGB color value type.

## Context

- `com.hybridcanvas.model` (new). Uses `Bounds2D` from geom only where needed
  later. No `javafx.graphics`.

## Tests first (red)

`src/test/java/com/hybridcanvas/model/`:
- `HybridCanvasColorTest` — pack/unpack ARGB int round-trips;
  `fromHex("#RRGGBB")` and `fromHex("#AARRGGBB")`; constants (TRANSPARENT,
  opaque helpers); equals.
- `HybridCanvasElementTest` — defaults (`visible=true`, `locked=false`,
  `opacity=1.0`, `scaleX=scaleY=1.0`, `rotate=0`, `translate=0`, `version=0`,
  `id` non-null unique); every setter (`rotate`, `scaleX`, `scaleY`,
  `translateX`, `translateY`, `visible`, `locked`, `opacity`, `parentId`)
  increments version by exactly 1; metadata map is mutable and independent;
  setting a field to the SAME value still bumps version (or document a no-op
  policy and test it consistently).

## Implement (green)

- `HybridCanvasColor` (final, immutable, wraps `int argb`): `fromArgb`/`toArgb`,
  `fromHex`, static `TRANSPARENT`, `equals`/`hashCode`/`toString`.
- `HybridCanvasElement`:
  ```java
  public sealed abstract class HybridCanvasElement
      permits HybridCanvasShape, HybridCanvasGroup { ... }
  ```
  Fields: `UUID id`; `double rotate, scaleX, scaleY, translateX, translateY`;
  `boolean visible, locked`; `double opacity`; `Map<String,Object> metadata`;
  `UUID parentId`; `volatile long version`. Every setter increments version.
  Getters only, no `javafx.graphics`.

## Wire / integrate

Subclasses come in Prompts 03–05; `version` is the sole dirty signal used by
the view (Prompt 08+).

## Acceptance

`gradlew test` green.
