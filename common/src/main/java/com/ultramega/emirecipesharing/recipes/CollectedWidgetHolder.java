package com.ultramega.emirecipesharing.recipes;

import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;

public final class CollectedWidgetHolder implements WidgetHolder {
    private final int width;
    private final int height;
    private final List<Widget> widgets = new ArrayList<>();

    public CollectedWidgetHolder(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public <T extends Widget> T add(final T widget) {
        this.widgets.add(widget);
        return widget;
    }

    public List<Widget> widgets() {
        return this.widgets;
    }
}
