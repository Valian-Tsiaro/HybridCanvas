package com.hybridcanvas;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FxSmokeTest extends ApplicationTest {

    private Stage testStage;

    @Override
    public void start(Stage stage) {
        testStage = stage;
        stage.setScene(new Scene(new Group()));
        stage.show();
    }

    @Test
    void stageShows() {
        assertTrue(testStage.isShowing());
    }
}
