package com.hybridcanvas.geom;

/**
 * Zoom and pan state mapping between world and screen coordinates.
 * Pure double math, no {@code javafx.graphics}. Scalar accessors to avoid per-call allocation.
 */
public final class Viewport {

    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;
    private double minZoom = -1.0;
    private double maxZoom = -1.0;

    /** Returns the current zoom factor. @return current zoom factor */
    public double getZoom() {
        return zoom;
    }

    /** @param zoom new zoom factor (clamped to limits if set) */
    public void setZoom(double zoom) {
        this.zoom = applyZoomLimits(zoom);
    }

    /** Returns the horizontal pan offset. @return horizontal pan offset in screen pixels */
    public double getPanX() {
        return panX;
    }

    /** Returns the vertical pan offset. @return vertical pan offset in screen pixels */
    public double getPanY() {
        return panY;
    }

    /**
     * @param panX horizontal pan offset in screen pixels
     * @param panY vertical pan offset in screen pixels
     */
    public void setPan(double panX, double panY) {
        this.panX = panX;
        this.panY = panY;
    }

    /**
     * Sets minimum and maximum zoom. Pass {@code -1} for {@code min} or {@code max} to leave
     * that axis unbounded. Also re-clamps the current zoom to the new limits.
     *
     * @param min minimum zoom factor, or {@code -1} for unbounded
     * @param max maximum zoom factor, or {@code -1} for unbounded
     */
    public void setZoomLimits(double min, double max) {
        this.minZoom = min;
        this.maxZoom = max;
        this.zoom = applyZoomLimits(this.zoom);
    }

    /**
     * @param wx world x
     * @param wy world y
     * @return screen x: {@code wx * zoom + panX}
     */
    public double worldToScreenX(double wx, double wy) {
        return wx * zoom + panX;
    }

    /**
     * @param wx world x
     * @param wy world y
     * @return screen y: {@code wy * zoom + panY}
     */
    public double worldToScreenY(double wx, double wy) {
        return wy * zoom + panY;
    }

    /**
     * @param sx screen x
     * @param sy screen y
     * @return world x: {@code (sx - panX) / zoom}
     */
    public double screenToWorldX(double sx, double sy) {
        return (sx - panX) / zoom;
    }

    /**
     * @param sx screen x
     * @param sy screen y
     * @return world y: {@code (sy - panY) / zoom}
     */
    public double screenToWorldY(double sx, double sy) {
        return (sy - panY) / zoom;
    }

    /**
     * Zooms by {@code factor} anchored at screen ({@code sx},{@code sy}), keeping the
     * world point under it fixed. Respects zoom limits.
     *
     * @param sx     screen x of the anchor point
     * @param sy     screen y of the anchor point
     * @param factor zoom multiplier
     */
    public void zoomAt(double sx, double sy, double factor) {
        double ns = applyZoomLimits(zoom * factor);
        double applied = ns / zoom;
        panX = sx - (sx - panX) * applied;
        panY = sy - (sy - panY) * applied;
        zoom = ns;
    }

    /** Re-clamps the current zoom to the configured limits. */
    public void clampZoom() {
        this.zoom = applyZoomLimits(this.zoom);
    }

    private double applyZoomLimits(double z) {
        if (minZoom >= 0 && z < minZoom) return minZoom;
        if (maxZoom >= 0 && z > maxZoom) return maxZoom;
        return z;
    }
}
