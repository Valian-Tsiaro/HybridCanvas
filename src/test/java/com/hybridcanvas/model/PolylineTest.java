package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolylineTest {

    @Test
    void openNoImplicitClosingEdge() {
        // L-shape polyline: (0,0) -> (10,0) -> (10,10)
        double[] pts = {0, 0, 10, 0, 10, 10};
        HybridCanvasPolyline pl = new HybridCanvasPolyline(pts);
        // point near the hypothetical closing edge (0,0)-(10,10) but far from real segments
        // e.g. (0,10) is 10 units from the diagonal closing edge, but also >tolerance from segments
        assertFalse(pl.containsLocal(1, 1));
    }

    @Test
    void containsOnSegmentTrue() {
        double[] pts = {0, 0, 10, 0, 10, 10};
        HybridCanvasPolyline pl = new HybridCanvasPolyline(pts);
        // strokeWidth=1.0 default => tolerance=0.5
        assertTrue(pl.containsLocal(5, 0));     // midpoint of first segment
        assertTrue(pl.containsLocal(10, 5));    // midpoint of second segment
    }

    @Test
    void containsFarFalse() {
        double[] pts = {0, 0, 10, 0};
        HybridCanvasPolyline pl = new HybridCanvasPolyline(pts);
        assertFalse(pl.containsLocal(5, 10));
        assertFalse(pl.containsLocal(-10, -10));
    }

    @Test
    void constructorRejectsOddLengthPoints() {
        assertThrows(IllegalArgumentException.class, () ->
                new HybridCanvasPolyline(new double[]{0, 0, 10, 0, 5}));
    }

    @Test
    void boundsFromVertices() {
        double[] pts = {2, 5, 10, 3, 7, 15};
        HybridCanvasPolyline pl = new HybridCanvasPolyline(pts);
        Bounds2D out = new Bounds2D();
        pl.getLocalBounds(out);
        assertEquals(2, out.minX);
        assertEquals(3, out.minY);
        assertEquals(10, out.maxX);
        assertEquals(15, out.maxY);
    }

    @Test
    void setPointBumpsVersion() {
        HybridCanvasPolyline pl = new HybridCanvasPolyline(new double[]{0, 0, 10, 0});
        long before = pl.getVersion();
        pl.setPoint(1, 10, 10);
        assertEquals(before + 1, pl.getVersion());
    }
}
