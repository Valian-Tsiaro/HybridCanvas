# Hybrid Canvas

A zoomable, pannable JavaFX `Canvas` + minimal `Pane` overlay that renders **20,000+ shapes and images at interactive framerates**, where only selection chrome lives as real JavaFX `Node`s. A drop-in replacement for `Pane` (positional only).

## Why

A plain `Pane` with 20k `Node`s dies: scene-graph overhead, layout passes, memory bloat. `HybridCanvas` draws the scene on a single `Canvas` (R-tree culled, full-redraw of the visible set) and keeps real `Node`s only for the thin interaction chrome — selection handles, rubber-band, tooltips, context menu. The result behaves like a `Pane` but scales like a renderer.

- **Scale** — 20k+ objects now, 100k+ noted; R-tree hit-test and culling at O(log n)
- **Smooth** — ~60 FPS pan/zoom/drag/select on a representative machine
- **Drop-in** — `HybridCanvas extends Pane`; sits in a `ScrollPane`/`BorderPane` unchanged
- **Typed model** — sealed `HybridCanvasElement` hierarchy (Java 21 pattern-match dispatch), plain POJOs with `version`-based dirty checking
- **Async images** — placeholder → background decode → fade-in; LRU + mipmap LOD, atlas support

## Status

Spec-complete, phased test-driven build in progress. See [plans/checklist.md](plans/checklist.md) for phase-by-phase progress.

## Quick start (target API)

```java
HybridCanvas canvas = new HybridCanvas();
canvas.setZoomLimits(0.05, 20);
canvas.setMaxSelected(50);

canvas.addObject(new HybridCanvasRectangle(0, 0, 100, 60));
canvas.addObject(new HybridCanvasImageRect("sprites/atlas.png#ship", 200, 100, 64, 64));

canvas.setOnObjectClicked(e -> System.out.println(e.elements()));
canvas.selectedObjectsProperty().addListener((obs, was, now) -> /* ... */);
```

Scene objects are `HybridCanvasElement`s added via `addObject` — never `getChildren().add(node)`, which is reserved for the chrome overlay. Migration from `Pane` means swapping `getChildren().add` for `addObject` and JavaFX shape nodes for their `HybridCanvas*` counterparts.

## Build & run

Requires Java 21+.

| Command | Purpose |
|---|---|
| `gradlew test` | Full test suite (TestFX + Monocle headless — no display needed) |
| `gradlew run` | Launch the `DemoApp` harness (Hybrid vs plain-Pane toggle, benchmark generator) |

## Documentation

| Doc | What |
|---|---|
| [SPEC.md](SPEC.md) | Full specification — the source of truth |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Layering, model→view contract, rendering, threading |
| [STYLE_GUIDE.md](STYLE_GUIDE.md) | Project conventions |
| [AGENTS.md](AGENTS.md) | Working instructions for code-generation agents |
| [plans/](plans/) | Prompt-by-prompt build plan + [checklist](plans/checklist.md) |
