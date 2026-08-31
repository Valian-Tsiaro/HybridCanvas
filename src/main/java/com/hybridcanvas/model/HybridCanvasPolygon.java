package com.hybridcanvas.model;

/** Closed polygon — fill and stroke. Points stored as flat interleaved {@code double[]}. */
public final class HybridCanvasPolygon extends HybridCanvasPolyShape {

    /**
     * @param points flat interleaved {@code double[]} {@code [x0, y0, x1, y1, …]}
     *               representing the polygon vertices; must have even length
     * @throws IllegalArgumentException if {@code points.length} is odd
     */
    public HybridCanvasPolygon(double[] points) {
        super(points);
        if ((points.length & 1) != 0) {
            throw new IllegalArgumentException("points must have even length, got " + points.length);
        }
    }

    /** {@inheritDoc} Uses the even-odd rule for interior detection. */
    @Override
    public boolean containsLocal(double x, double y) {
        int n = getPointCount();
        if (n < 3) return false;

        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = points[2 * i];
            double yi = points[2 * i + 1];
            double xj = points[2 * j];
            double yj = points[2 * j + 1];

            if (((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        return inside;
    }
}
