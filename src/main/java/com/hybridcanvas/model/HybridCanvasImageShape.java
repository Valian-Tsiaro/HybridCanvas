package com.hybridcanvas.model;

/**
 * Sealed abstract base for image-bearing shapes. Holds only {@code imageRef}
 * (a pure data key: atlas-key string or unique-image path). No runtime image
 * state here — load state, decoded {@code Image}, and mipmap levels live in
 * the view-side {@code ImageStore}.
 */
public sealed abstract class HybridCanvasImageShape extends HybridCanvasShape
        permits HybridCanvasImageRect, HybridCanvasImageInShape,
                HybridCanvasImageWithOverlay {

    private String imageRef;

    /**
     * @param imageRef the pure data key referencing an image in the store
     */
    protected HybridCanvasImageShape(String imageRef) {
        this.imageRef = imageRef;
    }

    /** @return the image reference key */
    public String getImageRef() {
        return imageRef;
    }

    /** @param imageRef the new image reference key */
    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
        bumpVersion();
    }
}
