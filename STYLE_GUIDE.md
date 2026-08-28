# Style Guide

Project-specific conventions. Everything not covered here: standard Java 21 conventions.

## Language & build

- **Java 21** (toolchain), JavaFX 21 (modules: `controls`, `graphics`, `base`), Gradle Groovy DSL.
- Package root `com.hybridcanvas`; groupId `com.hybridcanvas`, artifact `hybrid-canvas`.
- Public API classes carry the `HybridCanvas` prefix (`HybridCanvasElement`, `HybridCanvasPolygon`, …) — the component is a drop-in library; the prefix is the namespace.

## Model (`model/`)

- Plain POJOs. Zero `javafx.*` imports (enforced by rule, not build — keep it that way). Colors are `HybridCanvasColor` (int ARGB), never `javafx.scene.paint`.
- **Every setter bumps `volatile long version`.** No exceptions, no listener callbacks, no parent back-references. Getters are plain reads.
- Points: flat `double[]` interleaved `[x0,y0,x1,y1,…]`. Accessors `getX/getY/setPoint/getPointCount`; `getPoint(i)` returns a transient view object only.
- `rotate` is **radians**. Rectangle is **2-corner** (`x,y,w,h`). Transform order: `world = translate ∘ rotate ∘ scale`.
- Geometry contract is local-space only: `getLocalBounds()` (fills a mutable `Bounds2D`, no alloc) and `containsLocal(double,double)`. Never add world-coord methods to the model.

## Sealed hierarchy

- `HybridCanvasElement` / `HybridCanvasShape` are `sealed` with explicit `permits`.
- Renderer dispatch is exhaustive pattern-match `switch` over the concrete cases. **No `instanceof`, no casts, no visitor.** Adding a subclass = edit `permits` + add a `case` (the compiler forces both).

## View (`view/`)

- All scene-graph and `Canvas` ops on the FX Application Thread. Off-thread work is image decode only, uploaded via `Platform.runLater`.
- Hot paths (render loop, hit-test loop) allocate **zero** per call: reuse `Bounds2D`/`double[]` scratch, transform 4 corners into a caller-owned bounds, no streams, no boxing, no `new` in per-object loops.
- Chrome handles are fixed screen-pixel size — divide by zoom for draw and hit-test.
- `HybridCanvas.getChildren()` is never used for scene objects — only chrome. Scene objects enter via `addObject`/`removeObject`.

## Comments

- No comment narrating what the code says. Comment *why*, when non-obvious.
- Deliberate shortcuts/ceilings get a `// ponytail:` comment naming the ceiling and upgrade path.

## Tests

- `geom/ model/ index/ store/`: pure JUnit 5, no FX bootstrap, no Monocle — must run headless-plain.
- `view/ ui/ demo/`: TestFX + Monocle (`ApplicationTest`); headless props come from the `test` task in `build.gradle`.
- Failing-first for non-trivial logic (branch, loop, parser, math). Each plan's test list is the acceptance criteria.
- Test class = production class + `Test`; one behavior per test method, named for the behavior (`zoomAtKeepsWorldPointUnderCursor`).

## Formatting

- 4-space indent, no tabs. One class per file. Imports explicit, no wildcards.
- Fields before constructors before methods; `static` factories (`Transform2D.identity()`) over telescoping constructors.
