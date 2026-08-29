package com.hybridcanvas.demo;

import com.hybridcanvas.view.HybridCanvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.Random;

/**
 * Hand-driven harness comparing plain {@code Pane} rendering against
 * {@link HybridCanvas} under zoom, pan, and bulk shape/image generation.
 * Scratch UI for eyeballing performance; not part of the library surface.
 */
public class DemoApp extends Application {

    // ponytail: fixed range, upgrade to configurable zoom bounds per SPEC §5
    private static final double MIN_ZOOM = 0.05;
    private static final double MAX_ZOOM = 50.0;

    private final Random rnd = new Random();
    private BorderPane root;
    private Pane paneMode;
    private Pane paneContent;
    private final Scale zoom = new Scale(1, 1);
    private final Translate pan = new Translate(0, 0);
    private HybridCanvas hybridMode;
    private boolean hybrid;

    private double dragStartX, dragStartY, dragStartPanX, dragStartPanY;

    /** Builds the toolbar and both render modes, then wires zoom and pan gestures. */
    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setTop(buildToolbar());

        paneMode = new Pane();
        paneMode.setId("canvas-center");
        clipToBounds(paneMode);

        paneContent = new Pane();
        paneContent.getTransforms().addAll(zoom, pan);
        paneMode.getChildren().add(paneContent);

        hybridMode = new HybridCanvas();
        hybridMode.setId("canvas-center");
        clipToBounds(hybridMode);

        paneMode.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
            zoomAt(e.getX(), e.getY(), factor);
        });

        paneMode.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                dragStartX = e.getX();
                dragStartY = e.getY();
                dragStartPanX = pan.getX();
                dragStartPanY = pan.getY();
            }
        });

        paneMode.setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                pan.setX(dragStartPanX + (e.getX() - dragStartX));
                pan.setY(dragStartPanY + (e.getY() - dragStartY));
            }
        });

        root.setCenter(paneMode);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("HybridCanvas Demo");
        stage.show();
    }

    /**
     * Scales by {@code factor} around the screen point ({@code mx}, {@code my}) so
     * the world point under it stays pinned. Clamps to the zoom bounds and compensates
     * the pan offset accordingly.
     */
    void zoomAt(double mx, double my, double factor) {
        double s = zoom.getX();
        double ns = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, s * factor));
        double applied = ns / s;
        pan.setX(mx - (mx - pan.getX()) * applied);
        pan.setY(my - (my - pan.getY()) * applied);
        zoom.setX(ns);
        zoom.setY(ns);
    }

    /** Current zoom factor, exposed for TestFX assertions. */
    double getZoom() {
        return zoom.getX();
    }

    /** Horizontal pan offset, exposed for TestFX assertions. */
    double getPanX() {
        return pan.getX();
    }

    /** Vertical pan offset, exposed for TestFX assertions. */
    double getPanY() {
        return pan.getY();
    }

    /** Assembles the add/generate buttons, count field, and mode toggle. */
    private HBox buildToolbar() {
        Button addShape = new Button("Add Shape");
        addShape.setOnAction(e -> addRandomShape());

        Button addImage = new Button("Add Image");
        addImage.setOnAction(e -> addRandomImage());

        TextField countField = new TextField("100");
        countField.setId("count");
        countField.setPrefWidth(60);

        Button generate = new Button("Generate N");
        generate.setOnAction(e -> generateN(countField));

        RadioButton paneRadio = new RadioButton("Pane");
        paneRadio.setSelected(true);
        paneRadio.setToggleGroup(new ToggleGroup());

        RadioButton hybridRadio = new RadioButton("Hybrid");
        hybridRadio.setToggleGroup(paneRadio.getToggleGroup());

        paneRadio.getToggleGroup().selectedToggleProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                hybrid = sel == hybridRadio;
                root.setCenter(hybrid ? hybridMode : paneMode);
            }
        });

        HBox toolbar = new HBox(8, addShape, addImage, countField, generate, paneRadio, hybridRadio);
        toolbar.setStyle("-fx-padding: 8; -fx-background-color: #f0f0f0;");
        return toolbar;
    }

    /** Adds n random shapes; silently ignores non-numeric input. */
    private void generateN(TextField countField) {
        int n;
        try {
            n = Integer.parseInt(countField.getText().trim());
        } catch (NumberFormatException e) {
            return;
        }
        for (int i = 0; i < n; i++) {
            addRandomShape();
        }
    }

    /** Adds one randomly chosen shape (rect, ellipse, or polygon). */
    private void addRandomShape() {
        Pane target = currentTarget();
        if (target == null) return; // ponytail: hybrid stub — shapes wired at prompt 10
        switch (rnd.nextInt(3)) {
            case 0 -> target.getChildren().add(randomRect());
            case 1 -> target.getChildren().add(randomEllipse());
            default -> target.getChildren().add(randomPolygon());
        }
    }

    /** Adds a small randomly generated image. */
    private void addRandomImage() {
        Pane target = currentTarget();
        if (target != null) {
            target.getChildren().add(randomImageView());
        }
    }

    /** Content target for new objects; null in hybrid mode until wiring lands. */
    private Pane currentTarget() {
        return hybrid ? null : paneContent;
    }

    /** Clips the pane to its own bounds so panned content never bleeds outside. */
    private static void clipToBounds(Pane pane) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        pane.setClip(clip);
    }

    private Rectangle randomRect() {
        double x = rnd.nextDouble() * 800;
        double y = rnd.nextDouble() * 600;
        double w = 10 + rnd.nextDouble() * 90;
        double h = 10 + rnd.nextDouble() * 90;
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(Color.color(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 0.5 + rnd.nextDouble() * 0.5));
        return r;
    }

    private Ellipse randomEllipse() {
        double cx = rnd.nextDouble() * 900;
        double cy = rnd.nextDouble() * 650;
        double rx = 5 + rnd.nextDouble() * 50;
        double ry = 5 + rnd.nextDouble() * 50;
        Ellipse e = new Ellipse(cx, cy, rx, ry);
        e.setFill(Color.color(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 0.5 + rnd.nextDouble() * 0.5));
        return e;
    }

    private Polygon randomPolygon() {
        double cx = rnd.nextDouble() * 900;
        double cy = rnd.nextDouble() * 650;
        double size = 10 + rnd.nextDouble() * 40;
        int sides = 3 + rnd.nextInt(4);
        Polygon p = new Polygon();
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            p.getPoints().addAll(cx + size * Math.cos(angle), cy + size * Math.sin(angle));
        }
        p.setFill(Color.color(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 0.5 + rnd.nextDouble() * 0.5));
        return p;
    }

    private ImageView randomImageView() {
        int w = 8 + rnd.nextInt(16);
        int h = 8 + rnd.nextInt(16);
        WritableImage img = new WritableImage(w, h);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                img.getPixelWriter().setColor(x, y, Color.color(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()));
            }
        }
        ImageView iv = new ImageView(img);
        iv.setX(rnd.nextDouble() * 900);
        iv.setY(rnd.nextDouble() * 650);
        return iv;
    }
}
