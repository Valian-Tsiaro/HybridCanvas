package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;

/** Ellipse shape defined by centre {@code (cx, cy)} and radii {@code (rx, ry)}. */
public final class HybridCanvasEllipse extends HybridCanvasShape {

    private double cx;
    private double cy;
    private double rx;
    private double ry;

    /**
     * @param cx centre x
     * @param cy centre y
     * @param rx horizontal radius
     * @param ry vertical radius
     */
    public HybridCanvasEllipse(double cx, double cy, double rx, double ry) {
        this.cx = cx;
        this.cy = cy;
        this.rx = rx;
        this.ry = ry;
    }

    /** @return centre x */
    public double getCx() { return cx; }

    /** @return centre y */
    public double getCy() { return cy; }

    /** @return horizontal radius */
    public double getRx() { return rx; }

    /** @return vertical radius */
    public double getRy() { return ry; }

    /** @param cx new centre x */
    public void setCx(double cx) { this.cx = cx; bumpVersion(); }

    /** @param cy new centre y */
    public void setCy(double cy) { this.cy = cy; bumpVersion(); }

    /** @param rx new horizontal radius */
    public void setRx(double rx) { this.rx = rx; bumpVersion(); }

    /** @param ry new vertical radius */
    public void setRy(double ry) { this.ry = ry; bumpVersion(); }

    /** {@inheritDoc} */
    @Override
    public Bounds2D getLocalBounds(Bounds2D out) {
        out.set(cx - rx, cy - ry, cx + rx, cy + ry);
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsLocal(double px, double py) {
        if (rx <= 0 || ry <= 0) return false;
        double nx = (px - cx) / rx;
        double ny = (py - cy) / ry;
        return nx * nx + ny * ny <= 1.0;
    }
}
