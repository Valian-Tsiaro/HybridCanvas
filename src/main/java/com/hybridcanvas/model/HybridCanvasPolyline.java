package com.hybridcanvas.model;

/** Open polyline — stroke only, no implicit closing edge. */
public final class HybridCanvasPolyline extends HybridCanvasPolyShape {

    /**
     * @param points flat interleaved {@code double[]} {@code [x0, y0, x1, y1, …]}
     *               representing the polyline vertices in order; must have even length
     * @throws IllegalArgumentException if {@code points.length} is odd
     */
    public HybridCanvasPolyline(double[] points) {
        super(points);
        if ((points.length & 1) != 0) {
            throw new IllegalArgumentException("points must have even length, got " + points.length);
        }
    }

    /**
     * {@inheritDoc}
     * Returns {@code true} when the test point is within
     * {@code strokeWidth / 2} of any open segment.
     */
    @Override
    public boolean containsLocal(double x, double y) {
        double tol = getStrokeWidth() * 0.5;
        int n = getPointCount();
        for (int i = 0; i < n - 1; i++) {
            double d = pointToSegment(x, y,
                    points[2 * i], points[2 * i + 1],
                    points[2 * (i + 1)], points[2 * (i + 1) + 1]);
            if (d <= tol) return true;
        }
        return false;
    }
}
