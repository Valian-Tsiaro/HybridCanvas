package com.hybridcanvas.view;

import com.hybridcanvas.geom.Bounds2D;
import com.hybridcanvas.geom.Viewport;
import com.hybridcanvas.model.HybridCanvasGroup;
import com.hybridcanvas.model.HybridCanvasLayer;
import com.hybridcanvas.model.HybridCanvasRectangle;
import com.hybridcanvas.store.ModelStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RenderTraversalTest {

    private final RenderTraversal traversal = new RenderTraversal();
    private final Viewport vp = new Viewport();

    /** Default world view covers a large area; no culling for basic tests. */
    private static Bounds2D fullWorldView() {
        return new Bounds2D(-10_000, -10_000, 10_000, 10_000);
    }

    private List<RenderItem> walk(ModelStore store, Bounds2D worldView) {
        List<RenderItem> items = new ArrayList<>();
        traversal.walk(store, vp, worldView, items::add);
        return items;
    }

    @Test
    void translateShiftsWorldBounds() {
        ModelStore store = new ModelStore();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        rect.setTranslateX(100);
        store.addObject(rect);

        List<RenderItem> items = walk(store, fullWorldView());
        assertEquals(1, items.size());
        assertEquals(100.0, items.get(0).worldBounds().minX, 1e-9);
        assertEquals(0.0, items.get(0).worldBounds().minY, 1e-9);
        assertEquals(110.0, items.get(0).worldBounds().maxX, 1e-9);
        assertEquals(10.0, items.get(0).worldBounds().maxY, 1e-9);
    }

    @Test
    void nestedGroupTransformChain() {
        // group translate(50,0) ∘ scale(2)
        //   child: HybridCanvasRectangle(10, 10, 5, 5)
        // local bounds: (10,10)→(15,15)
        // world = translate(50,0) ∘ scale(2): (10,10)→(20,20), (15,15)→(30,30)
        // then translate by (50,0): (20,20)→(80,30)
        ModelStore store = new ModelStore();
        HybridCanvasGroup g = new HybridCanvasGroup();
        g.setTranslateX(50);
        g.setScaleX(2);
        g.setScaleY(2);
        HybridCanvasRectangle rect = new HybridCanvasRectangle(10, 10, 5, 5);
        g.getChildren().add(rect);
        store.addObject(g);

        List<RenderItem> items = walk(store, fullWorldView());
        assertEquals(1, items.size());
        Bounds2D wb = items.get(0).worldBounds();
        // After scale(2): local (10,10)→(15,15) becomes (20,20)→(30,30)
        // After translate(50,0): (20,20)→(30,30) becomes (70,20)→(80,30)
        assertEquals(70.0, wb.minX, 1e-9);
        assertEquals(20.0, wb.minY, 1e-9);
        assertEquals(80.0, wb.maxX, 1e-9);
        assertEquals(30.0, wb.maxY, 1e-9);
    }

    @Test
    void sinkReceivesLeavesOnly() {
        ModelStore store = new ModelStore();
        HybridCanvasGroup g = new HybridCanvasGroup();
        g.getChildren().add(new HybridCanvasRectangle(0, 0, 5, 5));
        HybridCanvasGroup inner = new HybridCanvasGroup();
        inner.getChildren().add(new HybridCanvasRectangle(10, 10, 5, 5));
        g.getChildren().add(inner);
        store.addObject(g);

        List<RenderItem> items = walk(store, fullWorldView());
        // Two leaf rects emitted, no groups
        assertEquals(2, items.size());
        for (RenderItem item : items) {
            assertFalse(item.element() instanceof HybridCanvasGroup);
        }
    }

    @Test
    void groupOpacityComposedIntoLeaf() {
        ModelStore store = new ModelStore();
        HybridCanvasGroup g = new HybridCanvasGroup();
        g.setOpacity(0.5);
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        rect.setOpacity(0.4);
        g.getChildren().add(rect);
        store.addObject(g);

        List<RenderItem> items = walk(store, fullWorldView());
        assertEquals(1, items.size());
        assertEquals(0.2, items.get(0).effectiveOpacity(), 1e-9);
    }

    @Test
    void invisibleSkippedLockedStillEmitted() {
        ModelStore store = new ModelStore();
        // invisible leaf → not emitted
        HybridCanvasRectangle invisible = new HybridCanvasRectangle(0, 0, 10, 10);
        invisible.setVisible(false);
        store.addObject(invisible);
        // locked leaf → emitted
        HybridCanvasRectangle locked = new HybridCanvasRectangle(20, 20, 10, 10);
        locked.setLocked(true);
        store.addObject(locked);

        List<RenderItem> items = walk(store, fullWorldView());
        assertEquals(1, items.size());
        assertSame(locked, items.get(0).element());
    }

    @Test
    void cullingEmitsOnlyIntersecting() {
        ModelStore store = new ModelStore();
        HybridCanvasRectangle visible = new HybridCanvasRectangle(5, 5, 10, 10);
        HybridCanvasRectangle outside = new HybridCanvasRectangle(500, 500, 10, 10);
        HybridCanvasRectangle alsoOutside = new HybridCanvasRectangle(-200, -200, 10, 10);
        store.addObject(visible);
        store.addObject(outside);
        store.addObject(alsoOutside);

        // Viewport only sees world region (-100, -100)→(100, 100)
        Bounds2D worldView = new Bounds2D(-100, -100, 100, 100);
        List<RenderItem> items = walk(store, worldView);
        assertEquals(1, items.size());
        assertSame(visible, items.get(0).element());
    }

    @Test
    void ancestorTransformChangeReEmitsDescendants() {
        ModelStore store = new ModelStore();
        HybridCanvasGroup g = new HybridCanvasGroup();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        g.getChildren().add(rect);
        store.addObject(g);

        List<RenderItem> first = walk(store, fullWorldView());
        assertEquals(1, first.size());
        double firstMinX = first.get(0).worldBounds().minX;

        // Mutate group — leaf version stays the same
        long leafVersionBefore = rect.getVersion();
        g.setTranslateX(100);
        assertEquals(leafVersionBefore, rect.getVersion(), "leaf version unchanged by group mutation");

        List<RenderItem> second = walk(store, fullWorldView());
        assertEquals(1, second.size());
        double secondMinX = second.get(0).worldBounds().minX;

        // World bounds must reflect the new group transform
        assertEquals(firstMinX + 100, secondMinX, 1e-9);
    }

    @Test
    void layerOpacityComposesAndInvisibleLayerSkipped() {
        ModelStore store = new ModelStore();
        HybridCanvasLayer hiddenLayer = new HybridCanvasLayer("h", "Hidden");
        hiddenLayer.setVisible(false);
        store.addLayer(hiddenLayer);

        HybridCanvasRectangle hiddenRect = new HybridCanvasRectangle(0, 0, 5, 5);
        hiddenRect.setOpacity(0.8);
        store.addObject(hiddenLayer, hiddenRect);

        HybridCanvasLayer semiLayer = new HybridCanvasLayer("s", "Semi");
        semiLayer.setOpacity(0.5);
        store.addLayer(semiLayer);

        HybridCanvasRectangle semiRect = new HybridCanvasRectangle(0, 0, 10, 10);
        semiRect.setOpacity(0.4);
        store.addObject(semiLayer, semiRect);

        List<RenderItem> items = walk(store, fullWorldView());
        // Hidden layer: rect not emitted
        // Semi layer: rect emitted with opacity 0.5 × 0.4 = 0.2
        assertEquals(1, items.size());
        assertSame(semiRect, items.get(0).element());
        assertEquals(0.2, items.get(0).effectiveOpacity(), 1e-9);
    }
}
