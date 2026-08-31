package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageShapeTest {

    // ── imageRef get/set ──────────────────────────────────────────────

    @Test
    void imageRefGetSetBumpsVersion() {
        var r = new HybridCanvasImageRect("ship.png", 0, 0, 10, 10);
        long v = r.getVersion();
        r.setImageRef("ship2.png");
        assertEquals("ship2.png", r.getImageRef());
        assertEquals(v + 1, r.getVersion());
    }

    // ── HybridCanvasImageRect ─────────────────────────────────────────

    @Test
    void imageRectLocalBoundsMatchesGeometry() {
        var r = new HybridCanvasImageRect("a", 10, 20, 30, 40);
        Bounds2D out = new Bounds2D();
        r.getLocalBounds(out);
        assertEquals(10, out.minX);
        assertEquals(20, out.minY);
        assertEquals(40, out.maxX);
        assertEquals(60, out.maxY);
    }

    @Test
    void imageRectContainsLocalInside() {
        var r = new HybridCanvasImageRect("a", 0, 0, 100, 50);
        assertTrue(r.containsLocal(50, 25));
    }

    @Test
    void imageRectContainsLocalCorner() {
        var r = new HybridCanvasImageRect("a", 0, 0, 100, 50);
        assertTrue(r.containsLocal(0, 0));
        assertTrue(r.containsLocal(100, 50));
    }

    @Test
    void imageRectContainsLocalOutside() {
        var r = new HybridCanvasImageRect("a", 0, 0, 100, 50);
        assertFalse(r.containsLocal(101, 25));
        assertFalse(r.containsLocal(-1, 25));
        assertFalse(r.containsLocal(50, 51));
    }

    @Test
    void imageRectNegativeWidth() {
        var r = new HybridCanvasImageRect("a", 50, 50, -30, -20);
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
    void imageRectSetXBumpsVersion() {
        var r = new HybridCanvasImageRect("a", 0, 0, 10, 10);
        long before = r.getVersion();
        r.setX(5);
        assertEquals(before + 1, r.getVersion());
    }

    // ── HybridCanvasImageInShape ──────────────────────────────────────

    @Test
    void imageInShapeContainsLocalInside() {
        // triangle: (0,0) (20,0) (10,15)
        var is = new HybridCanvasImageInShape("mask.png",
                new double[]{0, 0, 20, 0, 10, 15});
        assertTrue(is.containsLocal(10, 3));
    }

    @Test
    void imageInShapeContainsLocalOutside() {
        var is = new HybridCanvasImageInShape("mask.png",
                new double[]{0, 0, 20, 0, 10, 15});
        assertFalse(is.containsLocal(50, 50));
        assertFalse(is.containsLocal(0, -1));
    }

    @Test
    void imageInShapeLocalBoundsHull() {
        var is = new HybridCanvasImageInShape("mask.png",
                new double[]{0, 0, 20, 0, 10, 15});
        Bounds2D out = new Bounds2D();
        is.getLocalBounds(out);
        assertEquals(0, out.minX);
        assertEquals(0, out.minY);
        assertEquals(20, out.maxX);
        assertEquals(15, out.maxY);
    }

    @Test
    void imageInShapeRejectsOddPointCount() {
        assertThrows(IllegalArgumentException.class,
                () -> new HybridCanvasImageInShape("mask.png",
                        new double[]{0, 0, 20}));
    }

    @Test
    void imageInShapeSetPointBumpsVersion() {
        var is = new HybridCanvasImageInShape("mask.png",
                new double[]{0, 0, 20, 0, 10, 15});
        long before = is.getVersion();
        is.setPoint(1, 20, 5);
        assertEquals(before + 1, is.getVersion());
        assertEquals(20, is.getX(1));
        assertEquals(5, is.getY(1));
    }

    // ── HybridCanvasImageWithOverlay ──────────────────────────────────

    @Test
    void imageWithOverlayBoundsIsUnion() {
        var img = new HybridCanvasImageRect("bg.png", 0, 0, 10, 10);
        var overlay = new HybridCanvasRectangle(20, 20, 30, 30);
        var iwo = new HybridCanvasImageWithOverlay("bg.png", 0, 0, 10, 10, overlay);

        Bounds2D out = new Bounds2D();
        iwo.getLocalBounds(out);
        assertEquals(0, out.minX);
        assertEquals(0, out.minY);
        assertEquals(50, out.maxX);
        assertEquals(50, out.maxY);
    }

    @Test
    void imageWithOverlayContainsLocalInRect() {
        var overlay = new HybridCanvasRectangle(20, 20, 30, 30);
        var iwo = new HybridCanvasImageWithOverlay("bg.png", 0, 0, 10, 10, overlay);
        assertTrue(iwo.containsLocal(5, 5));
    }

    @Test
    void imageWithOverlayContainsLocalInOverlay() {
        var overlay = new HybridCanvasRectangle(20, 20, 30, 30);
        var iwo = new HybridCanvasImageWithOverlay("bg.png", 0, 0, 10, 10, overlay);
        assertTrue(iwo.containsLocal(25, 25));
    }

    @Test
    void imageWithOverlayContainsLocalOutsideBoth() {
        var overlay = new HybridCanvasRectangle(20, 20, 30, 30);
        var iwo = new HybridCanvasImageWithOverlay("bg.png", 0, 0, 10, 10, overlay);
        assertFalse(iwo.containsLocal(100, 100));
    }

    @Test
    void imageWithOverlaySetOverlayBumpsVersion() {
        var overlay1 = new HybridCanvasRectangle(0, 0, 10, 10);
        var overlay2 = new HybridCanvasRectangle(50, 50, 20, 20);
        var iwo = new HybridCanvasImageWithOverlay("bg.png", 0, 0, 10, 10, overlay1);
        long before = iwo.getVersion();
        iwo.setOverlay(overlay2);
        assertEquals(before + 1, iwo.getVersion());
        assertSame(overlay2, iwo.getOverlay());
    }

    @Test
    void imageWithOverlayTransformAppliedToBounds() {
        var overlay = new HybridCanvasRectangle(0, 0, 10, 10);
        overlay.setTranslateX(100);
        overlay.setTranslateY(100);
        var iwo = new HybridCanvasImageWithOverlay("bg.png", 0, 0, 10, 10, overlay);
        Bounds2D out = new Bounds2D();
        iwo.getLocalBounds(out);
        assertEquals(0, out.minX);
        assertEquals(0, out.minY);
        assertEquals(110, out.maxX);
        assertEquals(110, out.maxY);
    }

    @Test
    void imageWithOverlayTransformAppliedToContainsLocal() {
        var overlay = new HybridCanvasRectangle(0, 0, 10, 10);
        overlay.setTranslateX(100);
        overlay.setTranslateY(100);
        var iwo = new HybridCanvasImageWithOverlay("bg.png", 0, 0, 10, 10, overlay);
        // point at overlay's translated position (105, 105) should hit
        assertTrue(iwo.containsLocal(105, 105));
        // point between both rects — outside both
        assertFalse(iwo.containsLocal(50, 50));
        // still in image rect
        assertTrue(iwo.containsLocal(3, 3));
    }
}
