package com.ultramega.emirecipesharing.mixin.client;

import com.ultramega.emirecipesharing.client.ClientChatRecipeInteraction;
import com.ultramega.emirecipesharing.client.ClientChatRecipeInteraction.Entry;
import com.ultramega.emirecipesharing.client.ClientRecipeShareManager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.api.EmiApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Unique
    private static final float EMIRS_COMMAND_SUGGESTIONS_Z = 400.0F;

    @WrapOperation(method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void emirs$renderCommandSuggestionsAboveRecipes(final CommandSuggestions suggestions,
                                                            final GuiGraphics graphics,
                                                            final int mouseX,
                                                            final int mouseY,
                                                            final Operation<Void> original) {
        // EMI widgets render at positive Z offsets (up to 300), which leaves items above the command suggestions even though they are drawn later.
        // Therefore, render the whole suggestions tab at the tooltip layer.
        // This won't be required in 26.1.2
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, EMIRS_COMMAND_SUGGESTIONS_Z);
        try {
            original.call(suggestions, graphics, mouseX, mouseY);
        } finally {
            graphics.pose().popPose();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(final double mouseX, final double mouseY, final int button, final CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();

        final double localX = mouseX / mc.options.chatScale().get() - 4.0;
        final double localY = mouseY / mc.options.chatScale().get();

        final Entry recipeEntry = ClientChatRecipeInteraction.findEntryAt(localX, localY);
        if (recipeEntry == null) {
            return;
        }
        final ClientRecipeShareManager.SharedRecipeDrawable shared = ClientRecipeShareManager.get(recipeEntry.recipeId());
        if (shared == null) {
            return;
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        EmiApi.displayRecipe(shared.recipe());
        cir.setReturnValue(true);
    }
}
