package com.hybridcanvas.model;

/** Cubic (4 control points) or quadratic (3 control points) Bezier curve. */
public final class HybridCanvasBezier extends HybridCanvasPolyShape {

    /**
     * @param ctrl flat interleaved control-point array:
     *             {@code [x0, y0, x1, y1, x2, y2]} for quadratic or
     *             {@code [x0, y0, x1, y1, x2, y2, x3, y3]} for cubic
     * @throws IllegalArgumentException if {@code ctrl.length} is not 6 (quad) or 8 (cubic)
     */
    public HybridCanvasBezier(double[] ctrl) {
        super(ctrl);
        int n = ctrl.length;
        if (n != 6 && n != 8) {
            throw new IllegalArgumentException(
                    "Bezier needs 3 (quad) or 4 (cubic) control points, got " + (n / 2));
        }
    }

    /**
     * {@inheritDoc}
     * Uses fixed-sample flattening (24 segments) and compares
     * the test point's distance to each flattened segment against
     * {@code strokeWidth / 2}.
     */
    @Override
    public boolean containsLocal(double x, double y) {
        double tol = getStrokeWidth() * 0.5;
        boolean cubic = points.length == 8;
        int samples = 24; // ponytail: fixed sample count; increase if curves at tight radii cause misses

        double p0x = points[0], p0y = points[1];
        double p1x = points[2], p1y = points[3];
        double p2x = points[4], p2y = points[5];
        double p3x = cubic ? points[6] : 0;
        double p3y = cubic ? points[7] : 0;

        double prevX = p0x, prevY = p0y;
        for (int i = 1; i <= samples; i++) {
            double t = (double) i / samples;
            double mt = 1.0 - t;
            double ex, ey;
            if (cubic) {
                double a = mt * mt * mt;
                double b = 3 * mt * mt * t;
                double c = 3 * mt * t * t;
                double d = t * t * t;
                ex = a * p0x + b * p1x + c * p2x + d * p3x;
                ey = a * p0y + b * p1y + c * p2y + d * p3y;
            } else {
                ex = mt * mt * p0x + 2 * mt * t * p1x + t * t * p2x;
                ey = mt * mt * p0y + 2 * mt * t * p1y + t * t * p2y;
            }
            if (pointToSegment(x, y, prevX, prevY, ex, ey) <= tol) return true;
            prevX = ex;
            prevY = ey;
        }
        return false;
    }
}
