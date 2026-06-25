package com.ultramega.emirecipesharing.mixin;

import com.ultramega.emirecipesharing.client.ClientChatRecipeInteraction;
import com.ultramega.emirecipesharing.client.ClientChatRecipeInteraction.Entry;
import com.ultramega.emirecipesharing.client.ClientRecipeShareManager;

import dev.emi.emi.api.EmiApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
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
