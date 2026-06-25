package com.ultramega.emirecipesharing.recipes;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

public record RecipePreview(EmiRecipe recipe, int width, int height) {
    private static final int EMI_PADDING = 4;

    public RecipePreview(final EmiRecipe recipe) {
        this(recipe, recipe.getDisplayWidth() + EMI_PADDING * 2, recipe.getDisplayHeight() + EMI_PADDING * 2);
    }

    public Rect2i getRectWithBorder(final int x, final int y) {
        return new Rect2i(x, y, this.width, this.height);
    }

    public void render(final GuiGraphics graphics, final int x, final int y, final int mouseX, final int mouseY) {
        final EmiDrawContext context = EmiDrawContext.wrap(graphics);

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);

        EmiRenderHelper.renderRecipe(this.recipe, context, 0, 0, false, -1);

        final HoveredSlot hoveredSlot = this.getHoveredSlot(x, y, mouseX, mouseY);
        if (hoveredSlot != null) {
            final Bounds bounds = hoveredSlot.bounds();

            EmiRenderHelper.drawSlotHightlight(
                context,
                bounds.x() + EMI_PADDING + 1,
                bounds.y() + EMI_PADDING + 1,
                bounds.width() - 2,
                bounds.height() - 2,
                200
            );
        }

        graphics.pose().popPose();
    }

    public EmiStackInteraction getStackAt(final int recipeX, final int recipeY, final int localMouseX, final int localMouseY) {
        final int widgetMouseX = localMouseX - recipeX - EMI_PADDING;
        final int widgetMouseY = localMouseY - recipeY - EMI_PADDING;

        for (final Widget widget : this.collectWidgets()) {
            if (!(widget instanceof SlotWidget slot)) {
                continue;
            }

            if (!slot.getBounds().contains(widgetMouseX, widgetMouseY)) {
                continue;
            }

            if (slot.getStack().isEmpty()) {
                return EmiStackInteraction.EMPTY;
            }

            return new EmiStackInteraction(slot.getStack(), slot.getRecipe() != null ? slot.getRecipe() : this.recipe, true);
        }

        return EmiStackInteraction.EMPTY;
    }

    private List<Widget> collectWidgets() {
        final List<Widget> widgets = new ArrayList<>();

        this.recipe.addWidgets(new WidgetHolder() {
            @Override
            public int getWidth() {
                return RecipePreview.this.recipe.getDisplayWidth();
            }

            @Override
            public int getHeight() {
                return RecipePreview.this.recipe.getDisplayHeight();
            }

            @Override
            public <T extends Widget> T add(final T widget) {
                widgets.add(widget);
                return widget;
            }
        });

        return widgets;
    }

    @Nullable
    private HoveredSlot getHoveredSlot(final int recipeX, final int recipeY, final int mouseX, final int mouseY) {
        final int widgetMouseX = mouseX - recipeX - EMI_PADDING;
        final int widgetMouseY = mouseY - recipeY - EMI_PADDING;

        for (final Widget widget : this.collectWidgets()) {
            if (!(widget instanceof SlotWidget slot)) {
                continue;
            }

            final Bounds bounds = slot.getBounds();
            if (bounds.contains(widgetMouseX, widgetMouseY)) {
                return new HoveredSlot(slot, bounds);
            }
        }

        return null;
    }

    private record HoveredSlot(SlotWidget slot, Bounds bounds) {
    }
}
