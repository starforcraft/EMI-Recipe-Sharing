package com.ultramega.emirecipesharing.mixin;

import com.ultramega.emirecipesharing.EmiRecipeSharingPlugin;
import com.ultramega.emirecipesharing.client.ClientChatRecipeInteraction;
import com.ultramega.emirecipesharing.client.ClientRecipeShareManager;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(final double mouseX, final double mouseY, final int button, final CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();

        final double localX = mouseX / mc.options.chatScale().get() - 4.0;
        final double localY = mouseY / mc.options.chatScale().get();

        final UUID recipeId = ClientChatRecipeInteraction.findRecipeAt(localX, localY);
        if (recipeId == null) {
            return;
        }
        final ClientRecipeShareManager.SharedRecipeDrawable shared = ClientRecipeShareManager.get(recipeId);
        if (shared == null) {
            return;
        }

        EmiRecipeSharingPlugin.openSharedRecipe(shared);
        cir.setReturnValue(true);
    }
}
