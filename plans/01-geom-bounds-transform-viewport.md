# Prompt 01 — geom: Bounds2D, Transform2D, Viewport (pure double math)

**Depends on:** 00 (build + test harness).

## Objective

The pure-double geometry layer everything else sits on. No `javafx.graphics`;
headless-testable; allocation-conscious.

## Context

- `com.hybridcanvas.geom` (new package). No model yet.

## Tests first (red)

`src/test/java/com/hybridcanvas/geom/`:
- `Bounds2DTest` — set/get; union of two boxes; expand by delta; contains point
  (inside/on-edge/outside); intersects (overlap/touch/disjoint); width/height;
  copy independence.
- `Transform2DTest` — identity; `translate(tx,ty)` maps `(x,y)->(x+tx,y+ty)`;
  `scale(2,2)` doubles a point; `rotate(PI/2)` maps `(1,0)->(~0,1)`; concat
  order (translate ∘ rotate ∘ scale) matches hand-computed point; inverse
  round-trips a point; `transformBounds` on an axis-aligned box yields the
  expected AABB (test a rotated rect's AABB corners).
- `ViewportTest` — `worldToScreen`/`screenToWorld` inverse each other;
  `zoomAt` keeps the world point under `(sx,sy)` fixed before/after; zoom
  limits clamp (and `-1` means unbounded); default is `1.0` zoom, `0` pan.

## Implement (green)

- `Bounds2D` — mutable public `double minX,minY,maxX,maxY`; methods `set`,
  `union`, `expand`, `contains(x,y)`, `intersects`, `width`, `height`, `copy`.
  No per-call alloc.
- `Transform2D` — immutable 6-coeff affine (`m00,m01,m02,m10,m11,m12`); static
  factories `identity()`, `translate(tx,ty)`, `rotate(radians)`, `scale(sx,sy)`;
  `concat(other)` = `this∘other`; `mapX`/`mapY` or `apply(x,y,double[2])`;
  `inverse()`; `transformBounds(Bounds2D in, Bounds2D out)` (transform 4
  corners → AABB).
- `Viewport` — `double zoom, panX, panY`; `screenToWorld`/`worldToScreen`;
  `zoomAt(sx,sy,factor)`; `setZoomLimits(min,max)` with `-1` = unbounded;
  `clampZoom()`; apply zoom limits on mutation.

## Wire / integrate

Pure library — consumed by model (Prompt 02+), traversal (Prompt 08), and
`HybridCanvas` (Prompt 10+). No UI yet.

## Acceptance

`gradlew test` green (geom tests only).
