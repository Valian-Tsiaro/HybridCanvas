package com.hybridcanvas.view;

import com.hybridcanvas.model.HybridCanvasBezier;
import com.hybridcanvas.model.HybridCanvasColor;
import com.hybridcanvas.model.HybridCanvasEllipse;
import com.hybridcanvas.model.HybridCanvasImageInShape;
import com.hybridcanvas.model.HybridCanvasImageRect;
import com.hybridcanvas.model.HybridCanvasImageShape;
import com.hybridcanvas.model.HybridCanvasImageWithOverlay;
import com.hybridcanvas.model.HybridCanvasPolygon;
import com.hybridcanvas.model.HybridCanvasPolyShape;
import com.hybridcanvas.model.HybridCanvasPolyline;
import com.hybridcanvas.model.HybridCanvasRectangle;
import com.hybridcanvas.model.HybridCanvasShape;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Exhaustive pattern-match renderer for non-image leaf shapes.
 * Pixel-drawing lives ONLY here; the model stays graphics-free.
 *
 * <p>The caller must set the {@link GraphicsContext} transform to world→screen
 * before calling {@link #draw}; {@code strokeWidth} is in world units.</p>
 */
public final class ShapeRenderer {

    // ponytail: per-call Color alloc; add a small LRU keyed by argb when
    // redraw loops hit 20k scale.
    private static final Color PLACEHOLDER_FILL = Color.color(0.7, 0.7, 0.7, 0.6);
    private static final Color PLACEHOLDER_STROKE = Color.color(0.4, 0.4, 0.4, 1.0);

    /**
     * Draws a non-image leaf shape to the given {@link GraphicsContext}.
     *
     * @param gc   the graphics context (must be in world→screen transform)
     * @param item the render item for a non-image leaf
     * @throws IllegalStateException if {@code item.element()} is not a {@link HybridCanvasShape}
     */
    public void draw(GraphicsContext gc, RenderItem item) {
        if (!(item.element() instanceof HybridCanvasShape s)) {
            throw new IllegalStateException(
                    "draw() expects a shape element, got " + item.element().getClass().getSimpleName());
        }

        gc.save();
        gc.setGlobalAlpha(item.effectiveOpacity());
        try {
            switch (s) {
                case HybridCanvasRectangle r -> drawRectangle(gc, r);
                case HybridCanvasEllipse e -> drawEllipse(gc, e);
                case HybridCanvasPolyShape p -> drawPolyShape(gc, p);
                case HybridCanvasImageShape i -> throw new IllegalStateException(
                        "use drawImagePlaceholder() for image shapes: " + i.getClass().getSimpleName());
            }
        } finally {
            gc.restore();
        }
    }

    /**
     * Draws an image shape as a grey placeholder.
     * Real image rendering arrives when {@code ImageStore} is available.
     *
     * @param gc   the graphics context (must be in world→screen transform)
     * @param item the render item for an image shape
     * @throws IllegalStateException if {@code item.element()} is not a {@link HybridCanvasImageShape}
     */
    public void drawImagePlaceholder(GraphicsContext gc, RenderItem item) {
        if (!(item.element() instanceof HybridCanvasImageShape img)) {
            throw new IllegalStateException(
                    "drawImagePlaceholder() expects an image shape, got "
                            + item.element().getClass().getSimpleName());
        }

        gc.save();
        gc.setGlobalAlpha(item.effectiveOpacity());
        gc.setFill(PLACEHOLDER_FILL);
        gc.setStroke(PLACEHOLDER_STROKE);
        gc.setLineWidth(Math.max(img.getStrokeWidth(), 1.0));
        try {
            switch (img) {
                case HybridCanvasImageRect ir -> drawImageRectPlaceholder(gc, ir);
                case HybridCanvasImageInShape iis -> drawImageInShapePlaceholder(gc, iis);
                case HybridCanvasImageWithOverlay iwo -> drawImageWithOverlayPlaceholder(gc, iwo);
            }
        } finally {
            gc.restore();
        }
    }

    // ── non-image shape draws ─────────────────────────────────────────

    private void drawRectangle(GraphicsContext gc, HybridCanvasRectangle r) {
        applyAppearance(gc, r);
        double x = r.getX();
        double y = r.getY();
        double w = r.getWidth();
        double h = r.getHeight();
        if (r.getFill() != null) {
            gc.fillRect(x, y, w, h);
        }
        if (r.getStrokeWidth() > 0) {
            gc.strokeRect(x, y, w, h);
        }
    }

    private void drawEllipse(GraphicsContext gc, HybridCanvasEllipse e) {
        applyAppearance(gc, e);
        double rx = e.getRx();
        double ry = e.getRy();
        double x = e.getCx() - rx;
        double y = e.getCy() - ry;
        double w = 2 * rx;
        double h = 2 * ry;
        if (e.getFill() != null) {
            gc.fillOval(x, y, w, h);
        }
        if (e.getStrokeWidth() > 0) {
            gc.strokeOval(x, y, w, h);
        }
    }

    private void drawPolyShape(GraphicsContext gc, HybridCanvasPolyShape p) {
        applyAppearance(gc, p);
        switch (p) {
            case HybridCanvasPolygon poly -> drawPolygonPath(gc, poly);
            case HybridCanvasPolyline pl -> drawPolylinePath(gc, pl);
            case HybridCanvasBezier bz -> drawBezierPath(gc, bz);
        }
    }

    private void drawPolygonPath(GraphicsContext gc, HybridCanvasPolygon poly) {
        int n = poly.getPointCount();
        if (n < 2) return;

        gc.beginPath();
        gc.moveTo(poly.getX(0), poly.getY(0));
        for (int i = 1; i < n; i++) {
            gc.lineTo(poly.getX(i), poly.getY(i));
        }
        gc.closePath();
        if (poly.getFill() != null) {
            gc.fill();
        }
        if (poly.getStrokeWidth() > 0) {
            gc.stroke();
        }
    }

    private void drawPolylinePath(GraphicsContext gc, HybridCanvasPolyline pl) {
        int n = pl.getPointCount();
        if (n < 2) return;

        gc.beginPath();
        gc.moveTo(pl.getX(0), pl.getY(0));
        for (int i = 1; i < n; i++) {
            gc.lineTo(pl.getX(i), pl.getY(i));
        }
        if (pl.getStrokeWidth() > 0) {
            gc.stroke();
        }
    }

    private void drawBezierPath(GraphicsContext gc, HybridCanvasBezier bz) {
        int n = bz.getPointCount();
        if (n < 2) return;

        gc.beginPath();
        gc.moveTo(bz.getX(0), bz.getY(0));

        boolean cubic = (n == 4);
        if (cubic) {
            gc.bezierCurveTo(
                    bz.getX(1), bz.getY(1),
                    bz.getX(2), bz.getY(2),
                    bz.getX(3), bz.getY(3));
        } else {
            gc.quadraticCurveTo(
                    bz.getX(1), bz.getY(1),
                    bz.getX(2), bz.getY(2));
        }

        if (bz.getFill() != null) {
            gc.fill();
        }
        if (bz.getStrokeWidth() > 0) {
            gc.stroke();
        }
    }

    // ── image placeholder draws ───────────────────────────────────────

    private void drawImageRectPlaceholder(GraphicsContext gc, HybridCanvasImageRect ir) {
        gc.fillRect(ir.getX(), ir.getY(), ir.getWidth(), ir.getHeight());
        gc.strokeRect(ir.getX(), ir.getY(), ir.getWidth(), ir.getHeight());
    }

    private void drawImageInShapePlaceholder(GraphicsContext gc, HybridCanvasImageInShape iis) {
        // Grey fill over bounding box
        double minX = iis.getX(0), minY = iis.getY(0);
        double maxX = minX, maxY = minY;
        for (int i = 1; i < iis.getPointCount(); i++) {
            double px = iis.getX(i);
            double py = iis.getY(i);
            if (px < minX) minX = px;
            if (py < minY) minY = py;
            if (px > maxX) maxX = px;
            if (py > maxY) maxY = py;
        }
        gc.fillRect(minX, minY, maxX - minX, maxY - minY);

        // Stroke the mask polygon outline
        int n = iis.getPointCount();
        gc.beginPath();
        gc.moveTo(iis.getX(0), iis.getY(0));
        for (int i = 1; i < n; i++) {
            gc.lineTo(iis.getX(i), iis.getY(i));
        }
        gc.closePath();
        gc.stroke();
    }

    private void drawImageWithOverlayPlaceholder(GraphicsContext gc, HybridCanvasImageWithOverlay iwo) {
        // Grey fill for the image rect
        gc.fillRect(iwo.getX(), iwo.getY(), iwo.getWidth(), iwo.getHeight());
        gc.strokeRect(iwo.getX(), iwo.getY(), iwo.getWidth(), iwo.getHeight());

        // Draw the overlay outline
        HybridCanvasShape overlay = iwo.getOverlay();
        switch (overlay) {
            case HybridCanvasRectangle r -> {
                gc.strokeRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            }
            case HybridCanvasEllipse e -> {
                gc.strokeOval(e.getCx() - e.getRx(), e.getCy() - e.getRy(),
                        2 * e.getRx(), 2 * e.getRy());
            }
            case HybridCanvasPolygon poly -> {
                int n = poly.getPointCount();
                gc.beginPath();
                gc.moveTo(poly.getX(0), poly.getY(0));
                for (int i = 1; i < n; i++) {
                    gc.lineTo(poly.getX(i), poly.getY(i));
                }
                gc.closePath();
                gc.stroke();
            }
            case HybridCanvasPolyline pl -> {
                int n = pl.getPointCount();
                gc.beginPath();
                gc.moveTo(pl.getX(0), pl.getY(0));
                for (int i = 1; i < n; i++) {
                    gc.lineTo(pl.getX(i), pl.getY(i));
                }
                gc.stroke();
            }
            case HybridCanvasBezier bz -> {
                int n = bz.getPointCount();
                gc.beginPath();
                gc.moveTo(bz.getX(0), bz.getY(0));
                if (n == 4) {
                    gc.bezierCurveTo(bz.getX(1), bz.getY(1),
                            bz.getX(2), bz.getY(2),
                            bz.getX(3), bz.getY(3));
                } else {
                    gc.quadraticCurveTo(bz.getX(1), bz.getY(1),
                            bz.getX(2), bz.getY(2));
                }
                gc.stroke();
            }
            default -> {} // no-op for other shape types
        }
    }

    // ── appearance helpers ────────────────────────────────────────────

    private static void applyAppearance(GraphicsContext gc, HybridCanvasShape s) {
        HybridCanvasColor fill = s.getFill();
        if (fill != null) {
            gc.setFill(toFxColor(fill));
        }
        gc.setStroke(toFxColor(s.getStroke()));
        gc.setLineWidth(s.getStrokeWidth());
    }

    private static Color toFxColor(HybridCanvasColor c) {
        return new Color(
                c.getRed() / 255.0,
                c.getGreen() / 255.0,
                c.getBlue() / 255.0,
                c.getAlpha() / 255.0);
    }
}
