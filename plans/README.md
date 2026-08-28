# Hybrid Canvas — Implementation Plan

Prompt-by-prompt, test-driven build plan for the `HybridCanvas` component
(SPEC.md). Each numbered markdown file is one prompt for a code-generation
LLM. Execute in order; every prompt ends green on `gradlew test` and consumes
the previous prompt's output. Commit after each green step.

## Resolved decisions (from SPEC §15 + build choices)

| # | Decision |
|---|---|
| Build | Gradle (Groovy DSL `build.gradle`), Java 21 toolchain, JavaFX 21, TestFX + `openjfx-monocle` headless |
| Package | `com.hybridcanvas` (groupId `com.hybridcanvas`, artifact `hybrid-canvas`) |
| R-tree | Hand-rolled ~150 LOC, incremental `insert/remove/query` |
| Testing | Model/geom/index/store = pure JUnit 5 headless; FX layer = TestFX + Monocle headless |
| World size | `setWorldSize(double w, double h)` (added to §3.3 config surface; needed for ScrollPane pref-size in Prompt 12) |
| Color | Model uses `HybridCanvasColor` (int ARGB); renderer maps to `javafx.scene.paint.Color` |
| Points | `containsLocal(double,double)` + `getX/getY/setPoint/getPointCount` + transient `HybridCanvasPoint` view |
| rotate unit | radians |
| Rectangle | `x,y,w,h` (2-corner) |
| Transform order | `world = translate ∘ rotate ∘ scale` |

## Package layout

```
com.hybridcanvas
  geom/    Bounds2D, Transform2D, Viewport        (pure double, headless-testable)
  model/   HybridCanvasColor, sealed Element/Shape hierarchy, Group, Layer  (no javafx.graphics)
  index/   RTree                                   (hand-rolled, view-side)
  store/   ModelStore                              (javafx.base ObservableList only)
  view/    RenderTraversal, RenderItem, ShapeRenderer, ImageStore, HybridCanvas, chrome/*
  ui/      PropertiesPanel (later)
  demo/    DemoApp (harness from Prompt 00, benchmark at Prompt 21)
```

## Phase → prompt map

| Phase | Prompts |
|---|---|
| 0 Scaffold + demo harness | 00 |
| 1 Geometry | 01 |
| 2 Model | 02, 03, 04, 05 |
| 3 Spatial index | 06 |
| 4 Store + traversal | 07, 08 |
| 5 Rendering | 09, 10 |
| 6 Index + hit-test | 11 |
| 7 Interactions | 12, 13 |
| 8 Chrome + transforms | 14, 15, 16 |
| 9 Images | 17 |
| 10 Properties + tooltip | 19 |
| 11 Events + context menu | 18 |
| 12 Optional features | 20 |
| 13 Perf harness | 21 |

## Progress tracking

See `checklist.md`.
