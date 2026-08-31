---
name: javadoc
description: Writes concise English Javadoc for the HybridCanvas codebase. Class and method docs are at most 2 sentences unless the method is complex; classes may add one enumerated list. @param/@return/@throws are always written for public API. Use when adding or reviewing Javadoc comments in this project.
---

# Javadoc Conventions

## Rules

- **English only.** No other language in any Javadoc.
- **2 sentences max** for a class or method summary — except *complex* methods (below), which may go longer.
- **Class Javadoc:** the 2-sentence summary is the norm. Only when the class really needs it, append one enumerated list (`<ol>`/`<ul>`) *in addition to* the summary — e.g. responsibilities, invariants, or the thread-safety contract.
- **Block tags:** `@param`, `@return`, `@throws` are written for every public method in `view/`, `geom/`, `model/`, `index/`, `store/`, `ui/`. Omit them for private/package-private methods and for `demo/` code.
- **Document only the non-obvious.** A member gets a Javadoc only when its name and signature don't tell the whole story: a non-obvious contract, invariant, or side effect (a model setter bumping `version`), nullability, thread-safety, or a mutable/transient return. Trivial accessors and factories (`randomRect`), and overrides that merely implement an inherited contract, stay bare — the tool inherits. An override that adds non-obvious behavior documents only that delta. A member that exists only as a test/visibility hook is non-obvious and keeps a one-liner.
- **Public fields on value types** get a one-line doc when the name alone doesn't convey the role (e.g. matrix coefficients `m00`–`m12`, bounding-box corners `minX`/`maxY`). Trivial fields on simple POJOs (`x`, `y` on a point) stay bare.
- **Summary sentence:** the first sentence stands alone and ends with a period. Methods use a present-tense verb phrase ("Returns…", "Scales…"); classes, constructors, and accessors use a noun phrase ("Zoomable, pannable canvas…").
- **Inline tags:** `{@code}` for identifiers, `{@link}` for cross-refs, `{@literal}` to escape `< > &`. (See STYLE_GUIDE.md §Comments for inline `//` comments — out of scope here.)

## When a method may exceed 2 sentences

A method counts as *complex* and may get a longer Javadoc when it touches:

- concurrency / thread-safety (locks, `volatile`, FX Application Thread contract, off-thread decode)
- math / geometry / algorithms (transforms, radians, hit-test, coordinate spaces)
- non-obvious invariants (pre/post conditions, ordering, side effects)
- public API contract (the `HybridCanvas*` drop-in library surface)

## Examples

```java
// Good — 2 sentences, why not what.
/**
 * Tracks the world-space bounds of every added element so hit-tests and
 * culling stay logarithmic instead of linear in element count. Not safe
 * for concurrent use; all mutation happens on the FX Application Thread.
 *
 * <ul>
 *   <li>Insert/remove rebinds exactly one element's entry</li>
 *   <li>Queries never allocate; results fill a caller-owned bounds</li>
 * </ul>
 */
public final class HybridCanvasIndex { ... }

// Good — complex (thread-safety), longer, with tags.
/**
 * Decodes the image off the Application Thread and schedules the decoded
 * texture for upload on the FX thread. Decode is the only off-thread work
 * in the view; the executor is bounded to 8 threads with a capped queue.
 *
 * @param path the filesystem path of the image to decode
 * @return a handle that resolves to the decoded image on the FX thread
 * @throws IllegalArgumentException if {@code path} is null or unreadable
 */
public CompletableFuture<Image> decodeAsync(String path) { ... }
```

```java
// Bad — restates the code.
/** Sets the x coordinate to the given value. */
public void setX(double x) { this.x = x; }
```

```java
// Good — setter whose only interesting fact is the side effect.
/** Bumps the version so the view's dirty-check re-renders next pass. */
public void setWidth(double w) { this.w = w; version++; }
```
