# Prompt 05 — model: HybridCanvasImageShape + 3 image shapes

**Depends on:** 03 (shapes), 04 (group/layer).

## Objective

Image-bearing shapes. Model holds ONLY the `imageRef` (pure data key/String);
zero runtime image state here.

## Context

- `com.hybridcanvas.model`. Builds on Prompt 03.

## Tests first (red)

- `ImageShapeTest` — `imageRef` get/set (setter bumps version); `ImageRect`
  bounds == its rect and `containsLocal` == rect test; `ImageInShape`
  `containsLocal` delegates to the polygon mask; `ImageWithOverlay` bounds ==
  union of image rect and overlay shape.

## Implement (green)

- `HybridCanvasImageShape` (sealed abstract extends `HybridCanvasShape`):
  ```java
  public sealed abstract class HybridCanvasImageShape extends HybridCanvasShape
      permits HybridCanvasImageRect, HybridCanvasImageInShape,
              HybridCanvasImageWithOverlay { ... }
  ```
  Field: `String imageRef`.
- `HybridCanvasImageRect` (axis-aligned rect `x,y,w,h`).
- `HybridCanvasImageInShape` (`imageRef` + `double[]` polygon mask;
  `containsLocal` = even-odd on mask).
- `HybridCanvasImageWithOverlay` (`imageRef` + image rect + separate shape
  outline; bounds = union).
- No `Image`, no mipmap, no load state — just the key.

## Wire / integrate

Renderer reads `imageRef` and consults `ImageStore` (Prompt 17); renderer draw
cases land in Prompt 09's switch.

## Acceptance

`gradlew test` green.
