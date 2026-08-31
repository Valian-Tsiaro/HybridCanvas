package com.hybridcanvas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HybridCanvasLayerTest {

    private HybridCanvasLayer newLayer() {
        return new HybridCanvasLayer("layer-1", "Background");
    }

    @Test
    void defaults() {
        HybridCanvasLayer l = newLayer();
        assertTrue(l.isVisible());
        assertFalse(l.isLocked());
        assertEquals(1.0, l.getOpacity(), 0.0);
        assertEquals(0, l.getZOrder());
        assertTrue(l.getElements().isEmpty());
    }

    @Test
    void ctorSetsIdAndName() {
        HybridCanvasLayer l = newLayer();
        assertEquals("layer-1", l.getId());
        assertEquals("Background", l.getName());
    }

    @Test
    void elementsAddRemove() {
        HybridCanvasLayer l = newLayer();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        l.getElements().add(rect);
        assertEquals(1, l.getElements().size());
        assertTrue(l.getElements().contains(rect));
        l.getElements().remove(rect);
        assertTrue(l.getElements().isEmpty());
    }

    @Test
    void setNameUpdatesValue() {
        HybridCanvasLayer l = newLayer();
        l.setName("Foreground");
        assertEquals("Foreground", l.getName());
    }

    @Test
    void setVisibleUpdatesValue() {
        HybridCanvasLayer l = newLayer();
        l.setVisible(false);
        assertFalse(l.isVisible());
    }

    @Test
    void setLockedUpdatesValue() {
        HybridCanvasLayer l = newLayer();
        l.setLocked(true);
        assertTrue(l.isLocked());
    }

    @Test
    void setZOrderUpdatesValue() {
        HybridCanvasLayer l = newLayer();
        l.setZOrder(7);
        assertEquals(7, l.getZOrder());
    }

    @Test
    void setOpacityClampsToValidRange() {
        HybridCanvasLayer l = newLayer();
        l.setOpacity(1.5);
        assertEquals(1.0, l.getOpacity(), 0.0);
        l.setOpacity(-0.5);
        assertEquals(0.0, l.getOpacity(), 0.0);
    }

    @Test
    void elementsListIsMutable() {
        HybridCanvasLayer l = newLayer();
        l.getElements().add(new HybridCanvasRectangle(0, 0, 5, 5));
        l.getElements().add(new HybridCanvasEllipse(10, 10, 3, 3));
        assertEquals(2, l.getElements().size());
    }
}
