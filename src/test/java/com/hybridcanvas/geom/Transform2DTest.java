package com.hybridcanvas.geom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static java.lang.Math.PI;

class Transform2DTest {

    private static final double EPS = 1e-9;

    @Test
    void identityMapsPointUnchanged() {
        Transform2D t = Transform2D.identity();
        assertEquals(3.0, t.mapX(3, 4), EPS);
        assertEquals(4.0, t.mapY(3, 4), EPS);
    }

    @Test
    void translateMapsPoint() {
        Transform2D t = Transform2D.translate(5, -2);
        assertEquals(6.0, t.mapX(1, 1), EPS);
        assertEquals(-1.0, t.mapY(1, 1), EPS);
    }

    @Test
    void scaleDoublesPoint() {
        Transform2D t = Transform2D.scale(2, 2);
        assertEquals(6.0, t.mapX(3, 4), EPS);
        assertEquals(8.0, t.mapY(3, 4), EPS);
    }

    @Test
    void rotate90MapsXToY() {
        Transform2D t = Transform2D.rotate(PI / 2);
        assertEquals(0.0, t.mapX(1, 0), EPS);
        assertEquals(1.0, t.mapY(1, 0), EPS);
    }

    @Test
    void concatOrderTranslateRotateScale() {
        // world = translate ∘ rotate ∘ scale
        Transform2D t = Transform2D.translate(10, 20)
                .concat(Transform2D.rotate(PI / 2))
                .concat(Transform2D.scale(2, 3));
        // (1,1) → scale(2,3) = (2,3) → rotate(π/2) = (-3,2) → translate(10,20) = (7,22)
        assertEquals(7.0, t.mapX(1, 1), EPS);
        assertEquals(22.0, t.mapY(1, 1), EPS);
    }

    @Test
    void inverseRoundTripsPoint() {
        Transform2D t = Transform2D.translate(3, 4)
                .concat(Transform2D.rotate(0.7))
                .concat(Transform2D.scale(2, 5));
        Transform2D inv = t.inverse();
        double x = 7, y = -3;
        double mx = t.mapX(x, y);
        double my = t.mapY(x, y);
        assertEquals(x, inv.mapX(mx, my), EPS);
        assertEquals(y, inv.mapY(mx, my), EPS);
    }

    @Test
    void transformBoundsRotatedRect() {
        // Rotate a (0,0)→(10,20) rect by π/2
        Transform2D t = Transform2D.rotate(PI / 2);
        Bounds2D in = new Bounds2D(0, 0, 10, 20);
        Bounds2D out = new Bounds2D();
        t.transformBounds(in, out);
        // Corners → (0,0), (0,10), (-20,10), (-20,0)
        assertEquals(-20, out.minX, EPS);
        assertEquals(0, out.minY, EPS);
        assertEquals(0, out.maxX, EPS);
        assertEquals(10, out.maxY, EPS);
    }

    @Test
    void transformBoundsTranslate() {
        Transform2D t = Transform2D.translate(5, 5);
        Bounds2D in = new Bounds2D(0, 0, 10, 10);
        Bounds2D out = new Bounds2D();
        t.transformBounds(in, out);
        assertEquals(5, out.minX, EPS);
        assertEquals(5, out.minY, EPS);
        assertEquals(15, out.maxX, EPS);
        assertEquals(15, out.maxY, EPS);
    }

    @Test
    void transformBoundsScale() {
        Transform2D t = Transform2D.scale(2, 3);
        Bounds2D in = new Bounds2D(0, 0, 10, 20);
        Bounds2D out = new Bounds2D();
        t.transformBounds(in, out);
        assertEquals(0, out.minX, EPS);
        assertEquals(0, out.minY, EPS);
        assertEquals(20, out.maxX, EPS);
        assertEquals(60, out.maxY, EPS);
    }

    @Test
    void transformBoundsInPlaceSafe() {
        // transformBounds(in, in) should not corrupt — corners read before write
        Transform2D t = Transform2D.rotate(PI / 2);
        Bounds2D b = new Bounds2D(0, 0, 10, 20);
        t.transformBounds(b, b);
        assertEquals(-20, b.minX, EPS);
        assertEquals(0, b.minY, EPS);
        assertEquals(0, b.maxX, EPS);
        assertEquals(10, b.maxY, EPS);
    }
}
