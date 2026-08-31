package com.hybridcanvas.index;

import com.hybridcanvas.geom.Bounds2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hand-rolled R-tree that indexes world-space {@link Bounds2D} keyed by
 * {@link UUID} for logarithmic hit-tests and culling. Linear split with no
 * rebalance; query and update do not allocate beyond a handful of temporaries.
 *
 * <ul>
 *   <li>{@code insert} silently replaces an existing id with new bounds</li>
 *   <li>Removed leaves stay in the tree until a full rebuild — no ghost bounds, just empty leaves</li>
 *   <li>Thread-affinity: caller owns synchronization; the FX Application Thread is the intended owner</li>
 * </ul>
 */
public final class RTree {

    // ponytail: linear split, no rebalance — rebuild/STR if deletes skew the tree.
    private static final int MAX = 16;

    private final Map<UUID, Entry> entries = new HashMap<>();
    private Node root;
    private int size;

    private static final class Entry {
        final UUID id;
        final Bounds2D bounds;
        Node leaf;

        Entry(UUID id, Bounds2D bounds) {
            this.id = id;
            this.bounds = bounds.copy();
        }
    }

    private static final class Node {
        final Bounds2D bounds = new Bounds2D();
        boolean isLeaf = true;
        List<Entry> entries = new ArrayList<>();
        List<Node> children = new ArrayList<>();
    }

    /**
     * Indexes {@code bounds} under {@code id}, normalising negative-size boxes
     * so {@code minX <= maxX} and {@code minY <= maxY} before storage. Replaces
     * any prior entry for the same id.
     *
     * @param id stable key; re-inserting the same id updates its bounds
     * @param bounds world-space box; copied internally so the caller may mutate its instance
     */
    public void insert(UUID id, Bounds2D bounds) {
        double minX = Math.min(bounds.minX, bounds.maxX);
        double minY = Math.min(bounds.minY, bounds.maxY);
        double maxX = Math.max(bounds.minX, bounds.maxX);
        double maxY = Math.max(bounds.minY, bounds.maxY);
        Bounds2D b = new Bounds2D(minX, minY, maxX, maxY);

        if (entries.containsKey(id)) {
            remove(id);
        }

        Entry entry = new Entry(id, b);
        entries.put(id, entry);

        if (root == null) {
            root = new Node();
            root.entries.add(entry);
            entry.leaf = root;
            root.bounds.set(b);
            size = 1;
            return;
        }

        Node leaf = chooseLeaf(root, b);
        entry.leaf = leaf;
        leaf.entries.add(entry);
        leaf.bounds.union(b);
        size++;

        if (leaf.entries.size() > MAX) {
            split(leaf);
        }
    }

    /**
     * Removes the entry for {@code id}; no-op if absent. Empty leaves remain in
     * the tree — queries skip them, but a long delete-heavy run may need a rebuild.
     *
     * @param id the key previously passed to {@link #insert}
     */
    public void remove(UUID id) {
        Entry entry = entries.remove(id);
        if (entry == null) return;

        Node leaf = entry.leaf;
        leaf.entries.remove(entry);
        size--;
        recomputeUp(leaf);
    }

    /**
     * Appends the ids whose stored bounds intersect {@code query} to {@code out}.
     * Does not clear {@code out} first; the caller owns the list.
     *
     * @param query world-space query box; not retained
     * @param out caller-owned list that receives matching ids in unspecified order
     */
    public void search(Bounds2D query, List<UUID> out) {
        if (root != null) {
            searchNode(root, query, out);
        }
    }

    public int size() {
        return size;
    }

    /** Drops every entry and resets the tree to empty. */
    public void clear() {
        entries.clear();
        root = null;
        size = 0;
    }

    // -- internals --

    private void searchNode(Node node, Bounds2D q, List<UUID> out) {
        if (!node.bounds.intersects(q)) return;
        if (node.isLeaf) {
            for (Entry e : node.entries) {
                if (e.bounds.intersects(q)) {
                    out.add(e.id);
                }
            }
        } else {
            for (Node child : node.children) {
                searchNode(child, q, out);
            }
        }
    }

    private Node chooseLeaf(Node node, Bounds2D b) {
        if (node.isLeaf) return node;
        Node best = null;
        double bestArea = Double.MAX_VALUE;
        for (Node child : node.children) {
            double enlarged = areaEnlargement(child.bounds, b);
            if (enlarged < bestArea) {
                bestArea = enlarged;
                best = child;
            }
        }
        return chooseLeaf(best, b);
    }

    private static double areaEnlargement(Bounds2D existing, Bounds2D addition) {
        double w = Math.max(existing.maxX, addition.maxX) - Math.min(existing.minX, addition.minX);
        double h = Math.max(existing.maxY, addition.maxY) - Math.min(existing.minY, addition.minY);
        double currentArea = existing.width() * existing.height();
        return w * h - currentArea;
    }

    private void split(Node node) {
        List<Entry> items = node.entries;
        int n = items.size();

        // pick two seeds by max x-separation
        int seed1 = 0, seed2 = 1;
        double maxX = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            if (items.get(i).bounds.maxX > maxX) {
                maxX = items.get(i).bounds.maxX;
                seed2 = i;
            }
        }
        maxX = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            if (i == seed2) continue;
            if (items.get(i).bounds.maxX > maxX) {
                maxX = items.get(i).bounds.maxX;
                seed1 = i;
            }
        }

        Bounds2D b1 = items.get(seed1).bounds;
        Bounds2D b2 = items.get(seed2).bounds;

        List<Entry> group1 = new ArrayList<>();
        List<Entry> group2 = new ArrayList<>();
        group1.add(items.get(seed1));
        group2.add(items.get(seed2));

        Bounds2D box1 = b1.copy();
        Bounds2D box2 = b2.copy();

        for (int i = 0; i < n; i++) {
            if (i == seed1 || i == seed2) continue;
            Entry e = items.get(i);
            double d1 = areaEnlargement(box1, e.bounds);
            double d2 = areaEnlargement(box2, e.bounds);
            if (d1 < d2) {
                group1.add(e);
                box1.union(e.bounds);
            } else {
                group2.add(e);
                box2.union(e.bounds);
            }
        }

        // fix: if one group is empty, move one entry from the other
        if (group1.isEmpty() && !group2.isEmpty()) {
            group1.add(group2.remove(group2.size() - 1));
        } else if (group2.isEmpty() && !group1.isEmpty()) {
            group2.add(group1.remove(group1.size() - 1));
        }

        node.isLeaf = true;
        node.entries = group1;
        node.bounds.set(box1);

        Node sibling = new Node();
        sibling.isLeaf = true;
        sibling.entries = group2;
        sibling.bounds.set(box2);

        for (Entry e : group1) e.leaf = node;
        for (Entry e : group2) e.leaf = sibling;

        if (node == root) {
            Node newRoot = new Node();
            newRoot.isLeaf = false;
            newRoot.children.add(node);
            newRoot.children.add(sibling);
            newRoot.bounds.set(node.bounds);
            newRoot.bounds.union(sibling.bounds);
            root = newRoot;
        } else {
            Node parent = findParent(root, node);
            parent.children.add(sibling);
            recomputeUp(parent);
        }
    }

    private Node findParent(Node current, Node target) {
        if (current.isLeaf) return null;
        for (Node child : current.children) {
            if (child == target) return current;
            Node found = findParent(child, target);
            if (found != null) return found;
        }
        return null;
    }

    private void recomputeUp(Node node) {
        Bounds2D b = new Bounds2D();
        b.setEmpty();
        if (node.isLeaf) {
            for (Entry e : node.entries) b.union(e.bounds);
        } else {
            for (Node child : node.children) b.union(child.bounds);
        }
        if (b.isEmpty()) b.set(0, 0, 0, 0);
        node.bounds.set(b);
        if (node != root) {
            Node parent = findParent(root, node);
            if (parent != null) recomputeUp(parent);
        }
    }
}
