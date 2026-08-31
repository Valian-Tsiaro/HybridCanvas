package com.hybridcanvas.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Named z-ordered container of elements. The canvas holds a {@code List<HybridCanvasLayer>}
 * with one non-deletable default layer; layers are plain POJOs with no version field.
 */
public final class HybridCanvasLayer {

    private final String id;
    private String name;
    private boolean visible;
    private double opacity;
    private boolean locked;
    private int zOrder;
    private final List<HybridCanvasElement> elements;

    /**
     * @param id   stable identifier (never changes)
     * @param name human-readable display name
     */
    public HybridCanvasLayer(String id, String name) {
        this.id = id;
        this.name = name;
        this.visible = true;
        this.locked = false;
        this.opacity = 1.0;
        this.zOrder = 0;
        this.elements = new ArrayList<>();
    }

    /** @return the layer id (immutable) */
    public String getId() {
        return id;
    }

    /** @return the display name */
    public String getName() {
        return name;
    }

    /** @param name new display name */
    public void setName(String name) {
        this.name = name;
    }

    /** @return whether the layer is visible */
    public boolean isVisible() {
        return visible;
    }

    /** @param visible new visibility flag */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /** @return the layer opacity (0..1) */
    public double getOpacity() {
        return opacity;
    }

    /** @param opacity new opacity, clamped to 0..1 */
    public void setOpacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
    }

    /** @return whether the layer is locked */
    public boolean isLocked() {
        return locked;
    }

    /** @param locked new locked flag */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /** @return the z-order; higher values are drawn later */
    public int getZOrder() {
        return zOrder;
    }

    /** @param zOrder new z-order */
    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    /** @return the mutable elements list */
    public List<HybridCanvasElement> getElements() {
        return elements;
    }
}
