package com.ultramega.emirecipesharing.recipes;

import com.ultramega.emirecipesharing.Constants;
import com.ultramega.emirecipesharing.network.ShareRecipePacket;
import com.ultramega.emirecipesharing.platform.Services;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.widget.RecipeButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ShareRecipeWidget extends RecipeButtonWidget {
    private static final ResourceLocation TEXTURE = Constants.modLoc("textures/gui/icons/share.png");

    public ShareRecipeWidget(final EmiRecipe recipe, final int x, final int y) {
        super(x, y, 0, 0, recipe);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        final EmiDrawContext context = EmiDrawContext.wrap(graphics);
        context.resetColor();
        context.drawTexture(TEXTURE, this.x, this.y, 12, 12, 0, this.getTextureOffset(mouseX, mouseY), 12, 12, 12, 24);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(final int mouseX, final int mouseY) {
        return List.of(ClientTooltipComponent.create(Component.translatable("tooltip.emirecipesharing.share").getVisualOrderText()));
    }

    @Override
    public boolean mouseClicked(final int mouseX, final int mouseY, final int button) {
        final ResourceLocation recipeId = this.recipe.getId();
        if (button != 0 || recipeId == null || !this.getBounds().contains(mouseX, mouseY)) {
            return false;
        }
        this.playButtonSound();
        final var packet = new ShareRecipePacket(this.recipe.getCategory().getId(), recipeId, ShareRecipePacket.getSharerName(Minecraft.getInstance().player));
        Services.PLATFORM.sendPacketToServer(packet);
        return true;
    }
}
