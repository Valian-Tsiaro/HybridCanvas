package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;

/**
 * Image clipped to a polygon mask. {@code containsLocal} uses the even-odd
 * rule on the mask points.
 */
public final class HybridCanvasImageInShape extends HybridCanvasImageShape {

    /** Flat interleaved mask points {@code [x0, y0, x1, y1, …]}. */
    private final double[] mask;

    /**
     * @param imageRef image reference key
     * @param mask flat interleaved {@code double[]} polygon mask vertices; must have even length
     * @throws IllegalArgumentException if {@code mask.length} is odd
     */
    public HybridCanvasImageInShape(String imageRef, double[] mask) {
        super(imageRef);
        if ((mask.length & 1) != 0) {
            throw new IllegalArgumentException("mask must have even length, got " + mask.length);
        }
        this.mask = mask;
    }

    /** @return the number of mask vertices */
    public int getPointCount() {
        return mask.length / 2;
    }

    /**
     * @param i zero-based vertex index
     * @return the x coordinate of the i-th vertex
     */
    public double getX(int i) { return mask[2 * i]; }

    /**
     * @param i zero-based vertex index
     * @return the y coordinate of the i-th vertex
     */
    public double getY(int i) { return mask[2 * i + 1]; }

    /**
     * Sets the coordinates of the i-th mask vertex.
     *
     * @param i zero-based vertex index
     * @param x new x coordinate
     * @param y new y coordinate
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    public void setPoint(int i, double x, double y) {
        mask[2 * i] = x;
        mask[2 * i + 1] = y;
        bumpVersion();
    }

    /**
     * {@inheritDoc}
     * Bounds are the axis-aligned hull of all mask vertices.
     */
    @Override
    public Bounds2D getLocalBounds(Bounds2D out) {
        if (mask.length == 0) {
            out.set(0, 0, 0, 0);
            return out;
        }
        double minX = mask[0], minY = mask[1], maxX = mask[0], maxY = mask[1];
        for (int i = 2; i < mask.length; i += 2) {
            double px = mask[i];
            double py = mask[i + 1];
            if (px < minX) minX = px;
            if (px > maxX) maxX = px;
            if (py < minY) minY = py;
            if (py > maxY) maxY = py;
        }
        out.set(minX, minY, maxX, maxY);
        return out;
    }

    /** {@inheritDoc} Even-odd rule on the mask polygon. */
    // ponytail: even-odd duplicated from HybridCanvasPolygon.containsLocal;
    // extract a shared static helper when a third consumer appears.
    @Override
    public boolean containsLocal(double x, double y) {
        int n = getPointCount();
        if (n < 3) return false;

        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = mask[2 * i];
            double yi = mask[2 * i + 1];
            double xj = mask[2 * j];
            double yj = mask[2 * j + 1];

            if (((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        return inside;
    }
}
