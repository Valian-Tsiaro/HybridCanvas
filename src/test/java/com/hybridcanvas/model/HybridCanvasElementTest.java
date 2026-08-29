package com.hybridcanvas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HybridCanvasElementTest {

    @Test
    void defaultVisibleTrue() {
        HybridCanvasElement e = new HybridCanvasGroup();
        assertTrue(e.isVisible());
    }

    @Test
    void defaultLockedFalse() {
        HybridCanvasElement e = new HybridCanvasGroup();
        assertFalse(e.isLocked());
    }

    @Test
    void defaultOpacityOne() {
        HybridCanvasElement e = new HybridCanvasGroup();
        assertEquals(1.0, e.getOpacity(), 0.0);
    }

    @Test
    void defaultScaleXYOne() {
        HybridCanvasElement e = new HybridCanvasGroup();
        assertEquals(1.0, e.getScaleX(), 0.0);
        assertEquals(1.0, e.getScaleY(), 0.0);
    }

    @Test
    void defaultTranslateAndRotateZero() {
        HybridCanvasElement e = new HybridCanvasGroup();
        assertEquals(0.0, e.getRotate(), 0.0);
        assertEquals(0.0, e.getTranslateX(), 0.0);
        assertEquals(0.0, e.getTranslateY(), 0.0);
    }

    @Test
    void defaultVersionZero() {
        HybridCanvasElement e = new HybridCanvasGroup();
        assertEquals(0L, e.getVersion());
    }

    @Test
    void idNonNull() {
        HybridCanvasElement e = new HybridCanvasGroup();
        assertNotNull(e.getId());
    }

    @Test
    void idUniqueAcrossInstances() {
        HybridCanvasElement a = new HybridCanvasGroup();
        HybridCanvasElement b = new HybridCanvasGroup();
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void setRotateBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setRotate(0.5);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setScaleXBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setScaleX(2.0);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setScaleYBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setScaleY(2.0);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setTranslateXBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setTranslateX(10.0);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setTranslateYBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setTranslateY(10.0);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setVisibleBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setVisible(false);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setLockedBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setLocked(true);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setOpacityBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setOpacity(0.5);
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setParentIdBumpsVersionExactlyOnce() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setParentId(java.util.UUID.randomUUID());
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void setOpacityClampsToValidRange() {
        HybridCanvasElement e = new HybridCanvasGroup();
        e.setOpacity(1.5);
        assertEquals(1.0, e.getOpacity(), 0.0);
        e.setOpacity(-0.5);
        assertEquals(0.0, e.getOpacity(), 0.0);
    }

    @Test
    void settingSameValueStillBumpsVersion() {
        HybridCanvasElement e = new HybridCanvasGroup();
        long before = e.getVersion();
        e.setRotate(0.0); // same as default
        assertEquals(before + 1, e.getVersion());
    }

    @Test
    void metadataMutableAndIndependent() {
        HybridCanvasElement a = new HybridCanvasGroup();
        HybridCanvasElement b = new HybridCanvasGroup();
        a.getMetadata().put("key", "value");
        assertEquals("value", a.getMetadata().get("key"));
        assertNull(b.getMetadata().get("key"));
    }
}
