package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformable, nestable container of elements. Not a {@link HybridCanvasShape} —
 * it has no fill/stroke/points of its own. Own transform applies to all descendants.
 * Local bounds are the union of children's bounds (each transformed by that child's
 * local transform).
 */
public final class HybridCanvasGroup extends HybridCanvasElement {

    private final List<HybridCanvasElement> children = new ArrayList<>();
    private final Bounds2D scratch = new Bounds2D();

    /** @return the mutable children list (structural mutations bypass version — caller's responsibility) */
    public List<HybridCanvasElement> getChildren() {
        return children;
    }

    /**
     * Fills {@code out} with the union of all children's local bounds (each
     * transformed by that child's local transform). An empty group produces
     * an {@link Bounds2D#isEmpty() empty} result.
     */
    @Override
    public Bounds2D getLocalBounds(Bounds2D out) {
        // ponytail: getLocalTransform() allocates ~5 Transform2D per child;
        // negligible at group-level counts but upgradeable to in-place compose
        // if profiling shows group bounds are hot.
        out.setEmpty();
        for (HybridCanvasElement child : children) {
            child.getLocalBounds(scratch);
            child.getLocalTransform().transformBounds(scratch, scratch);
            out.union(scratch);
        }
        return out;
    }
}
