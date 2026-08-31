package com.hybridcanvas.geom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bounds2DTest {

    @Test
    void setAndGet() {
        Bounds2D b = new Bounds2D();
        b.set(1, 2, 3, 4);
        assertEquals(1, b.minX);
        assertEquals(2, b.minY);
        assertEquals(3, b.maxX);
        assertEquals(4, b.maxY);
    }

    @Test
    void constructorSetsFields() {
        Bounds2D b = new Bounds2D(-5, -10, 15, 20);
        assertEquals(-5, b.minX);
        assertEquals(-10, b.minY);
        assertEquals(15, b.maxX);
        assertEquals(20, b.maxY);
    }

    @Test
    void unionGrowsToContain() {
        Bounds2D b = new Bounds2D(0, 0, 10, 10);
        b.union(new Bounds2D(5, 5, 20, 20));
        assertEquals(0, b.minX);
        assertEquals(0, b.minY);
        assertEquals(20, b.maxX);
        assertEquals(20, b.maxY);
    }

    @Test
    void unionDisjoint() {
        Bounds2D b = new Bounds2D(0, 0, 5, 5);
        b.union(new Bounds2D(10, 10, 15, 15));
        assertEquals(0, b.minX);
        assertEquals(0, b.minY);
        assertEquals(15, b.maxX);
        assertEquals(15, b.maxY);
    }

    @Test
    void expandByDelta() {
        Bounds2D b = new Bounds2D(0, 0, 10, 10);
        b.expand(5, 5);
        assertEquals(-5, b.minX);
        assertEquals(-5, b.minY);
        assertEquals(15, b.maxX);
        assertEquals(15, b.maxY);
    }

    @Test
    void containsInside() {
        Bounds2D b = new Bounds2D(0, 0, 10, 10);
        assertTrue(b.contains(5, 5));
        assertTrue(b.contains(0, 0));
        assertTrue(b.contains(10, 10));
    }

    @Test
    void containsOnEdge() {
        Bounds2D b = new Bounds2D(0, 0, 10, 10);
        assertTrue(b.contains(5, 0));
        assertTrue(b.contains(10, 5));
    }

    @Test
    void containsOutside() {
        Bounds2D b = new Bounds2D(0, 0, 10, 10);
        assertFalse(b.contains(11, 5));
        assertFalse(b.contains(5, -1));
        assertFalse(b.contains(-1, -1));
    }

    @Test
    void intersectsOverlap() {
        Bounds2D a = new Bounds2D(0, 0, 10, 10);
        Bounds2D b = new Bounds2D(5, 5, 15, 15);
        assertTrue(a.intersects(b));
        assertTrue(b.intersects(a));
    }

    @Test
    void intersectsTouch() {
        Bounds2D a = new Bounds2D(0, 0, 10, 10);
        Bounds2D b = new Bounds2D(10, 0, 20, 10);
        assertTrue(a.intersects(b));
    }

    @Test
    void intersectsDisjoint() {
        Bounds2D a = new Bounds2D(0, 0, 10, 10);
        Bounds2D b = new Bounds2D(11, 0, 21, 10);
        assertFalse(a.intersects(b));
    }

    @Test
    void widthAndHeight() {
        Bounds2D b = new Bounds2D(0, 0, 30, 20);
        assertEquals(30, b.width());
        assertEquals(20, b.height());
    }

    @Test
    void copyIndependence() {
        Bounds2D b = new Bounds2D(1, 2, 3, 4);
        Bounds2D c = b.copy();
        assertEquals(b.minX, c.minX);
        c.set(10, 20, 30, 40);
        assertEquals(1, b.minX);
    }

    @Test
    void setFromOtherBounds() {
        Bounds2D a = new Bounds2D(0, 0, 5, 5);
        Bounds2D b = new Bounds2D(10, 20, 30, 40);
        a.set(b);
        assertEquals(10, a.minX);
        assertEquals(20, a.minY);
        assertEquals(30, a.maxX);
        assertEquals(40, a.maxY);
    }

    @Test
    void setEmptyCreatesEmptyBounds() {
        Bounds2D b = new Bounds2D(1, 2, 3, 4);
        assertFalse(b.isEmpty());
        b.setEmpty();
        assertTrue(b.isEmpty());
    }

    @Test
    void emptyBoundsFailsContains() {
        Bounds2D b = new Bounds2D();
        b.setEmpty();
        assertFalse(b.contains(0, 0));
    }

    @Test
    void emptyBoundsFailsIntersects() {
        Bounds2D a = new Bounds2D();
        a.setEmpty();
        Bounds2D b = new Bounds2D(0, 0, 10, 10);
        assertFalse(a.intersects(b));
    }

    @Test
    void normalBoundsAreNotEmpty() {
        Bounds2D b = new Bounds2D(0, 0, 10, 10);
        assertFalse(b.isEmpty());
    }
}
