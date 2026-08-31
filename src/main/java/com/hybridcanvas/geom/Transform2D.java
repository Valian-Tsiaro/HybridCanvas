package com.hybridcanvas.geom;

/**
 * Immutable 2D affine transform with 6 coefficients. Pure double math, no {@code javafx.graphics}.
 *
 * <p>Matrix layout: {@code x' = m00*x + m01*y + m02}, {@code y' = m10*x + m11*y + m12}.
 * Compose via {@link #concat(Transform2D)} ({@code this ∘ other}).</p>
 */
public final class Transform2D {

    /** Scales x on the x-axis. */
    public final double m00;

    /** Shears y onto the x-axis. */
    public final double m01;

    /** Translates along the x-axis. */
    public final double m02;

    /** Shears x onto the y-axis. */
    public final double m10;

    /** Scales y on the y-axis. */
    public final double m11;

    /** Translates along the y-axis. */
    public final double m12;

    private Transform2D(double m00, double m01, double m02,
                        double m10, double m11, double m12) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
    }

    /** Returns the identity transform. @return the identity transform */
    public static Transform2D identity() {
        return new Transform2D(1, 0, 0, 0, 1, 0);
    }

    /**
     * @param tx translation along x
     * @param ty translation along y
     * @return a translation transform
     */
    public static Transform2D translate(double tx, double ty) {
        return new Transform2D(1, 0, tx, 0, 1, ty);
    }

    /**
     * Creates a rotation transform. Angle is in radians.
     *
     * @param radians angle in radians
     * @return a rotation transform
     */
    public static Transform2D rotate(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new Transform2D(c, -s, 0, s, c, 0);
    }

    /**
     * @param sx scale factor along x
     * @param sy scale factor along y
     * @return a scale transform
     */
    public static Transform2D scale(double sx, double sy) {
        return new Transform2D(sx, 0, 0, 0, sy, 0);
    }

    /**
     * Returns {@code this ∘ other} — apply {@code other} first, then this.
     *
     * @param other transform to apply before this one
     * @return the composed transform
     */
    public Transform2D concat(Transform2D other) {
        return new Transform2D(
                m00 * other.m00 + m01 * other.m10,
                m00 * other.m01 + m01 * other.m11,
                m00 * other.m02 + m01 * other.m12 + m02,
                m10 * other.m00 + m11 * other.m10,
                m10 * other.m01 + m11 * other.m11,
                m10 * other.m02 + m11 * other.m12 + m12
        );
    }

    /**
     * @param x input x
     * @param y input y
     * @return transformed x: {@code m00*x + m01*y + m02}
     */
    public double mapX(double x, double y) {
        return m00 * x + m01 * y + m02;
    }

    /**
     * @param x input x
     * @param y input y
     * @return transformed y: {@code m10*x + m11*y + m12}
     */
    public double mapY(double x, double y) {
        return m10 * x + m11 * y + m12;
    }

    /**
     * Returns the inverse transform. Undefined (NaN/Inf) if the determinant is zero.
     *
     * @return the inverse transform
     */
    public Transform2D inverse() {
        double det = m00 * m11 - m01 * m10;
        double invDet = 1.0 / det;
        return new Transform2D(
                m11 * invDet,
                -m01 * invDet,
                (m01 * m12 - m11 * m02) * invDet,
                -m10 * invDet,
                m00 * invDet,
                (m10 * m02 - m00 * m12) * invDet
        );
    }

    /**
     * Transforms the 4 corners of {@code in} and writes the AABB of the result into {@code out}.
     * Safe when {@code in == out}.
     *
     * @param in  source bounds
     * @param out destination bounds (may be the same reference as {@code in})
     */
    public void transformBounds(Bounds2D in, Bounds2D out) {
        double x0 = mapX(in.minX, in.minY);
        double y0 = mapY(in.minX, in.minY);
        double x1 = mapX(in.maxX, in.minY);
        double y1 = mapY(in.maxX, in.minY);
        double x2 = mapX(in.maxX, in.maxY);
        double y2 = mapY(in.maxX, in.maxY);
        double x3 = mapX(in.minX, in.maxY);
        double y3 = mapY(in.minX, in.maxY);

        double minX = Math.min(Math.min(x0, x1), Math.min(x2, x3));
        double maxX = Math.max(Math.max(x0, x1), Math.max(x2, x3));
        double minY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        double maxY = Math.max(Math.max(y0, y1), Math.max(y2, y3));

        out.set(minX, minY, maxX, maxY);
    }
}
