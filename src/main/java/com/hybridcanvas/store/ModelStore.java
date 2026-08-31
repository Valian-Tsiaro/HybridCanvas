package com.hybridcanvas.store;

import com.hybridcanvas.model.HybridCanvasElement;
import com.hybridcanvas.model.HybridCanvasLayer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Model-side container the view observes. Holds layers, auto-creates the
 * non-deletable {@code "default"} layer, and exposes a read-only observable
 * element view in z-order.
 */
public final class ModelStore {

    /** The id of the auto-created, non-deletable default layer. */
    public static final String DEFAULT_LAYER_ID = "default";

    private final List<HybridCanvasLayer> layers;
    private final HybridCanvasLayer defaultLayer;
    private final ObservableList<HybridCanvasElement> elements;
    private final ObservableList<HybridCanvasElement> elementsView;
    // ponytail: single ObservableList mutated on structural ops rather than a
    // composed view — recompute order on reorder if needed.
    // ponytail: IdentityHashMap for O(1) element→layer lookup; switch to
    // WeakIdentityHashMap if the store ever outlives element lifecycles.
    private final Map<HybridCanvasElement, HybridCanvasLayer> owner;

    public ModelStore() {
        this.defaultLayer = new HybridCanvasLayer(DEFAULT_LAYER_ID, "Default");
        this.layers = new ArrayList<>();
        this.layers.add(defaultLayer);
        this.elements = FXCollections.observableArrayList();
        this.elementsView = FXCollections.unmodifiableObservableList(this.elements);
        this.owner = new IdentityHashMap<>();
    }

    /**
     * Returns the layer list in insertion order.
     *
     * @return the mutable layer list
     */
    public List<HybridCanvasLayer> getLayers() {
        return layers;
    }

    /**
     * Returns the non-deletable default layer.
     *
     * @return the non-deletable default layer
     */
    public HybridCanvasLayer getDefaultLayer() {
        return defaultLayer;
    }

    /**
     * Adds {@code layer} to the store.
     *
     * @param layer the layer to add
     * @throws IllegalArgumentException if {@code layer} is null
     */
    public void addLayer(HybridCanvasLayer layer) {
        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null");
        }
        layers.add(layer);
    }

    /**
     * Removes a layer and all its elements from the store. The default layer
     * cannot be removed; doing so throws {@link IllegalArgumentException}.
     *
     * @param layer the layer to remove
     * @throws IllegalArgumentException if {@code layer} is null or is the default layer
     */
    public void removeLayer(HybridCanvasLayer layer) {
        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null");
        }
        if (layer == defaultLayer) {
            throw new IllegalArgumentException("cannot remove the default layer");
        }
        // clear owner entries first so removeAll below leaves a consistent state
        for (HybridCanvasElement e : layer.getElements()) {
            owner.remove(e);
        }
        elements.removeAll(layer.getElements());
        layers.remove(layer);
    }

    /**
     * Returns a read-only observable view across all layers in z-order.
     * Structural mutations ({@link #addObject}, {@link #removeObject},
     * {@link #removeLayer}, {@link #clear}) keep this in sync.
     *
     * @return the observable element list
     */
    public ObservableList<HybridCanvasElement> getElements() {
        return elementsView;
    }

    /**
     * Adds {@code e} to the default layer.
     *
     * @param e the element to add
     */
    public void addObject(HybridCanvasElement e) {
        addObject(defaultLayer, e);
    }

    /**
     * Adds {@code e} to {@code layer}. The element is appended to the layer's
     * local list and inserted into the global view at the position respecting
     * the layer's z-order.
     *
     * @param layer the target layer (must belong to this store)
     * @param e     the element to add
     * @throws IllegalArgumentException if {@code layer} or {@code e} is null,
     *                                  or if {@code layer} is not in this store
     * @throws IllegalStateException    if {@code e} is already in the store
     */
    public void addObject(HybridCanvasLayer layer, HybridCanvasElement e) {
        if (layer == null) {
            throw new IllegalArgumentException("layer must not be null");
        }
        if (!layers.contains(layer)) {
            throw new IllegalArgumentException("layer not in store");
        }
        if (e == null) {
            throw new IllegalArgumentException("element must not be null");
        }
        if (owner.containsKey(e)) {
            throw new IllegalStateException("element already in store");
        }
        layer.getElements().add(e);
        owner.put(e, layer);
        elements.add(findInsertionIndex(layer), e);
    }

    /**
     * Removes {@code e} from every layer and from the global view.
     * No-op if {@code e} is not in the store.
     *
     * @param e the element to remove
     */
    public void removeObject(HybridCanvasElement e) {
        HybridCanvasLayer layer = owner.remove(e);
        if (layer != null) {
            layer.getElements().remove(e);
        }
        elements.remove(e);
    }

    /** Empties all layers' element lists and the global view. Layers are kept. */
    public void clear() {
        for (HybridCanvasLayer l : layers) {
            l.getElements().clear();
        }
        owner.clear();
        elements.clear();
    }

    /**
     * Returns the index at which {@code layer}'s next element should be
     * inserted to maintain z-order (ascending). Insertion is O(n) in the
     * worst case; acceptable at expected scale.
     */
    private int findInsertionIndex(HybridCanvasLayer layer) {
        int targetZ = layer.getZOrder();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HybridCanvasLayer existingLayer = owner.get(elements.get(i));
            if (existingLayer.getZOrder() <= targetZ) {
                return i + 1;
            }
        }
        return 0;
    }
}
