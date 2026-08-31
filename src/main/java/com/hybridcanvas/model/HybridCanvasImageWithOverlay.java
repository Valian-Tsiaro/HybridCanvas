package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import com.hybridcanvas.geom.Transform2D;

/**
 * Image with a separate shape outline overlay. Bounds are the union of the
 * image rect and the overlay shape's local bounds.
 */
public final class HybridCanvasImageWithOverlay extends HybridCanvasImageShape {

    private double x;
    private double y;
    private double w;
    private double h;
    private HybridCanvasShape overlay;
    private final Bounds2D scratch = new Bounds2D();

    /**
     * @param imageRef image reference key
     * @param x x coordinate of the image rectangle origin
     * @param y y coordinate of the image rectangle origin
     * @param w image rect width; may be negative
     * @param h image rect height; may be negative
     * @param overlay the shape providing the overlay outline
     * @throws IllegalArgumentException if {@code overlay} is {@code null}
     */
    public HybridCanvasImageWithOverlay(String imageRef, double x, double y,
                                        double w, double h, HybridCanvasShape overlay) {
        super(imageRef);
        if (overlay == null) throw new IllegalArgumentException("overlay must not be null");
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.overlay = overlay;
    }

    /** @return x coordinate of the image rectangle origin */
    public double getX() { return x; }

    /** @return y coordinate of the image rectangle origin */
    public double getY() { return y; }

    /** @return image rect width (may be negative) */
    public double getWidth() { return w; }

    /** @return image rect height (may be negative) */
    public double getHeight() { return h; }

    /** @return the overlay shape */
    public HybridCanvasShape getOverlay() { return overlay; }

    /** @param x new x */
    public void setX(double x) { this.x = x; bumpVersion(); }

    /** @param y new y */
    public void setY(double y) { this.y = y; bumpVersion(); }

    /** @param w new width */
    public void setWidth(double w) { this.w = w; bumpVersion(); }

    /** @param h new height */
    public void setHeight(double h) { this.h = h; bumpVersion(); }

    /**
     * @param overlay the new overlay shape; must not be {@code null}
     * @throws IllegalArgumentException if {@code overlay} is {@code null}
     */
    public void setOverlay(HybridCanvasShape overlay) {
        if (overlay == null) throw new IllegalArgumentException("overlay must not be null");
        this.overlay = overlay;
        bumpVersion();
    }

    /**
     * {@inheritDoc}
     * Union of the image rect bounds and the overlay shape's local bounds
     * (transformed by the overlay's own local transform).
     */
    @Override
    public Bounds2D getLocalBounds(Bounds2D out) {
        double x1 = Math.min(x, x + w);
        double y1 = Math.min(y, y + h);
        double x2 = Math.max(x, x + w);
        double y2 = Math.max(y, y + h);
        out.set(x1, y1, x2, y2);

        // ponytail: ~5 Transform2D allocs via getLocalTransform();
        // negligible at overlay-level counts but upgradeable to in-place compose
        // if profiling shows overlay bounds are hot.
        overlay.getLocalBounds(scratch);
        overlay.getLocalTransform().transformBounds(scratch, scratch);
        out.union(scratch);
        return out;
    }

    /** {@inheritDoc} True if the point is in the image rect or the overlay shape. */
    @Override
    public boolean containsLocal(double px, double py) {
        double x1 = Math.min(x, x + w);
        double y1 = Math.min(y, y + h);
        double x2 = Math.max(x, x + w);
        double y2 = Math.max(y, y + h);
        if (px >= x1 && px <= x2 && py >= y1 && py <= y2) return true;
        // ponytail: getLocalTransform() + inverse() ≈11 allocs per hit-test;
        // negligible for rare overlays but upgradeable to cached inverse
        // if profiling shows hit-test on overlays is hot.
        Transform2D inv = overlay.getLocalTransform().inverse();
        return overlay.containsLocal(inv.mapX(px, py), inv.mapY(px, py));
    }
}
