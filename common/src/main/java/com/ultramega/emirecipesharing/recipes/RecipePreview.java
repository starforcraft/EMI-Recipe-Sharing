package com.ultramega.emirecipesharing.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Widget;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

public record RecipePreview(EmiRecipe recipe, List<Widget> widgets, int width, int height) {
    public static RecipePreview create(final EmiRecipe recipe) {
        final var holder = new CollectedWidgetHolder(recipe.getDisplayWidth(), recipe.getDisplayHeight());
        recipe.addWidgets(holder);
        return new RecipePreview(recipe, List.copyOf(holder.widgets()), holder.getWidth(), holder.getHeight());
    }

    public Rect2i getRectWithBorder(final int x, final int y) {
        return new Rect2i(x - 4, y - 4, this.width + 8, this.height + 8);
    }

    public void render(final GuiGraphics graphics, final int x, final int y, final int mouseX, final int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        for (final Widget widget : this.widgets) {
            widget.render(graphics, mouseX - x, mouseY - y, 0.0F);
        }
        graphics.pose().popPose();
    }
}
