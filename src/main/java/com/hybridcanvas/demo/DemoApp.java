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
import javafx.stage.Stage;

import java.util.Random;

public class DemoApp extends Application {

    private final Random rnd = new Random();
    private BorderPane root;
    private Pane paneMode;
    private HybridCanvas hybridMode;
    private boolean hybrid;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setTop(buildToolbar());

        paneMode = new Pane();
        paneMode.setId("canvas-center");
        hybridMode = new HybridCanvas();
        hybridMode.setId("canvas-center");

        clipToBounds(paneMode);
        clipToBounds(hybridMode);

        root.setCenter(paneMode);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("HybridCanvas Demo");
        stage.show();
    }

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

    private void addRandomShape() {
        Pane target = currentTarget();
        switch (rnd.nextInt(3)) {
            case 0 -> target.getChildren().add(randomRect());
            case 1 -> target.getChildren().add(randomEllipse());
            default -> target.getChildren().add(randomPolygon());
        }
    }

    private void addRandomImage() {
        currentTarget().getChildren().add(randomImageView());
    }

    private Pane currentTarget() {
        return hybrid ? hybridMode : paneMode;
    }

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
