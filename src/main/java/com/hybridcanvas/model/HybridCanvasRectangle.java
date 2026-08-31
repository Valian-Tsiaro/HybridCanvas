package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;

/** Rectangle shape defined by the corner {@code (x, y)} and size {@code (w, h)}. */
public final class HybridCanvasRectangle extends HybridCanvasShape {

    private double x;
    private double y;
    private double w;
    private double h;

    /**
     * @param x x coordinate of the rectangle origin
     * @param y y coordinate of the rectangle origin
     * @param w width; may be negative
     * @param h height; may be negative
     */
    public HybridCanvasRectangle(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /** @return x coordinate of the rectangle origin */
    public double getX() { return x; }

    /** @return y coordinate of the rectangle origin */
    public double getY() { return y; }

    /** @return width of the rectangle (may be negative) */
    public double getWidth() { return w; }

    /** @return height of the rectangle (may be negative) */
    public double getHeight() { return h; }

    /**
     * @param x new x coordinate
     */
    public void setX(double x) { this.x = x; bumpVersion(); }

    /**
     * @param y new y coordinate
     */
    public void setY(double y) { this.y = y; bumpVersion(); }

    /**
     * @param w new width
     */
    public void setWidth(double w) { this.w = w; bumpVersion(); }

    /**
     * @param h new height
     */
    public void setHeight(double h) { this.h = h; bumpVersion(); }

    /**
     * {@inheritDoc}
     * Computes min/max from origin and extent; negative {@code w} or {@code h} are handled correctly.
     */
    @Override
    public Bounds2D getLocalBounds(Bounds2D out) {
        double x1 = Math.min(x, x + w);
        double y1 = Math.min(y, y + h);
        double x2 = Math.max(x, x + w);
        double y2 = Math.max(y, y + h);
        out.set(x1, y1, x2, y2);
        return out;
    }

    /**
     * {@inheritDoc}
     * Negative {@code w} or {@code h} are handled correctly.
     */
    @Override
    public boolean containsLocal(double px, double py) {
        double x1 = Math.min(x, x + w);
        double y1 = Math.min(y, y + h);
        double x2 = Math.max(x, x + w);
        double y2 = Math.max(y, y + h);
        return px >= x1 && px <= x2 && py >= y1 && py <= y2;
    }
}
