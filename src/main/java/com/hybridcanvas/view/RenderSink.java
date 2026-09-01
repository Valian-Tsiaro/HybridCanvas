package com.hybridcanvas.view;

/**
 * Callback for each visible, culled leaf emitted by {@link RenderTraversal}.
 * A test implementation typically collects items into a list.
 */
public interface RenderSink {

    /** Called for each visible leaf that passes the viewport cull test. */
    void accept(RenderItem item);
}
