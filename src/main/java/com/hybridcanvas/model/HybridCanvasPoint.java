package com.hybridcanvas.model;

/**
 * Tiny mutable {@code (x, y)} transient view returned by
 * {@link HybridCanvasPolyShape#getPoint(int)}.
 * The same instance is reused per element — do not cache references
 * across calls.
 */
public final class HybridCanvasPoint {

    /** x coordinate. */
    public double x;

    /** y coordinate. */
    public double y;

    /** Default constructor; fields are zero-initialised. */
    public HybridCanvasPoint() {}

    /**
     * @param x initial x coordinate
     * @param y initial y coordinate
     */
    public HybridCanvasPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
