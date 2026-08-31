package com.hybridcanvas.store;

import com.hybridcanvas.model.HybridCanvasElement;
import com.hybridcanvas.model.HybridCanvasLayer;
import com.hybridcanvas.model.HybridCanvasRectangle;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ModelStoreTest {

    private HybridCanvasRectangle rect(double x) {
        return new HybridCanvasRectangle(x, 0, 10, 10);
    }

    @Test
    void defaultLayerAutoCreated() {
        ModelStore store = new ModelStore();
        assertEquals(1, store.getLayers().size());
        assertEquals("default", store.getDefaultLayer().getId());
        assertSame(store.getDefaultLayer(), store.getLayers().get(0));
    }

    @Test
    void defaultLayerIsNonDeletable() {
        ModelStore store = new ModelStore();
        assertThrows(IllegalArgumentException.class,
                () -> store.removeLayer(store.getDefaultLayer()));
        assertEquals(1, store.getLayers().size());
    }

    @Test
    void addObjectGoesToDefaultLayer() {
        ModelStore store = new ModelStore();
        HybridCanvasRectangle e = rect(1);
        store.addObject(e);
        assertTrue(store.getDefaultLayer().getElements().contains(e));
        assertTrue(store.getElements().contains(e));
        assertEquals(1, store.getElements().size());
    }

    @Test
    void addObjectWithLayerHonorsLayer() {
        ModelStore store = new ModelStore();
        HybridCanvasLayer extra = new HybridCanvasLayer("l2", "Foreground");
        extra.setZOrder(1);
        store.addLayer(extra);
        HybridCanvasRectangle e = rect(1);
        store.addObject(extra, e);
        assertTrue(extra.getElements().contains(e));
        assertFalse(store.getDefaultLayer().getElements().contains(e));
        assertTrue(store.getElements().contains(e));
        assertEquals(1, store.getElements().size());
    }

    @Test
    void removeObjectRemovesFromLayerAndView() {
        ModelStore store = new ModelStore();
        HybridCanvasRectangle e = rect(1);
        store.addObject(e);
        store.removeObject(e);
        assertFalse(store.getDefaultLayer().getElements().contains(e));
        assertFalse(store.getElements().contains(e));
    }

    @Test
    void addObjectDuplicateThrows() {
        ModelStore store = new ModelStore();
        HybridCanvasRectangle e = rect(1);
        store.addObject(e);
        assertThrows(IllegalStateException.class, () -> store.addObject(e));
    }

    @Test
    void addObjectSameElementToDifferentLayerThrows() {
        ModelStore store = new ModelStore();
        HybridCanvasLayer extra = new HybridCanvasLayer("l2", "Foreground");
        store.addLayer(extra);
        HybridCanvasRectangle e = rect(1);
        store.addObject(extra, e);
        assertThrows(IllegalStateException.class, () -> store.addObject(e));
    }

    @Test
    void addLayerNullThrows() {
        ModelStore store = new ModelStore();
        assertThrows(IllegalArgumentException.class, () -> store.addLayer(null));
    }

    @Test
    void addObjectNullLayerThrows() {
        ModelStore store = new ModelStore();
        assertThrows(IllegalArgumentException.class,
                () -> store.addObject(null, rect(1)));
    }

    @Test
    void removeLayerNullThrows() {
        ModelStore store = new ModelStore();
        assertThrows(IllegalArgumentException.class, () -> store.removeLayer(null));
    }

    @Test
    void removeObjectUnknownIsNoOp() {
        ModelStore store = new ModelStore();
        HybridCanvasRectangle e = rect(1);
        store.removeObject(e);
        assertTrue(store.getElements().isEmpty());
    }

    @Test
    void clearEmptiesElementsKeepsAllLayers() {
        ModelStore store = new ModelStore();
        HybridCanvasLayer extra = new HybridCanvasLayer("l2", "Foreground");
        store.addLayer(extra);
        store.addObject(rect(1));
        store.addObject(extra, rect(2));
        store.clear();
        assertTrue(store.getDefaultLayer().getElements().isEmpty());
        assertTrue(extra.getElements().isEmpty());
        assertTrue(store.getElements().isEmpty());
        assertEquals(2, store.getLayers().size());
        assertEquals("default", store.getLayers().get(0).getId());
        assertEquals("l2", store.getLayers().get(1).getId());
    }

    @Test
    void getElementsIsObservableOnAdd() {
        ModelStore store = new ModelStore();
        AtomicInteger adds = new AtomicInteger();
        store.getElements().addListener((ListChangeListener<HybridCanvasElement>) c -> {
            while (c.next()) adds.addAndGet(c.getAddedSize());
        });
        store.addObject(rect(1));
        assertEquals(1, adds.get());
    }

    @Test
    void getElementsIsObservableOnRemove() {
        ModelStore store = new ModelStore();
        HybridCanvasRectangle e = rect(1);
        store.addObject(e);
        AtomicInteger removes = new AtomicInteger();
        store.getElements().addListener((ListChangeListener<HybridCanvasElement>) c -> {
            while (c.next()) removes.addAndGet(c.getRemovedSize());
        });
        store.removeObject(e);
        assertEquals(1, removes.get());
    }

    @Test
    void getElementsReflectsAcrossLayersInZOrder() {
        ModelStore store = new ModelStore();
        HybridCanvasLayer extra = new HybridCanvasLayer("l2", "Foreground");
        extra.setZOrder(1);
        store.addLayer(extra);
        HybridCanvasRectangle e1 = rect(1);
        HybridCanvasRectangle e2 = rect(2);
        HybridCanvasRectangle e3 = rect(3);
        store.addObject(e1);
        store.addObject(e2);
        store.addObject(extra, e3);
        assertEquals(3, store.getElements().size());
        assertSame(e1, store.getElements().get(0));
        assertSame(e2, store.getElements().get(1));
        assertSame(e3, store.getElements().get(2));
    }

    @Test
    void addLayerAppearsInGetLayers() {
        ModelStore store = new ModelStore();
        HybridCanvasLayer extra = new HybridCanvasLayer("l2", "Foreground");
        store.addLayer(extra);
        assertTrue(store.getLayers().contains(extra));
        assertEquals(2, store.getLayers().size());
    }

    @Test
    void removeLayerDropsElementsFromView() {
        ModelStore store = new ModelStore();
        HybridCanvasLayer extra = new HybridCanvasLayer("l2", "Foreground");
        store.addLayer(extra);
        HybridCanvasRectangle e1 = rect(1);
        HybridCanvasRectangle e2 = rect(2);
        store.addObject(e1);
        store.addObject(extra, e2);
        store.removeLayer(extra);
        assertFalse(store.getElements().contains(e2));
        assertTrue(store.getElements().contains(e1));
        assertEquals(1, store.getElements().size());
    }
}
