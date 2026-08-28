# Prompt 03 — model: HybridCanvasShape + concrete shapes (geometry + containsLocal)

**Depends on:** 02 (element base + color), 01 (geom).

## Objective

The sealed `Shape` branch and its five non-image concrete types, with
`getLocalBounds(Bounds2D out)` and `containsLocal(double,double)`. Local-space
only.

## Context

- `com.hybridcanvas.model`. Builds on Prompt 02.

## Tests first (red)

- `RectangleTest` — `getLocalBounds == x,y,w,h`; `containsLocal`
  inside/corner/edge/outside.
- `EllipseTest` — `containsLocal` on-center/on-boundary `(rx,0)`/`(0,ry)`/
  outside; bounds = `cx±rx, cy±ry`.
- `PolygonTest` — points stored flat `[x0,y0,...]`; `getPointCount`;
  `getX/getY(i)`; `setPoint` bumps version; `containsLocal` via even-odd
  (triangle: inside centroid, outside far point); closed (bounds from all
  vertices).
- `PolylineTest` — open (no implicit closing edge); `containsLocal` is
  within-stroke-tolerance of any segment (on-segment true, far false).
- `BezierTest` — cubic (4 ctrl pts) and quad (3) recognized; `getLocalBounds`
  covers the control hull (conservative AABB is fine — assert it contains the
  curve's endpoints and document conservativeness); contains via
  flattening/sampling tolerance.
- `HybridCanvasShapeTest` — `fill/stroke/strokeWidth/layerId/zOrder` defaults
  and setters bump version; `fill` nullable.

## Implement (green)

- `HybridCanvasShape` (sealed abstract extends `HybridCanvasElement`):
  ```java
  public sealed abstract class HybridCanvasShape extends HybridCanvasElement
      permits HybridCanvasPolygon, HybridCanvasPolyline, HybridCanvasRectangle,
              HybridCanvasEllipse, HybridCanvasBezier, HybridCanvasImageShape { ... }
  ```
  Fields: `HybridCanvasColor fill` (nullable), `HybridCanvasColor stroke`,
  `double strokeWidth` (world units), `String layerId`, `int zOrder`.
- Point accessors here (or a shared `PolyShape`): `int getPointCount()`;
  `double getX(int i)`; `double getY(int i)`; `void setPoint(int i, double x,
  double y)`; `HybridCanvasPoint getPoint(int i)` returning a transient reused
  view.
- Concrete finals: `Polygon` (`double[]` pts, closed), `Polyline` (open),
  `Rectangle` (`x,y,w,h`), `Ellipse` (`cx,cy,rx,ry`), `Bezier` (`double[]`
  ctrl, 4=cubic/3=quad).
- Abstract `Bounds2D getLocalBounds(Bounds2D out)` (reuse `out`; no alloc) and
  abstract `boolean containsLocal(double x, double y)` — put the contracts on
  `HybridCanvasShape`.
- `HybridCanvasPoint` — tiny mutable `(double x, double y)` view, reused per
  element.

## Wire / integrate

Consumed by `RenderTraversal` (Prompt 08) for bounds and by hit-test
(Prompt 11) for `containsLocal`. Renderer (Prompt 09) reads geometry.

## Acceptance

`gradlew test` green.
