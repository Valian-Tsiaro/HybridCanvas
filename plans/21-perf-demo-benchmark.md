# Prompt 21 — perf harness: 20k benchmark + acceptance gates

**Depends on:** everything (00–20).

## Objective

Prove §14 acceptance: 20k mixed objects (shapes + 1k unique images) visible
~500ms, progressive index build (no UI freeze), ~60 FPS pan/zoom, hover
hit-test <1 ms, 500-object group transform smooth.

## Context

- Everything above. Extend `demo/DemoApp` (from Prompt 00) rather than creating
  a new app.

## Tests first (red)

`BenchmarkTest` (or a documented runnable check):
- Load 20,000 mixed objects; assert first visible content renders within
  ~500 ms and the index finishes progressively (no single >100ms freeze frame —
  measure via an injected frame counter).
- Assert hit-test resolves in <1 ms (time `getObjectAt` over many samples).
- Assert group-mode selection of 500 transforms without exception and stays
  under the separate-mode Node-bloat path (cap auto-switch fires).

## Implement (green)

- Extend `demo/DemoApp`: replace the placeholder random generator with a 20k
  generator (polygons/polylines/rects/ellipses/beziers + 1k unique images);
  keep the Hybrid/Pane toggle; add toggles for grid/minimap/selection modes; add
  a simple FPS readout (`AnimationTimer` frame count). Set `setWorldSize(20_000,
  15_000)` on the `HybridCanvas` before generating so the ScrollPane pref-size
  reflects the canvas.
- Benchmark checks live in `demo` or a dedicated test, not in the library public
  API.
- Document how to launch the demo (`gradlew run`).

## Wire / integrate

Final integration — every prior step is exercised here; this is where orphan
code (if any) surfaces, so fix wiring rather than adding.

## Acceptance

`gradlew test` green; manual/automated demo meets the §14 gates or the
discrepancy is logged with a profiler note (shape LOD §6.3 is the noted upgrade
if point-count is the bottleneck).
