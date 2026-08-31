package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BezierTest {

    @Test
    void cubicRecognized() {
        double[] ctrl = {0, 0, 3, 10, 7, 10, 10, 0};
        HybridCanvasBezier b = new HybridCanvasBezier(ctrl);
        assertEquals(4, b.getPointCount());
    }

    @Test
    void quadRecognized() {
        double[] ctrl = {0, 0, 5, 10, 10, 0};
        HybridCanvasBezier b = new HybridCanvasBezier(ctrl);
        assertEquals(3, b.getPointCount());
    }

    @Test
    void invalidControlCountThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new HybridCanvasBezier(new double[]{0, 0, 1, 1, 2, 2, 3, 3, 4, 4}));
    }

    @Test
    void getLocalBoundsCoversControlHull() {
        double[] ctrl = {2, 5, 10, 3, 7, 15};
        HybridCanvasBezier b = new HybridCanvasBezier(ctrl);
        Bounds2D out = new Bounds2D();
        b.getLocalBounds(out);
        // hull = min/max of all control points
        assertEquals(2, out.minX);
        assertEquals(3, out.minY);
        assertEquals(10, out.maxX);
        assertEquals(15, out.maxY);
        // endpoints (t=0 and t=1) should be inside the hull
        assertTrue(out.contains(2, 5));   // t=0 = P0
        assertTrue(out.contains(7, 15));  // t=1 = P2
    }

    @Test
    void containsOnEndpointTrue() {
        double[] ctrl = {0, 0, 5, 10, 10, 0};
        HybridCanvasBezier b = new HybridCanvasBezier(ctrl);
        // t=0 endpoint = (0,0)
        assertTrue(b.containsLocal(0, 0));
        // t=1 endpoint = (10,0)
        assertTrue(b.containsLocal(10, 0));
    }

    @Test
    void containsFarFalse() {
        double[] ctrl = {0, 0, 5, 10, 10, 0};
        HybridCanvasBezier b = new HybridCanvasBezier(ctrl);
        assertFalse(b.containsLocal(100, 100));
    }
}
