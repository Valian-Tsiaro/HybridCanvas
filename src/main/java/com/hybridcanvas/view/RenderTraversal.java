package com.hybridcanvas.view;

import com.hybridcanvas.geom.Bounds2D;
import com.hybridcanvas.geom.Transform2D;
import com.hybridcanvas.geom.Viewport;
import com.hybridcanvas.model.HybridCanvasElement;
import com.hybridcanvas.model.HybridCanvasGroup;
import com.hybridcanvas.model.HybridCanvasLayer;
import com.hybridcanvas.model.HybridCanvasShape;
import com.hybridcanvas.store.ModelStore;

import java.util.List;

/**
 * Walks the model tree, composes ancestor transforms, computes world bounds,
 * culls against the viewport, and emits {@link RenderItem}s for visible leaves.
 * No {@code javafx.graphics} imports — headless-testable.
 *
 * <p>The traversal owns propagation: a bumped group version causes all
 * descendants to be re-emitted with current transforms without any version
 * reads on the model side.</p>
 */
public final class RenderTraversal {

    // ponytail: fixed-depth stack; no need to grow — 64 levels of nesting
    // is well beyond any realistic scene graph.
    private static final int MAX_STACK = 64;

    private final Transform2D[] transformStack = new Transform2D[MAX_STACK];
    private final double[] opacityStack = new double[MAX_STACK];
    private int topIndex;

    private final Bounds2D localBoundsScratch = new Bounds2D();
    private final Bounds2D worldBoundsScratch = new Bounds2D();

    /**
     * Walks the entire model store, emitting {@link RenderItem}s for visible
     * leaves whose world bounds intersect {@code worldView}.
     *
     * @param store     the model store to walk
     * @param vp        viewport (reserved for future use; not dereferenced in this prompt)
     * @param worldView the world-space AABB representing the visible region; items outside are culled
     * @param sink      receives emitted items
     */
    public void walk(ModelStore store, Viewport vp, Bounds2D worldView, RenderSink sink) {
        topIndex = 0;
        transformStack[0] = Transform2D.identity();
        opacityStack[0] = 1.0;

        List<HybridCanvasLayer> layers = store.getLayers();
        for (int i = 0, n = layers.size(); i < n; i++) {
            HybridCanvasLayer layer = layers.get(i);
            if (!layer.isVisible()) continue;

            push(transformStack[topIndex], opacityStack[topIndex] * layer.getOpacity());

            List<HybridCanvasElement> elements = layer.getElements();
            for (int j = 0, m = elements.size(); j < m; j++) {
                walkElement(elements.get(j), worldView, sink);
            }

            pop();
        }
    }

    private void walkElement(HybridCanvasElement element, Bounds2D worldView, RenderSink sink) {
        if (!element.isVisible()) return;

        Transform2D worldTransform = transformStack[topIndex]
                .concat(element.getLocalTransform());
        double worldOpacity = opacityStack[topIndex] * element.getOpacity();

        switch (element) {
            case HybridCanvasGroup g -> {
                push(worldTransform, worldOpacity);
                List<HybridCanvasElement> children = g.getChildren();
                for (int i = 0, nc = children.size(); i < nc; i++) {
                    walkElement(children.get(i), worldView, sink);
                }
                pop();
            }
            case HybridCanvasShape s -> {
                s.getLocalBounds(localBoundsScratch);
                worldTransform.transformBounds(localBoundsScratch, worldBoundsScratch);
                if (worldBoundsScratch.intersects(worldView)) {
                    sink.accept(new RenderItem(
                            s,
                            worldTransform,
                            worldBoundsScratch.copy(),
                            worldOpacity
                    ));
                }
            }
        }
    }

    private void push(Transform2D transform, double opacity) {
        topIndex++;
        transformStack[topIndex] = transform;
        opacityStack[topIndex] = opacity;
    }

    private void pop() {
        topIndex--;
    }
}
