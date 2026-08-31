package com.hybridcanvas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HybridCanvasShapeTest {

    private HybridCanvasShape newShape() {
        return new HybridCanvasRectangle(0, 0, 10, 10);
    }

    @Test
    void fillDefaultNull() {
        assertNull(newShape().getFill());
    }

    @Test
    void strokeDefaultBlack() {
        assertEquals(HybridCanvasColor.fromRgb(0, 0, 0), newShape().getStroke());
    }

    @Test
    void strokeWidthDefaultOne() {
        assertEquals(1.0, newShape().getStrokeWidth(), 0.0);
    }

    @Test
    void layerIdDefaultNull() {
        assertNull(newShape().getLayerId());
    }

    @Test
    void zOrderDefaultZero() {
        assertEquals(0, newShape().getZOrder());
    }

    @Test
    void setFillBumpsVersion() {
        HybridCanvasShape s = newShape();
        long before = s.getVersion();
        s.setFill(HybridCanvasColor.fromRgb(255, 0, 0));
        assertEquals(before + 1, s.getVersion());
    }

    @Test
    void setStrokeBumpsVersion() {
        HybridCanvasShape s = newShape();
        long before = s.getVersion();
        s.setStroke(HybridCanvasColor.fromRgb(255, 0, 0));
        assertEquals(before + 1, s.getVersion());
    }

    @Test
    void setStrokeWidthBumpsVersion() {
        HybridCanvasShape s = newShape();
        long before = s.getVersion();
        s.setStrokeWidth(3.0);
        assertEquals(before + 1, s.getVersion());
    }

    @Test
    void setLayerIdBumpsVersion() {
        HybridCanvasShape s = newShape();
        long before = s.getVersion();
        s.setLayerId("myLayer");
        assertEquals(before + 1, s.getVersion());
    }

    @Test
    void setZOrderBumpsVersion() {
        HybridCanvasShape s = newShape();
        long before = s.getVersion();
        s.setZOrder(5);
        assertEquals(before + 1, s.getVersion());
    }

    @Test
    void setStrokeNullThrows() {
        HybridCanvasShape s = newShape();
        assertThrows(IllegalArgumentException.class, () -> s.setStroke(null));
    }

    @Test
    void fillNullable() {
        HybridCanvasShape s = newShape();
        s.setFill(HybridCanvasColor.fromRgb(1, 2, 3));
        assertNotNull(s.getFill());
        s.setFill(null);
        assertNull(s.getFill());
    }
}
