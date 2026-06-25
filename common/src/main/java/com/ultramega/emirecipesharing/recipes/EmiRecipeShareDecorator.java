package com.ultramega.emirecipesharing.recipes;

import com.ultramega.emirecipesharing.Constants;
import com.ultramega.emirecipesharing.network.ShareRecipePacket;
import com.ultramega.emirecipesharing.platform.Services;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EmiRecipeShareDecorator implements EmiRecipeDecorator {
    private static final ResourceLocation TEXTURE = Constants.modLoc("textures/gui/icons/share.png");
    private static final int SIZE = 9;

    @Override
    public void decorateRecipe(final EmiRecipe recipe, final WidgetHolder widgets) {
        final ResourceLocation recipeId = recipe.getId();
        if (recipeId == null) {
            return;
        }

        final int x = Math.max(0, widgets.getWidth() - SIZE - 2);
        final int y = 2;
        widgets.add(new ShareRecipeWidget(recipe, x, y));
        widgets.addTooltipText(List.of(Component.translatable("tooltip.emirecipesharing.share")), x, y, SIZE, SIZE);
    }

    private static final class ShareRecipeWidget extends Widget {
        private final EmiRecipe recipe;
        private final int x;
        private final int y;

        private ShareRecipeWidget(final EmiRecipe recipe, final int x, final int y) {
            this.recipe = recipe;
            this.x = x;
            this.y = y;
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(this.x, this.y, SIZE, SIZE);
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
            graphics.blit(TEXTURE, this.x, this.y, 0, 0, SIZE, SIZE, SIZE, SIZE);
        }

        @Override
        public boolean mouseClicked(final int mouseX, final int mouseY, final int button) {
            final ResourceLocation recipeId = this.recipe.getId();
            if (button != 0 || recipeId == null || !this.getBounds().contains(mouseX, mouseY)) {
                return false;
            }
            final var packet = new ShareRecipePacket(this.recipe.getCategory().getId(), recipeId, ShareRecipePacket.getSharerName(Minecraft.getInstance().player));
            Services.PLATFORM.sendPacketToServer(packet);
            return true;
        }
    }
}
