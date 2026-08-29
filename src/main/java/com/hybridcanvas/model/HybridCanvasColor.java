package com.hybridcanvas.model;

/**
 * Immutable ARGB color value type for the model layer. Wraps a single {@code int argb}
 * and never touches {@code javafx.graphics}.
 */
public final class HybridCanvasColor {

    public static final HybridCanvasColor TRANSPARENT = new HybridCanvasColor(0x00000000);

    private final int argb;

    private HybridCanvasColor(int argb) {
        this.argb = argb;
    }

    /**
     * @param argb packed ARGB integer
     * @return a color wrapping the given int
     */
    public static HybridCanvasColor fromArgb(int argb) {
        return new HybridCanvasColor(argb);
    }

    /**
     * @param a alpha (0–255), masked to low 8 bits
     * @param r red   (0–255), masked to low 8 bits
     * @param g green (0–255), masked to low 8 bits
     * @param b blue  (0–255), masked to low 8 bits
     * @return a color from the given components
     */
    public static HybridCanvasColor fromArgb(int a, int r, int g, int b) {
        return fromArgb(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    /**
     * @param r red   (0–255), masked to low 8 bits
     * @param g green (0–255), masked to low 8 bits
     * @param b blue  (0–255), masked to low 8 bits
     * @return an opaque color (alpha 255)
     */
    public static HybridCanvasColor fromRgb(int r, int g, int b) {
        return fromArgb(255, r, g, b);
    }

    /**
     * Parses a hex string. Accepts {@code #RRGGBB} (opaque) or {@code #AARRGGBB}.
     * Leading {@code #} is optional.
     *
     * @param hex the hex color string
     * @return the parsed color
     * @throws IllegalArgumentException if the string length is not 6 or 8, or contains non-hex characters
     */
    public static HybridCanvasColor fromHex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() == 6) {
            return fromArgb(255, Integer.parseInt(h.substring(0, 2), 16),
                    Integer.parseInt(h.substring(2, 4), 16),
                    Integer.parseInt(h.substring(4, 6), 16));
        } else if (h.length() == 8) {
            return fromArgb(Integer.parseInt(h.substring(0, 2), 16),
                    Integer.parseInt(h.substring(2, 4), 16),
                    Integer.parseInt(h.substring(4, 6), 16),
                    Integer.parseInt(h.substring(6, 8), 16));
        } else {
            throw new IllegalArgumentException("Hex string must be #RRGGBB or #AARRGGBB: " + hex);
        }
    }

    /** @return packed ARGB integer */
    public int toArgb() {
        return argb;
    }

    public int getAlpha() {
        return (argb >> 24) & 0xFF;
    }

    public int getRed() {
        return (argb >> 16) & 0xFF;
    }

    public int getGreen() {
        return (argb >> 8) & 0xFF;
    }

    public int getBlue() {
        return argb & 0xFF;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof HybridCanvasColor that && argb == that.argb);
    }

    @Override
    public int hashCode() {
        return argb;
    }

    @Override
    public String toString() {
        return String.format("#%08X", argb);
    }
}
