package com.ultramega.emirecipesharing.mixin;

import com.ultramega.emirecipesharing.client.ClientChatRecipeInteraction;
import com.ultramega.emirecipesharing.client.ClientDrawRecipeInChat;
import com.ultramega.emirecipesharing.client.ClientRecipeShareManager;
import com.ultramega.emirecipesharing.recipes.RecipeChatComponentFactory;
import com.ultramega.emirecipesharing.recipes.RecipeChatLookup;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements RecipeChatLookup {
    @Unique
    private final Map<GuiMessage.Line, GuiMessage> emirs$messageByLine = new IdentityHashMap<>();

    @Unique
    @Nullable
    private EmirsPendingSpacer emirs$pendingSpacer;

    @Unique
    private final Map<GuiMessage, UUID> emirs$recipesByMessage = new IdentityHashMap<>();

    @Unique
    private final Map<GuiMessage.Line, Integer> emirs$spacerIndexFromBottom = new IdentityHashMap<>();

    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Unique
    @Override
    @Nullable
    public GuiMessage emirs$getMessageForLine(final GuiMessage.Line line) {
        return this.emirs$messageByLine.get(line);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("HEAD"))
    private void emirs$captureGuiGraphics(final GuiGraphics graphics,
                                         final int tickCount,
                                         final int mouseX,
                                         final int mouseY,
                                         final boolean focused,
                                         final CallbackInfo ci) {
        ClientChatRecipeInteraction.beginFrame();
        ClientDrawRecipeInChat.beginFrame(graphics);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("RETURN"))
    private void emirs$finishRender(final GuiGraphics guiGraphics,
                                   final int tickCount,
                                   final int mouseX,
                                   final int mouseY,
                                   final boolean focused,
                                   final CallbackInfo ci) {
        ClientDrawRecipeInChat.endFrame();
    }

    @Redirect(
        method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I")
    )
    private int emirs$drawRecipes(final GuiGraphics guiGraphics,
                                 final Font font,
                                 final FormattedCharSequence text,
                                 final int x,
                                 final int textTop,
                                 final int color,
                                 final @Local GuiMessage.Line line,
                                 final @Share("opacity") LocalFloatRef opacityRef) {
        opacityRef.set((((color >> 24) + 256) % 256) / 255f);
        ClientDrawRecipeInChat.renderRecipeForLine(line, textTop, opacityRef.get());
        return guiGraphics.drawString(font, text, x, textTop, color);
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("HEAD"))
    private void emirs$addMessageToDisplayQueueHead(final GuiMessage message, final CallbackInfo ci) {
        this.emirs$pendingSpacer = null;

        final UUID id = RecipeChatComponentFactory.extractRecipeId(message.content());
        if (id == null) {
            return;
        }
        final ClientRecipeShareManager.SharedRecipeDrawable shared = ClientRecipeShareManager.get(id);
        if (shared == null) {
            return;
        }

        this.emirs$recipesByMessage.put(message, id);

        final int recipeHeight = shared.preview().height();
        final int padding = 4;
        final int lineHeight = this.getLineHeight();
        final int spacerLines = Mth.ceil((recipeHeight + padding) / (float) lineHeight);
        if (spacerLines <= 0) {
            return;
        }

        this.emirs$pendingSpacer = new EmirsPendingSpacer(message, spacerLines);
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("TAIL"))
    private void emirs$addMessageToDisplayQueueTail(final GuiMessage message, final CallbackInfo ci) {
        final EmirsPendingSpacer pending = this.emirs$pendingSpacer;
        this.emirs$pendingSpacer = null;

        if (pending == null || pending.message() != message) {
            return;
        }

        // The newest entry is at the front
        // Its current bottom-most line is the first endOfEntry=true line
        int endIndex = -1;
        for (int i = 0; i < this.trimmedMessages.size(); i++) {
            if (this.trimmedMessages.get(i).endOfEntry()) {
                endIndex = i;
                break;
            }
        }

        if (endIndex < 0) {
            return;
        }

        final GuiMessage.Line originalEnd = this.trimmedMessages.get(endIndex);
        this.trimmedMessages.set(endIndex, new GuiMessage.Line(originalEnd.addedTime(), originalEnd.content(), originalEnd.tag(), false));

        // Insert spacers at the front so they are BELOW the text visually
        // Index 0 is the bottom-most line
        for (int spacerIndexFromBottom = pending.spacerLines() - 1; spacerIndexFromBottom >= 0; spacerIndexFromBottom--) {
            final boolean isBottomMostSpacer = spacerIndexFromBottom == 0;

            final GuiMessage.Line spacerLine = new GuiMessage.Line(message.addedTime(), FormattedCharSequence.EMPTY, null, isBottomMostSpacer);

            this.trimmedMessages.addFirst(spacerLine);
            this.emirs$spacerIndexFromBottom.put(spacerLine, spacerIndexFromBottom);
            this.emirs$messageByLine.put(spacerLine, message);
        }

        while (this.trimmedMessages.size() > 100) {
            final GuiMessage.Line removed = this.trimmedMessages.removeLast();
            this.emirs$spacerIndexFromBottom.remove(removed);
            this.emirs$messageByLine.remove(removed);
        }

        this.emirs$recipesByMessage.keySet().removeIf(msg -> {
            for (final GuiMessage.Line line : this.trimmedMessages) {
                if (this.emirs$messageByLine.get(line) == msg) {
                    return false;
                }
            }
            return true;
        });
    }

    @Unique
    @Override
    @Nullable
    public UUID emirs$getRecipeId(final GuiMessage message) {
        return this.emirs$recipesByMessage.get(message);
    }

    @Unique
    @Override
    @Nullable
    public Integer emirs$getSpacerIndexFromBottom(final GuiMessage.Line line) {
        return this.emirs$spacerIndexFromBottom.get(line);
    }

    @Shadow
    protected abstract int getLineHeight();

    @Unique
    private record EmirsPendingSpacer(GuiMessage message, int spacerLines) {
    }
}
