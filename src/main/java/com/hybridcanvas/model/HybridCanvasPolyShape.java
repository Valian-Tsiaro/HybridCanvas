package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;

/**
 * Shared sealed abstract for point-based shapes (polygon, polyline, bezier).
 * Holds the flat {@code double[]} interleaved points and provides common accessors.
 */
public sealed abstract class HybridCanvasPolyShape extends HybridCanvasShape
        permits HybridCanvasPolygon, HybridCanvasPolyline, HybridCanvasBezier {

    /** Flat interleaved point array {@code [x0, y0, x1, y1, …]}. */
    protected final double[] points;

    /**
     * Reused transient view returned by {@link #getPoint(int)}.
     * Callers must not cache the reference across calls.
     */
    private final HybridCanvasPoint pointView = new HybridCanvasPoint();

    /**
     * @param points flat interleaved {@code double[]} {@code [x0, y0, x1, y1, …]}
     */
    protected HybridCanvasPolyShape(double[] points) {
        this.points = points;
    }

    /** @return the number of points in this shape (i.e. {@code points.length / 2}) */
    public int getPointCount() {
        return points.length / 2;
    }

    /**
     * @param i zero-based point index
     * @return the x coordinate of the i-th point
     */
    public double getX(int i) {
        return points[2 * i];
    }

    /**
     * @param i zero-based point index
     * @return the y coordinate of the i-th point
     */
    public double getY(int i) {
        return points[2 * i + 1];
    }

    /**
     * Returns a transient mutable view of the i-th point. The same
     * {@link HybridCanvasPoint} instance is reused across calls — callers
     * must not cache the returned reference.
     *
     * @param i zero-based point index
     * @return a transient view whose {@code x}/{@code y} reflect the i-th point
     */
    public HybridCanvasPoint getPoint(int i) {
        pointView.x = points[2 * i];
        pointView.y = points[2 * i + 1];
        return pointView;
    }

    /**
     * Sets the coordinates of the i-th point and bumps the element version.
     *
     * @param i zero-based point index
     * @param x new x coordinate
     * @param y new y coordinate
     */
    public void setPoint(int i, double x, double y) {
        points[2 * i] = x;
        points[2 * i + 1] = y;
        bumpVersion();
    }

    /**
     * {@inheritDoc}
     * Bounds are the axis-aligned hull of all stored points.
     */
    @Override
    public Bounds2D getLocalBounds(Bounds2D out) {
        if (points.length == 0) {
            out.set(0, 0, 0, 0);
            return out;
        }
        double minX = points[0], minY = points[1], maxX = points[0], maxY = points[1];
        for (int i = 2; i < points.length; i += 2) {
            double px = points[i];
            double py = points[i + 1];
            if (px < minX) minX = px;
            if (px > maxX) maxX = px;
            if (py < minY) minY = py;
            if (py > maxY) maxY = py;
        }
        out.set(minX, minY, maxX, maxY);
        return out;
    }

    /**
     * Euclidean distance from {@code (px, py)} to the nearest point on
     * the segment {@code (x0, y0) → (x1, y1)}.
     *
     * @param px test x
     * @param py test y
     * @param x0 segment start x
     * @param y0 segment start y
     * @param x1 segment end x
     * @param y1 segment end y
     * @return the distance from the point to the segment
     */
    protected static double pointToSegment(double px, double py,
                                           double x0, double y0,
                                           double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : ((px - x0) * dx + (py - y0) * dy) / len2;
        t = Math.max(0, Math.min(1, t));
        double cx = x0 + t * dx;
        double cy = y0 + t * dy;
        return Math.hypot(px - cx, py - cy);
    }
}
