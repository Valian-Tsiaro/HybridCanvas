# AGENTS.md — Working Instructions

## What this is

`HybridCanvas` — a zoomable, pannable JavaFX `Canvas` + minimal `Pane` overlay that renders 20,000+ shapes and images at interactive framerates. Only selection chrome lives as real JavaFX `Node`s. Drop-in replacement for `Pane` (positional only).

**Source of truth:** [SPEC.md](SPEC.md). Plans never override the spec; if a plan contradicts SPEC, stop and ask.

## Build workflow

1. Execute prompts in [plans/](plans/) in **numeric order** (00 → 21). One prompt = one work session.
2. Per prompt: write the failing tests first (red) → implement until `gradlew test` is green → commit → tick the box in [plans/checklist.md](plans/checklist.md).
3. Never start a new prompt with a red suite. One prompt per commit, no batching.
4. Resolved decisions live in [plans/README.md](plans/README.md) — do not re-litigate them (Gradle/Groovy, Java 21, JavaFX 21, hand-rolled R-tree, radians, `translate ∘ rotate ∘ scale`, int-ARGB colors, 2-corner rectangles).
5. If a task seems to need something from the Do-NOT-build list below, it doesn't. Re-read the plan.

## Commands

| Command | Purpose |
|---|---|
| `gradlew test` | Full suite — must be green before any commit |
| `gradlew run` | Launch `com.hybridcanvas.demo.DemoApp` (the hand-driven harness) |

Headless FX test props are set by the `test` task in `build.gradle` (TestFX + Monocle); `gradlew test` just works.

## Golden rules (from SPEC — violations are bugs)

1. **Model = plain POJOs.** `model/` imports no `javafx.graphics` types. No JavaFX `Property` wrappers (`javafx.base` `ObservableList` is allowed in `store/` only). The model never touches world coordinates or decoded images.
2. **Every setter bumps `volatile long version`.** It is the *only* dirty signal. No listeners, no parent back-references, no version bubbling. The view dirty-checks versions during render and owns propagation (group transform changed → descendants redraw).
3. **Sealed hierarchy + exhaustive pattern-match `switch`** for renderer dispatch. No visitor, no `instanceof`, no casts. Adding a shape subclass = edit the `permits` list + add a `case`.
4. **Geometry is local-space only.** `getLocalBounds()` / `containsLocal(double,double)` on the model; world bounds, R-tree, and `ImageStore` are view-side.
5. **All scene-graph and `Canvas` ops run on the JavaFX Application Thread.** Image decode is the only off-thread work (bounded executor, 4–8 threads, capped queue; upload via `Platform.runLater`).
6. **Allocation discipline in hot paths.** Flat `double[]` interleaved points, mutable `Bounds2D`, transient `Point2D` views only. Zero per-call allocation in render/hit-test loops at 20k scale.
7. **`HybridCanvas.getChildren()` is reserved for the chrome overlay.** Scene objects go through `addObject`/`removeObject` as `HybridCanvasElement`s, never as `Node`s.
8. **Chrome handles are fixed screen-pixel size** — undo the zoom scale for both draw and hit-test.

## Do NOT build

Spec'd non-goals and deferred upgrades — skip on sight:

- Persistence, serialization, undo/redo, keyboard shortcuts, rulers, search/filter, touch/pinch-zoom
- Dirty-rect rendering, offscreen buffer + blit (noted upgrade paths only)
- Shape LOD / Douglas-Peucker (SPEC §6.3 flag: only if profiling shows point count is the bottleneck)
- Movable rotate pivot (fixed bbox-center pivot only)
- Per-object handler maps (per-gesture listeners + observable properties only)
- Any abstraction with one implementation, config for values that never change, scaffolding "for later"

## Testing

| Layer | Stack | Rule |
|---|---|---|
| `geom/ model/ index/ store/` | Pure JUnit 5, headless | No FX bootstrap — these must run without Monocle |
| `view/ ui/ demo/` | TestFX + Monocle headless | Extend `ApplicationTest` |

Every prompt's plan lists its tests; they are the acceptance criteria, not suggestions. Non-trivial logic (branch, loop, parser, math) never lands without a failing-first test.

## Style

Follow [STYLE_GUIDE.md](STYLE_GUIDE.md). Architecture overview: [ARCHITECTURE.md](ARCHITECTURE.md).
