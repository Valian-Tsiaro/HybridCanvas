package com.hybridcanvas.view;

import com.hybridcanvas.geom.Bounds2D;
import com.hybridcanvas.geom.Transform2D;
import com.hybridcanvas.model.HybridCanvasElement;

/**
 * Emitted by {@link RenderTraversal} for each visible leaf. Pre-culls against the
 * world view bounds so the caller never draws outside the viewport. Holds a
 * snapshot of the world transform and world bounds — mutable references are
 * copied at emission time so the traversal's scratch buffers can be reused.
 */
public record RenderItem(
        HybridCanvasElement element,
        Transform2D worldTransform,
        Bounds2D worldBounds,
        double effectiveOpacity
) {}
