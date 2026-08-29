package com.hybridcanvas.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Root of the sealed model hierarchy. Every setter bumps {@link #getVersion()} so the
 * view's dirty-check triggers a redraw on the next render pass.
 */
public sealed abstract class HybridCanvasElement permits HybridCanvasShape, HybridCanvasGroup {

    private final UUID id;
    private double rotate;
    private double scaleX;
    private double scaleY;
    private double translateX;
    private double translateY;
    private boolean visible;
    private boolean locked;
    private double opacity;
    private final Map<String, Object> metadata;
    private UUID parentId;
    private volatile long version;

    protected HybridCanvasElement() {
        this.id = UUID.randomUUID();
        this.scaleX = 1.0;
        this.scaleY = 1.0;
        this.visible = true;
        this.locked = false;
        this.opacity = 1.0;
        this.metadata = new HashMap<>();
    }

    public UUID getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public double getRotate() {
        return rotate;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isLocked() {
        return locked;
    }

    public double getOpacity() {
        return opacity;
    }

    /**
     * Returns the mutable metadata map. Mutations bypass {@link #getVersion()} — the
     * caller is responsible for triggering a redraw if needed.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public UUID getParentId() {
        return parentId;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setRotate(double rotate) {
        this.rotate = rotate;
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setTranslateX(double translateX) {
        this.translateX = translateX;
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setTranslateY(double translateY) {
        this.translateY = translateY;
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setVisible(boolean visible) {
        this.visible = visible;
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setLocked(boolean locked) {
        this.locked = locked;
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. Value is clamped to 0..1. */
    public void setOpacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setParentId(UUID parentId) {
        this.parentId = parentId;
        version++;
    }
}
