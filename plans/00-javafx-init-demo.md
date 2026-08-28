# Prompt 00 — JavaFX codebase init + Demo harness (Hybrid vs Pane toggle)

**Depends on:** nothing (empty repo, only SPEC.md).

## Objective

Initialize the JavaFX/Gradle codebase and a `DemoApp` you drive by hand:
toolbar on top, the active canvas in the center, and a switch that flips the
center between `HybridCanvas` and a plain JavaFX `Pane` so you can compare the
two approaches. This is the harness every later prompt feeds.

## Context

- Java 21+, JavaFX 21, package `com.hybridcanvas`.
- Gradle (Groovy DSL), JUnit 5 + TestFX/Monocle headless.

## Tests first (red)

- `src/test/java/com/hybridcanvas/PureSmokeTest.java` — JUnit 5 `@Test`,
  `1+1==2` (proves headless JUnit runs).
- `src/test/java/com/hybridcanvas/FxSmokeTest.java` — `ApplicationTest` +
  Monocle headless; launches a `Stage` with an empty `Scene` and asserts it
  shows. Must run with: `-Djava.awt.headless=true -Dtestfx.robot=glass
  -Dtestfx.headless=true -Dglass.platform=Monocle
  -Dmonocle.platform=Headless -Dprism.order=sw`.
- `src/test/java/com/hybridcanvas/demo/DemoAppTest.java` — launches `DemoApp`;
  asserts the toolbar has **Add Shape**, **Add Image**, a count field +
  **Generate N**, and the mode switch; asserts the switch swaps the center
  between a plain `Pane` and the `HybridCanvas` stub.

## Implement (green)

- `settings.gradle` — `rootProject.name = "hybrid-canvas"`.
- `build.gradle` (Groovy):
  - plugins: `java` + `org.openjfx.javafxplugin`.
  - `javafx` version 21, modules `javafx.controls`, `javafx.graphics`,
    `javafx.base`.
  - toolchain Java 21.
  - dependencies: `junit-jupiter` 5.10+, `testfx-core` 4.0.18,
    `testfx-junit5`, `openjfx-monocle` (jdk-21).
  - `test` task sets the headless system props above.
  - `application`/`run` mainClass = `com.hybridcanvas.demo.DemoApp`.
- `src/main/java/com/hybridcanvas/demo/DemoApp.java` — `Application`/`Stage`:
  - Top toolbar (`HBox`): "Add Shape", "Add Image", count `TextField` +
    "Generate N", and a `ToggleButton` (or `RadioButton`s) "Hybrid | Pane".
  - Center (`BorderPane`): swapped by the switch — a plain
    `javafx.scene.layout.Pane` (Pane mode) or the `HybridCanvas` stub
    (Hybrid mode).
  - Random generation from a small local set of descriptors: Pane mode maps
    each descriptor to a real `Node` (`Rectangle`/`Ellipse`/`Polygon`/
    `ImageView`); Hybrid mode is a no-op for now (nothing renders until
    Prompt 10 wires `addObject`), so the toggle is live but Hybrid is empty.
- `src/main/java/com/hybridcanvas/view/HybridCanvas.java` — STUB:
  `public class HybridCanvas extends Pane {}` (optionally a centered
  "HybridCanvas (coming soon)" `Label`). Grows into the real component in
  Prompt 10; no model ops yet.

## Wire / integrate

The persistent harness: Prompt 10 flips Hybrid mode live, Prompt 17 makes
images live, Prompt 21 replaces the placeholder generator with the 20k
benchmark.

## Acceptance

`gradlew test` green; `gradlew run` opens the demo, toolbar works, toggle swaps
center between Pane (renders shapes) and the empty Hybrid stub.
