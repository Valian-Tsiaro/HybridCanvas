package com.hybridcanvas.geom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewportTest {

    private static final double EPS = 1e-9;

    @Test
    void defaultIsOneZero() {
        Viewport vp = new Viewport();
        assertEquals(1.0, vp.getZoom(), EPS);
        assertEquals(0.0, vp.getPanX(), EPS);
        assertEquals(0.0, vp.getPanY(), EPS);
    }

    @Test
    void worldScreenRoundTrip() {
        Viewport vp = new Viewport();
        vp.setZoom(2);
        vp.setPan(10, -5);
        assertEquals(16.0, vp.worldToScreenX(3, 7), EPS);
        assertEquals(9.0, vp.worldToScreenY(3, 7), EPS);
        assertEquals(3.0, vp.screenToWorldX(16, 9), EPS);
        assertEquals(7.0, vp.screenToWorldY(16, 9), EPS);
    }

    @Test
    void zoomAtKeepsWorldPointUnderCursor() {
        Viewport vp = new Viewport();
        // World point under screen (100,50) before zoom: (100,50)
        double wx = vp.screenToWorldX(100, 50);
        double wy = vp.screenToWorldY(100, 50);
        assertEquals(100, wx, EPS);
        assertEquals(50, wy, EPS);

        vp.zoomAt(100, 50, 2.0);
        assertEquals(2.0, vp.getZoom(), EPS);
        assertEquals(-100.0, vp.getPanX(), EPS);
        assertEquals(-50.0, vp.getPanY(), EPS);

        // Same world point still under screen (100,50)
        assertEquals(100, vp.screenToWorldX(100, 50), EPS);
        assertEquals(50, vp.screenToWorldY(100, 50), EPS);
    }

    @Test
    void zoomLimitsClampHigh() {
        Viewport vp = new Viewport();
        vp.setZoomLimits(0.5, 2.0);
        vp.zoomAt(0, 0, 100.0); // would zoom to 100 → clamped to 2
        assertEquals(2.0, vp.getZoom(), EPS);
    }

    @Test
    void zoomLimitsClampLow() {
        Viewport vp = new Viewport();
        vp.setZoomLimits(0.5, 2.0);
        vp.zoomAt(0, 0, 0.001); // would zoom to 0.001 → clamped to 0.5
        assertEquals(0.5, vp.getZoom(), EPS);
    }

    @Test
    void unboundedZoomBothAxes() {
        Viewport vp = new Viewport();
        // default -1 = unbounded
        vp.zoomAt(0, 0, 1000);
        assertEquals(1000, vp.getZoom(), EPS);
    }

    @Test
    void unboundedMaxOnly() {
        Viewport vp = new Viewport();
        vp.setZoomLimits(0.5, -1); // lower bound only
        vp.zoomAt(0, 0, 1000);
        assertEquals(1000, vp.getZoom(), EPS);
        vp.setZoom(0.1);
        assertEquals(0.5, vp.getZoom(), EPS);
    }

    @Test
    void unboundedMinOnly() {
        Viewport vp = new Viewport();
        vp.setZoomLimits(-1, 10); // upper bound only
        vp.zoomAt(0, 0, 0.001);
        assertEquals(0.001, vp.getZoom(), EPS);
        vp.setZoom(20);
        assertEquals(10, vp.getZoom(), EPS);
    }

    @Test
    void setZoomClamps() {
        Viewport vp = new Viewport();
        vp.setZoomLimits(0.5, 2.0);
        vp.setZoom(5);
        assertEquals(2.0, vp.getZoom(), EPS);
        vp.setZoom(0.1);
        assertEquals(0.5, vp.getZoom(), EPS);
    }

    @Test
    void setPanNoClamp() {
        Viewport vp = new Viewport();
        vp.setPan(123, 456);
        assertEquals(123, vp.getPanX(), EPS);
        assertEquals(456, vp.getPanY(), EPS);
    }
}
