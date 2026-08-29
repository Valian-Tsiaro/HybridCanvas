package com.hybridcanvas.demo;

import com.hybridcanvas.view.HybridCanvas;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.api.FxAssert.verifyThat;

class DemoAppTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        new DemoApp().start(stage);
    }

    @Test
    void toolbarHasAddShapeButton() {
        verifyThat("Add Shape", isVisible());
    }

    @Test
    void toolbarHasAddImageButton() {
        verifyThat("Add Image", isVisible());
    }

    @Test
    void toolbarHasCountField() {
        verifyThat("#count", isVisible());
    }

    @Test
    void toolbarHasGenerateButton() {
        verifyThat("Generate N", isVisible());
    }

    @Test
    void toolbarHasModeRadioButtons() {
        verifyThat("Pane", isVisible());
        verifyThat("Hybrid", isVisible());
    }

    @Test
    void hybridModeShowsHybridCanvas() {
        clickOn("Hybrid");
        Node center = lookup("#canvas-center").query();
        assertEquals(HybridCanvas.class, center.getClass());
    }

    @Test
    void paneModeShowsPlainPane() {
        clickOn("Hybrid");
        clickOn("Pane");
        Node center = lookup("#canvas-center").query();
        assertEquals(Pane.class, center.getClass());
    }

    @Test
    void zoomAtKeepsWorldPointUnderCursor() {
        DemoApp app = new DemoApp();
        app.zoomAt(100, 50, 2.0);
        assertEquals(2.0, app.getZoom(), 1e-9);
        assertEquals(-100.0, app.getPanX(), 1e-9);
        assertEquals(-50.0, app.getPanY(), 1e-9);
    }
}
