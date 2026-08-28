# Prompt 17 — images: ImageStore (async decode, LRU cap, mipmap, fade-in)

**Depends on:** 09 (renderer image cases), 10 (core), 05 (image shapes).

## Objective

View-side store owning ALL runtime image state. Async decode on a bounded
executor, upload via `Platform.runLater`, 256 MB LRU cap, mipmap LOD,
placeholder until loaded. Image shapes now render real images.

## Context

- `com.hybridcanvas.view`. Builds on Prompt 09 (renderer's image cases
  currently draw placeholders). §4.9, §6.2.

## Tests first (red)

- State machine (headless): `PLACEHOLDER→LOADING→LOADED` / `FAILED` transitions
  for an `imageRef`; `get(imageRef)` returns placeholder and triggers async
  load.
- LRU: with a tiny cap, inserting >cap unique images evicts the least-recently
  used, and atlas (`registerAtlasImage`) entries are never evicted.
- Eviction does NOT bump any model version (assert no element version change).
- Mipmap: generate mips down to ≤16px; `pickLevel(zoom)` returns coarser level
  at low zoom, full at high zoom.
- FxSmoke: an `ImageRect` renders the decoded image pixels (or placeholder if
  decode pending) without blocking.

## Implement (green)

- `ImageStore`:
  ```java
  public final class ImageStore {
    public enum State { PLACEHOLDER, LOADING, LOADED, FAILED }
    public void registerAtlasImage(String key, javafx.scene.image.Image img);
    public State getState(String imageRef);
    public javafx.scene.image.Image getImage(String imageRef);
    public void setMaxMemoryMB(int cap);
    public javafx.scene.image.Image get(String imageRef); // placeholder + trigger async load
  }
  ```
  Bounded `ExecutorService` (4–8 threads, capped queue); `Map<imageRef, Entry>`;
  decode off-thread; `Platform.runLater` to publish `Image`; LRU eviction by
  memory budget; mipmap generation (downscale to 16×16) + level selection by
  current zoom.
- Widen `ShapeRenderer.draw` signature: `draw(GraphicsContext gc, RenderItem
  item, ImageStore images)`. Replace Prompt 09's `drawImagePlaceholder` calls
  with real image lookup: `ImageRect` → draw image if loaded else grey rect;
  `ImageInShape` → clip to polygon then draw image (or outline on miss);
  `ImageWithOverlay` → image + overlay outline. Fade-in when LOADED (opacity
  ramp on upload).

## Wire / integrate

`HybridCanvas` owns the store; renderer signature widens here. DemoApp "Add
Image" button now loads a real image.

## Acceptance

`gradlew test` green (state machine + LRU + eviction no-version-bump).
