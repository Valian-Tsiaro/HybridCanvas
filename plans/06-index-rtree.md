# Prompt 06 — index: hand-rolled RTree (world bounds)

**Depends on:** 01 (Bounds2D/geom), 02–05 (model).

## Objective

A minimal R-tree storing world bounds keyed by element `UUID`, with
incremental insert/remove and box query. ~150 LOC.

## Context

- `com.hybridcanvas.index` (new). Uses `Bounds2D` from geom. View-side (holds
  world bounds, not local).

## Tests first (red)

`src/test/java/com/hybridcanvas/index/RTreeTest.java`:
- insert then `query(bbox)` returns the id; query miss returns empty.
- `remove(id)` then query returns empty (no ghost bounds).
- re-insert same id with new bounds updates query results.
- mixed tiny + huge bboxes: querying a huge box returns all contained;
  querying a small box returns only overlapping ones.
- random stress: 20,000 random rects inserted; 100 random point/box queries
  each match a brute-force linear scan's expected set exactly; then remove all
  20,000 (no error, size 0).
- degenerate: zero-area bounds, negative-size (normalized), duplicate id.

## Implement (green)

- `RTree`:
  ```java
  public final class RTree {
    public void insert(UUID id, Bounds2D bounds);
    public void remove(UUID id);
    public void search(Bounds2D query, List<UUID> out);
    public int size();
    public void clear();
  }
  ```
  Internal `Map<UUID, Entry>` for exact removal; quadratic or linear split
  node; recursive box-overlap traversal.
- `// ponytail: linear/quadratic split, no rebalance — rebuild/STR if deletes
  skew the tree.`

## Wire / integrate

Consumed by `HybridCanvas` (Prompt 11) for hit-test and structural/bulk index
updates.

## Acceptance

`gradlew test` green (stress test is the gate).
