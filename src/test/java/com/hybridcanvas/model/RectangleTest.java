package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RectangleTest {

    @Test
    void getLocalBoundsMatchesGeometry() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(10, 20, 30, 40);
        Bounds2D out = new Bounds2D();
        r.getLocalBounds(out);
        assertEquals(10, out.minX);
        assertEquals(20, out.minY);
        assertEquals(40, out.maxX);
        assertEquals(60, out.maxY);
    }

    @Test
    void containsLocalInside() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(0, 0, 100, 50);
        assertTrue(r.containsLocal(50, 25));
    }

    @Test
    void containsLocalCorner() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(0, 0, 100, 50);
        assertTrue(r.containsLocal(0, 0));
        assertTrue(r.containsLocal(100, 50));
    }

    @Test
    void containsLocalEdge() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(0, 0, 100, 50);
        assertTrue(r.containsLocal(50, 0));
        assertTrue(r.containsLocal(100, 25));
    }

    @Test
    void containsLocalOutside() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(0, 0, 100, 50);
        assertFalse(r.containsLocal(101, 25));
        assertFalse(r.containsLocal(-1, 25));
        assertFalse(r.containsLocal(50, 51));
        assertFalse(r.containsLocal(50, -1));
    }

    @Test
    void setXBumpsVersion() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(0, 0, 10, 10);
        long before = r.getVersion();
        r.setX(5);
        assertEquals(before + 1, r.getVersion());
    }

    @Test
    void containsLocalWithNegativeWidth() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(50, 50, -30, -20);
        Bounds2D out = new Bounds2D();
        r.getLocalBounds(out);
        assertEquals(20, out.minX);
        assertEquals(30, out.minY);
        assertEquals(50, out.maxX);
        assertEquals(50, out.maxY);
        assertTrue(r.containsLocal(35, 40));
        assertFalse(r.containsLocal(55, 40));
    }

    @Test
    void setWidthBumpsVersion() {
        HybridCanvasRectangle r = new HybridCanvasRectangle(0, 0, 10, 10);
        long before = r.getVersion();
        r.setWidth(20);
        assertEquals(before + 1, r.getVersion());
    }
}
