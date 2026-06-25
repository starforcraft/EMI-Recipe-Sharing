package com.ultramega.emirecipesharing.mixin;

import com.ultramega.emirecipesharing.recipes.ShareRecipeWidget;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.screen.RecipeDisplay;
import dev.emi.emi.screen.WidgetGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeDisplay.class)
public class RecipeDisplayMixin {
    @Unique
    private static final int BUTTON_SIZE = 14;

    @Shadow(remap = false)
    @Final
    public EmiRecipe recipe;

    @Shadow(remap = false)
    @Final
    private int height;

    @Shadow(remap = false)
    private int rows;

    @Shadow(remap = false)
    private int rightWidth;

    @Shadow(remap = false)
    private List<?> rightButtons;

    @Inject(method = "<init>(Ldev/emi/emi/api/recipe/EmiRecipe;)V", at = @At("TAIL"), remap = false)
    private void emirs$init(final EmiRecipe recipe, final CallbackInfo ci) {
        final int existingColumns = (this.rightButtons.size() + this.rows - 1) / this.rows;

        // Reserve one additional right-side column for the share button
        this.rightWidth = Math.max(0, (existingColumns + 1) * BUTTON_SIZE - 1);
    }

    @Inject(method = "addButtons", at = @At("TAIL"), remap = false)
    private void emirs$addButtons(final WidgetGroup widgets, final List<?> types, final int x, final int xOff, final CallbackInfo ci) {
        // Only inject on the right side
        if (xOff != BUTTON_SIZE) {
            return;
        }

        final int buttonCount = types.size();

        final int usedRows = Math.min(this.rows, buttonCount);
        final int space = Math.min(8, this.height + 8 - (usedRows * BUTTON_SIZE - 2));
        final int bottom = this.height + RecipeDisplay.DISPLAY_PADDING / 2 - 12 - space / 2;

        widgets.add(new ShareRecipeWidget(this.recipe, x, bottom));
    }
}
