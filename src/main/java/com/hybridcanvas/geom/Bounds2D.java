package com.hybridcanvas.geom;

/**
 * Mutable 2D axis-aligned bounding box. Pure double math, no {@code javafx.graphics}.
 * Public fields for zero-alloc hot-path access; all mutation methods are in-place.
 */
public final class Bounds2D {

    /** Minimum x coordinate. */
    public double minX;

    /** Minimum y coordinate. */
    public double minY;

    /** Maximum x coordinate. */
    public double maxX;

    /** Maximum y coordinate. */
    public double maxY;

    /**
     * Defaults to {@code (0,0,0,0)}. Not an empty sentinel — {@link #union} assumes this box
     * is already initialized, so callers starting from empty must set initial bounds first.
     */
    // ponytail: Prompt 04 must define empty/infinite sentinel for Group union.
    public Bounds2D() {}

    /**
     * @param minX initial minimum x
     * @param minY initial minimum y
     * @param maxX initial maximum x
     * @param maxY initial maximum y
     */
    public Bounds2D(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * @param minX new minimum x
     * @param minY new minimum y
     * @param maxX new maximum x
     * @param maxY new maximum y
     */
    public void set(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /** @param other bounds to copy from */
    public void set(Bounds2D other) {
        this.minX = other.minX;
        this.minY = other.minY;
        this.maxX = other.maxX;
        this.maxY = other.maxY;
    }

    /** @return a new independent copy of this box */
    public Bounds2D copy() {
        return new Bounds2D(minX, minY, maxX, maxY);
    }

    /**
     * Expands this box to include {@code other}.
     *
     * @param other box to union with
     */
    public void union(Bounds2D other) {
        if (other.minX < minX) minX = other.minX;
        if (other.minY < minY) minY = other.minY;
        if (other.maxX > maxX) maxX = other.maxX;
        if (other.maxY > maxY) maxY = other.maxY;
    }

    /**
     * Expands this box outward by {@code dx} on each X side and {@code dy} on each Y side.
     *
     * @param dx expansion along X axis per side
     * @param dy expansion along Y axis per side
     */
    public void expand(double dx, double dy) {
        minX -= dx;
        minY -= dy;
        maxX += dx;
        maxY += dy;
    }

    /**
     * @param x test x coordinate
     * @param y test y coordinate
     * @return true if the point is inside or on the edge of this box
     */
    public boolean contains(double x, double y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /**
     * @param other box to test
     * @return true if this box overlaps or touches {@code other}
     */
    public boolean intersects(Bounds2D other) {
        return minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY;
    }

    /** @return width ({@code maxX - minX}) */
    public double width() {
        return maxX - minX;
    }

    /** @return height ({@code maxY - minY}) */
    public double height() {
        return maxY - minY;
    }
}
