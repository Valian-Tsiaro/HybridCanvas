package com.hybridcanvas.view;

import com.hybridcanvas.geom.Bounds2D;
import com.hybridcanvas.geom.Viewport;
import com.hybridcanvas.model.HybridCanvasBezier;
import com.hybridcanvas.model.HybridCanvasColor;
import com.hybridcanvas.model.HybridCanvasEllipse;
import com.hybridcanvas.model.HybridCanvasImageInShape;
import com.hybridcanvas.model.HybridCanvasImageRect;
import com.hybridcanvas.model.HybridCanvasImageWithOverlay;
import com.hybridcanvas.model.HybridCanvasPolygon;
import com.hybridcanvas.model.HybridCanvasPolyline;
import com.hybridcanvas.model.HybridCanvasRectangle;
import com.hybridcanvas.store.ModelStore;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShapeRendererTest extends ApplicationTest {

    private Canvas canvas;
    private GraphicsContext gc;
    private ShapeRenderer renderer;
    private RenderTraversal traversal;
    private final Bounds2D worldView = new Bounds2D(-10_000, -10_000, 10_000, 10_000);
    private final Viewport viewport = new Viewport();

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(200, 200);
        gc = canvas.getGraphicsContext2D();
        renderer = new ShapeRenderer();
        traversal = new RenderTraversal();
    }

    private List<RenderItem> walk(ModelStore store) {
        List<RenderItem> items = new ArrayList<>();
        traversal.walk(store, viewport, worldView, items::add);
        return items;
    }

    // ── non-image smoke ───────────────────────────────────────────────

    @Test
    void drawsRectangleWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasRectangle(10, 10, 50, 50));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.draw(gc, items.get(0)));
    }

    @Test
    void drawsPolygonWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasPolygon(new double[]{0, 0, 50, 0, 25, 50}));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.draw(gc, items.get(0)));
    }

    @Test
    void drawsPolylineWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasPolyline(new double[]{0, 0, 50, 0, 25, 50}));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.draw(gc, items.get(0)));
    }

    @Test
    void drawsEllipseWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasEllipse(50, 50, 25, 20));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.draw(gc, items.get(0)));
    }

    @Test
    void drawsBezierCubicWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasBezier(new double[]{0, 0, 20, 30, 40, 30, 60, 0}));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.draw(gc, items.get(0)));
    }

    @Test
    void drawsBezierQuadraticWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasBezier(new double[]{0, 0, 30, 50, 60, 0}));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.draw(gc, items.get(0)));
    }

    // ── pixel assertion ───────────────────────────────────────────────

    @Test
    void filledRectangleProducesRedPixel() {
        HybridCanvasRectangle rect = new HybridCanvasRectangle(20, 20, 80, 80);
        rect.setFill(HybridCanvasColor.fromRgb(255, 0, 0));
        rect.setStroke(HybridCanvasColor.TRANSPARENT);
        ModelStore store = new ModelStore();
        store.addObject(rect);

        List<RenderItem> items = walk(store);
        renderer.draw(gc, items.get(0));

        // snapshot() requires FX thread — use interact() from TestFX
        WritableImage[] holder = new WritableImage[1];
        interact(() -> holder[0] = canvas.snapshot(new SnapshotParameters(), new WritableImage(200, 200)));
        Color pixel = holder[0].getPixelReader().getColor(50, 50);
        assertEquals(1.0, pixel.getRed(), 0.05);
        assertEquals(0.0, pixel.getGreen(), 0.05);
        assertEquals(0.0, pixel.getBlue(), 0.05);
    }

    // ── image placeholder smoke ───────────────────────────────────────

    @Test
    void imageRectPlaceholderDrawsWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasImageRect("missing.png", 10, 10, 50, 50));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.drawImagePlaceholder(gc, items.get(0)));
    }

    @Test
    void imageInShapePlaceholderDrawsWithoutThrowing() {
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasImageInShape("missing.png", new double[]{0, 0, 50, 0, 25, 50}));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.drawImagePlaceholder(gc, items.get(0)));
    }

    @Test
    void imageWithOverlayPlaceholderDrawsWithoutThrowing() {
        HybridCanvasRectangle overlay = new HybridCanvasRectangle(20, 20, 30, 30);
        ModelStore store = new ModelStore();
        store.addObject(new HybridCanvasImageWithOverlay("missing.png", 0, 0, 50, 50, overlay));
        List<RenderItem> items = walk(store);
        assertEquals(1, items.size());
        assertDoesNotThrow(() -> renderer.drawImagePlaceholder(gc, items.get(0)));
    }
}
