package com.hybridcanvas.view;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * Zoomable, pannable canvas that renders scene objects to a backing Canvas so
 * 20k+ elements stay interactive while only selection chrome lives as real nodes.
 * Drop-in positional replacement for {@link Pane}; scene objects enter via
 * {@code addObject}/{@code removeObject}, never through {@link #getChildren()}.
 */
public class HybridCanvas extends Pane {

    /** Placeholder setup until the render pipeline lands: a centered, mouse-transparent label. Replaced by Canvas + chrome-overlay wiring in later prompts. */
    public HybridCanvas() {
        Label label = new Label("HybridCanvas (coming soon)");
        label.setMouseTransparent(true);
        getChildren().add(label);
        label.layoutXProperty().bind(widthProperty().subtract(label.widthProperty()).divide(2));
        label.layoutYProperty().bind(heightProperty().subtract(label.heightProperty()).divide(2));
    }
}
