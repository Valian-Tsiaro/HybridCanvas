package com.hybridcanvas.model;

/**
 * Sealed abstract base for the five non-image shape types.
 * Carries appearance fields ({@code fill}, {@code stroke}, {@code strokeWidth})
 * and layer/z-order metadata. Geometry contract is local-space only:
 * {@link #getLocalBounds} and {@link #containsLocal}.
 */
public sealed abstract class HybridCanvasShape extends HybridCanvasElement
        permits HybridCanvasPolyShape, HybridCanvasRectangle, HybridCanvasEllipse {

    private HybridCanvasColor fill;
    private HybridCanvasColor stroke;
    private double strokeWidth;
    private String layerId;
    private int zOrder;

    /** Default stroke is opaque black; strokeWidth is 1.0. */
    protected HybridCanvasShape() {
        this.stroke = HybridCanvasColor.fromRgb(0, 0, 0);
        this.strokeWidth = 1.0;
    }

    /** @return the fill color, or {@code null} if no fill is set */
    public HybridCanvasColor getFill() {
        return fill;
    }

    /**
     * @param fill the fill color, or {@code null} for no fill
     */
    public void setFill(HybridCanvasColor fill) {
        this.fill = fill;
        bumpVersion();
    }

    /** @return the stroke color (never {@code null}) */
    public HybridCanvasColor getStroke() {
        return stroke;
    }

    /**
     * @param stroke the stroke color; must not be {@code null}
     * @throws IllegalArgumentException if {@code stroke} is {@code null}
     */
    public void setStroke(HybridCanvasColor stroke) {
        if (stroke == null) throw new IllegalArgumentException("stroke must not be null");
        this.stroke = stroke;
        bumpVersion();
    }

    /** @return the stroke width in world units (scales with zoom) */
    public double getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * @param strokeWidth the stroke width in world units (should be non-negative)
     */
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        bumpVersion();
    }

    /** @return the layer id, or {@code null} if unassigned */
    public String getLayerId() {
        return layerId;
    }

    /**
     * @param layerId the layer id, or {@code null} to leave unassigned
     */
    public void setLayerId(String layerId) {
        this.layerId = layerId;
        bumpVersion();
    }

    /** @return the z-order; higher values are drawn later */
    public int getZOrder() {
        return zOrder;
    }

    /**
     * @param zOrder the z-order; higher values are drawn later
     */
    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
        bumpVersion();
    }

    /**
     * Point-in-shape test in local space. No world-coord conversion is applied.
     *
     * @param x local x coordinate
     * @param y local y coordinate
     * @return {@code true} if the point is inside or on the boundary of this shape
     */
    public abstract boolean containsLocal(double x, double y);
}
