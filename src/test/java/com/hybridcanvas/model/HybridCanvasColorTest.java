package com.hybridcanvas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HybridCanvasColorTest {

    @Test
    void fromArgbToArgbRoundTrip() {
        int argb = 0xFF7F2C00;
        assertEquals(argb, HybridCanvasColor.fromArgb(argb).toArgb());
    }

    @Test
    void fromArgbComponentsRoundTrip() {
        HybridCanvasColor c = HybridCanvasColor.fromArgb(128, 10, 20, 30);
        assertEquals(128, c.getAlpha());
        assertEquals(10, c.getRed());
        assertEquals(20, c.getGreen());
        assertEquals(30, c.getBlue());
    }

    @Test
    void fromRgbSetsAlphaTo255() {
        HybridCanvasColor c = HybridCanvasColor.fromRgb(1, 2, 3);
        assertEquals(255, c.getAlpha());
        assertEquals(1, c.getRed());
        assertEquals(2, c.getGreen());
        assertEquals(3, c.getBlue());
    }

    @Test
    void fromHexRRGGBBIsOpaque() {
        HybridCanvasColor c = HybridCanvasColor.fromHex("#FF8040");
        assertEquals(255, c.getAlpha());
        assertEquals(0xFF, c.getRed());
        assertEquals(0x80, c.getGreen());
        assertEquals(0x40, c.getBlue());
    }

    @Test
    void fromHexAARRGGBB() {
        HybridCanvasColor c = HybridCanvasColor.fromHex("#80FF8040");
        assertEquals(0x80, c.getAlpha());
        assertEquals(0xFF, c.getRed());
        assertEquals(0x80, c.getGreen());
        assertEquals(0x40, c.getBlue());
    }

    @Test
    void transparentHasZeroArgb() {
        assertEquals(0, HybridCanvasColor.TRANSPARENT.toArgb());
    }

    @Test
    void equalsAndHashCode() {
        HybridCanvasColor a = HybridCanvasColor.fromArgb(0xAABBCCDD);
        HybridCanvasColor b = HybridCanvasColor.fromArgb(0xAABBCCDD);
        HybridCanvasColor d = HybridCanvasColor.fromArgb(0x11223344);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, d);
        assertNotEquals(a, null);
    }

    @Test
    void toStringRoundTripsViaFromHex() {
        HybridCanvasColor c = HybridCanvasColor.fromArgb(0xAABBCCDD);
        assertEquals(c.toArgb(), HybridCanvasColor.fromHex(c.toString()).toArgb());
    }

    @Test
    void fromArgbMasksOutOfRangeComponents() {
        HybridCanvasColor c = HybridCanvasColor.fromArgb(0, 300, 10, 20);
        assertEquals(300 & 0xFF, c.getRed());
    }

    @Test
    void fromHexRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> HybridCanvasColor.fromHex("#F"));
        assertThrows(IllegalArgumentException.class, () -> HybridCanvasColor.fromHex("#1234567890"));
    }
}
