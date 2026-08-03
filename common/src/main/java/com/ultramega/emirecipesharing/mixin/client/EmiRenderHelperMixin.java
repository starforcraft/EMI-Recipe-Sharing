package com.ultramega.emirecipesharing.mixin.client;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiDrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// We need to flush the recipes because of immeditatelyfast's hud batching
@Mixin(value = EmiRenderHelper.class, remap = false)
public abstract class EmiRenderHelperMixin {
    @Inject(
        method = "renderRecipe(Ldev/emi/emi/api/recipe/EmiRecipe;Ldev/emi/emi/runtime/EmiDrawContext;IIZI)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/emi/emi/api/widget/Widget;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            shift = At.Shift.AFTER,
            remap = false
        ),
        remap = false,
        require = 1
    )
    private static void emirs$flushAfterEachWidget(final EmiRecipe recipe,
                                                   final EmiDrawContext context,
                                                   final int x,
                                                   final int y,
                                                   final boolean showMissing,
                                                   final int overlayColor,
                                                   final CallbackInfo ci) {
        context.raw().flush();
    }

    @Inject(
        method = "renderRecipe(Ldev/emi/emi/api/recipe/EmiRecipe;Ldev/emi/emi/runtime/EmiDrawContext;IIZI)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/emi/emi/EmiRenderHelper;renderRecipeBackground(Ldev/emi/emi/api/recipe/EmiRecipe;Ldev/emi/emi/runtime/EmiDrawContext;II)V",
            ordinal = 1,
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private static void emirs$flushRecipeAlphaPass(final EmiRecipe recipe,
                                                   final EmiDrawContext context,
                                                   final int x,
                                                   final int y,
                                                   final boolean showMissing,
                                                   final int overlayColor,
                                                   final CallbackInfo ci) {
        context.raw().flush();
    }
}
