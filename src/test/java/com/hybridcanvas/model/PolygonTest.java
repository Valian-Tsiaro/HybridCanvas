package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolygonTest {

    @Test
    void pointsStoredFlat() {
        double[] pts = {0, 0, 10, 0, 5, 10};
        HybridCanvasPolygon p = new HybridCanvasPolygon(pts);
        assertEquals(3, p.getPointCount());
        assertEquals(0, p.getX(0));
        assertEquals(0, p.getY(0));
        assertEquals(10, p.getX(1));
        assertEquals(0, p.getY(1));
        assertEquals(5, p.getX(2));
        assertEquals(10, p.getY(2));
    }

    @Test
    void getPointReturnsTransientView() {
        HybridCanvasPolygon p = new HybridCanvasPolygon(new double[]{0, 0, 10, 0, 5, 10});
        HybridCanvasPoint a = p.getPoint(0);
        HybridCanvasPoint b = p.getPoint(1);
        assertSame(a, b);
        assertEquals(10, a.x);
        assertEquals(0, a.y);
    }

    @Test
    void setPointBumpsVersion() {
        HybridCanvasPolygon p = new HybridCanvasPolygon(new double[]{0, 0, 10, 0, 5, 10});
        long before = p.getVersion();
        p.setPoint(2, 5, 20);
        assertEquals(before + 1, p.getVersion());
        assertEquals(5, p.getX(2));
        assertEquals(20, p.getY(2));
    }

    @Test
    void containsLocalTriangleCentroidInside() {
        // equilateral-ish triangle
        double[] pts = {0, 0, 12, 0, 6, 10};
        HybridCanvasPolygon p = new HybridCanvasPolygon(pts);
        assertTrue(p.containsLocal(6, 3));
    }

    @Test
    void containsLocalFarOutside() {
        double[] pts = {0, 0, 12, 0, 6, 10};
        HybridCanvasPolygon p = new HybridCanvasPolygon(pts);
        assertFalse(p.containsLocal(100, 100));
        assertFalse(p.containsLocal(-50, -50));
    }

    @Test
    void constructorRejectsOddLengthPoints() {
        assertThrows(IllegalArgumentException.class, () ->
                new HybridCanvasPolygon(new double[]{0, 0, 10, 0, 5}));
    }

    @Test
    void boundsFromAllVertices() {
        double[] pts = {2, 5, 10, 3, 7, 15};
        HybridCanvasPolygon p = new HybridCanvasPolygon(pts);
        Bounds2D out = new Bounds2D();
        p.getLocalBounds(out);
        assertEquals(2, out.minX);
        assertEquals(3, out.minY);
        assertEquals(10, out.maxX);
        assertEquals(15, out.maxY);
    }
}
