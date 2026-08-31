package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HybridCanvasGroupTest {

    @Test
    void childrenListInitiallyEmpty() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        assertTrue(g.getChildren().isEmpty());
    }

    @Test
    void childrenListMutable() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        g.getChildren().add(rect);
        assertEquals(1, g.getChildren().size());
        assertTrue(g.getChildren().contains(rect));
        g.getChildren().remove(rect);
        assertTrue(g.getChildren().isEmpty());
    }

    @Test
    void emptyGroupBoundsIsEmpty() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        Bounds2D out = new Bounds2D();
        g.getLocalBounds(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void singleChildBoundsPassThrough() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(5, 10, 20, 30);
        g.getChildren().add(rect);
        Bounds2D out = new Bounds2D();
        g.getLocalBounds(out);
        assertEquals(5, out.minX, 0.0);
        assertEquals(10, out.minY, 0.0);
        assertEquals(25, out.maxX, 0.0);
        assertEquals(40, out.maxY, 0.0);
    }

    @Test
    void unionOfTwoChildBounds() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        g.getChildren().add(new HybridCanvasRectangle(0, 0, 10, 10));
        g.getChildren().add(new HybridCanvasRectangle(20, 20, 5, 5));
        Bounds2D out = new Bounds2D();
        g.getLocalBounds(out);
        assertEquals(0, out.minX, 0.0);
        assertEquals(0, out.minY, 0.0);
        assertEquals(25, out.maxX, 0.0);
        assertEquals(25, out.maxY, 0.0);
    }

    @Test
    void childTranslateAppliedToBounds() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        rect.setTranslateX(100);
        rect.setTranslateY(50);
        g.getChildren().add(rect);
        Bounds2D out = new Bounds2D();
        g.getLocalBounds(out);
        assertEquals(100, out.minX, 0.0);
        assertEquals(50, out.minY, 0.0);
        assertEquals(110, out.maxX, 0.0);
        assertEquals(60, out.maxY, 0.0);
    }

    @Test
    void childScaleAppliedToBounds() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        rect.setScaleX(2);
        rect.setScaleY(3);
        g.getChildren().add(rect);
        Bounds2D out = new Bounds2D();
        g.getLocalBounds(out);
        assertEquals(0, out.minX, 0.0);
        assertEquals(0, out.minY, 0.0);
        assertEquals(20, out.maxX, 0.0);
        assertEquals(30, out.maxY, 0.0);
    }

    @Test
    void nestedGroupsUnionCorrectly() {
        HybridCanvasRectangle rect = new HybridCanvasRectangle(0, 0, 10, 10);
        HybridCanvasGroup inner = new HybridCanvasGroup();
        inner.getChildren().add(rect);
        inner.setTranslateX(5);

        HybridCanvasRectangle rect2 = new HybridCanvasRectangle(0, 20, 10, 10);
        HybridCanvasGroup outer = new HybridCanvasGroup();
        outer.getChildren().add(inner);
        outer.getChildren().add(rect2);

        Bounds2D out = new Bounds2D();
        outer.getLocalBounds(out);
        assertEquals(0, out.minX, 0.0);
        assertEquals(0, out.minY, 0.0);
        assertEquals(15, out.maxX, 0.0);
        assertEquals(30, out.maxY, 0.0);
    }

    @Test
    void getLocalTransformIdentityByDefault() {
        HybridCanvasElement e = new HybridCanvasGroup();
        var t = e.getLocalTransform();
        assertEquals(1.0, t.m00, 0.0);
        assertEquals(0.0, t.m01, 0.0);
        assertEquals(0.0, t.m02, 0.0);
        assertEquals(0.0, t.m10, 0.0);
        assertEquals(1.0, t.m11, 0.0);
        assertEquals(0.0, t.m12, 0.0);
    }

    @Test
    void getLocalTransformAppliesTranslateScale() {
        HybridCanvasElement e = new HybridCanvasGroup();
        e.setTranslateX(10);
        e.setTranslateY(20);
        e.setScaleX(2);
        e.setScaleY(3);
        var t = e.getLocalTransform();
        // translate(10,20) ∘ scale(2,3)  (rotate=0)
        assertEquals(2.0, t.m00, 1e-9);
        assertEquals(0.0, t.m01, 1e-9);
        assertEquals(10.0, t.m02, 1e-9);
        assertEquals(0.0, t.m10, 1e-9);
        assertEquals(3.0, t.m11, 1e-9);
        assertEquals(20.0, t.m12, 1e-9);
    }

    @Test
    void getLocalTransformAppliesTranslateRotateScale() {
        HybridCanvasElement e = new HybridCanvasGroup();
        e.setTranslateX(10);
        e.setTranslateY(20);
        e.setRotate(Math.PI / 2);
        e.setScaleX(2);
        e.setScaleY(3);
        var t = e.getLocalTransform();
        // translate(10,20) ∘ rotate(π/2) ∘ scale(2,3)
        double cos = Math.cos(Math.PI / 2); // ≈ 0
        double sin = Math.sin(Math.PI / 2); // ≈ 1
        assertEquals(cos * 2, t.m00, 1e-9);   // m00 = cos·sx
        assertEquals(-sin * 3, t.m01, 1e-9);  // m01 = -sin·sy
        assertEquals(10.0, t.m02, 1e-9);       // tx
        assertEquals(sin * 2, t.m10, 1e-9);   // m10 = sin·sx
        assertEquals(cos * 3, t.m11, 1e-9);   // m11 = cos·sy
        assertEquals(20.0, t.m12, 1e-9);       // ty
    }

    @Test
    void childRotateAppliedToBounds() {
        HybridCanvasGroup g = new HybridCanvasGroup();
        HybridCanvasRectangle rect = new HybridCanvasRectangle(-5, -5, 10, 10);
        rect.setRotate(Math.PI / 2);
        g.getChildren().add(rect);
        Bounds2D out = new Bounds2D();
        g.getLocalBounds(out);
        // 10×10 box rotated 90° about origin → still 10×10 centered at origin
        assertEquals(-5, out.minX, 1e-9);
        assertEquals(-5, out.minY, 1e-9);
        assertEquals(5, out.maxX, 1e-9);
        assertEquals(5, out.maxY, 1e-9);
    }
}
