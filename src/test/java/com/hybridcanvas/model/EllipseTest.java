package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EllipseTest {

    @Test
    void getLocalBoundsCoversEllipse() {
        HybridCanvasEllipse e = new HybridCanvasEllipse(10, 20, 5, 8);
        Bounds2D out = new Bounds2D();
        e.getLocalBounds(out);
        assertEquals(5, out.minX);
        assertEquals(12, out.minY);
        assertEquals(15, out.maxX);
        assertEquals(28, out.maxY);
    }

    @Test
    void containsLocalOnCenter() {
        HybridCanvasEllipse e = new HybridCanvasEllipse(10, 20, 5, 8);
        assertTrue(e.containsLocal(10, 20));
    }

    @Test
    void containsLocalOnBoundaryRx() {
        HybridCanvasEllipse e = new HybridCanvasEllipse(10, 20, 5, 8);
        assertTrue(e.containsLocal(15, 20));
        assertTrue(e.containsLocal(5, 20));
    }

    @Test
    void containsLocalOnBoundaryRy() {
        HybridCanvasEllipse e = new HybridCanvasEllipse(10, 20, 5, 8);
        assertTrue(e.containsLocal(10, 28));
        assertTrue(e.containsLocal(10, 12));
    }

    @Test
    void containsLocalOutside() {
        HybridCanvasEllipse e = new HybridCanvasEllipse(10, 20, 5, 8);
        assertFalse(e.containsLocal(15.1, 20));
        assertFalse(e.containsLocal(10, 28.1));
        assertFalse(e.containsLocal(100, 100));
    }

    @Test
    void setCyBumpsVersion() {
        HybridCanvasEllipse e = new HybridCanvasEllipse(10, 20, 5, 8);
        long before = e.getVersion();
        e.setCy(30);
        assertEquals(before + 1, e.getVersion());
    }
}
