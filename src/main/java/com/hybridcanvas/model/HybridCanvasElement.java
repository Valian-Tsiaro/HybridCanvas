package com.hybridcanvas.model;

import com.hybridcanvas.geom.Bounds2D;
import com.hybridcanvas.geom.Transform2D;

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

    /**
     * Fills {@code out} with the intrinsic local-space bounding box.
     * No allocation — the caller owns the {@link Bounds2D} instance.
     *
     * @param out reusable bounds to populate
     * @return {@code out}, for chaining
     */
    public abstract Bounds2D getLocalBounds(Bounds2D out);

    /**
     * Composes this element's separate transform fields into a single
     * {@link Transform2D} in the resolved order {@code translate ∘ rotate ∘ scale}.
     *
     * @return a new composed local transform
     */
    public Transform2D getLocalTransform() {
        return Transform2D.translate(translateX, translateY)
                .concat(Transform2D.rotate(rotate))
                .concat(Transform2D.scale(scaleX, scaleY));
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
    protected final void bumpVersion() {
        version++;
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setRotate(double rotate) {
        this.rotate = rotate;
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setTranslateX(double translateX) {
        this.translateX = translateX;
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setTranslateY(double translateY) {
        this.translateY = translateY;
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setVisible(boolean visible) {
        this.visible = visible;
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setLocked(boolean locked) {
        this.locked = locked;
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. Value is clamped to 0..1. */
    public void setOpacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
        bumpVersion();
    }

    /** Bumps the version so the view's dirty-check re-renders next pass. */
    public void setParentId(UUID parentId) {
        this.parentId = parentId;
        bumpVersion();
    }
}
