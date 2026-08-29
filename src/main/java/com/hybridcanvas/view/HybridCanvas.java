package com.hybridcanvas.view;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class HybridCanvas extends Pane {

    public HybridCanvas() {
        Label label = new Label("HybridCanvas (coming soon)");
        label.setMouseTransparent(true);
        getChildren().add(label);
        label.layoutXProperty().bind(widthProperty().subtract(label.widthProperty()).divide(2));
        label.layoutYProperty().bind(heightProperty().subtract(label.heightProperty()).divide(2));
    }
}
