package com.ultramega.emirecipesharing.client;

import com.ultramega.emirecipesharing.mixin.InvokerChatComponent;
import com.ultramega.emirecipesharing.recipes.RecipeChatLookup;
import com.ultramega.emirecipesharing.recipes.RecipePreview;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class ClientDrawRecipeInChat {
    @Nullable
    private static GuiGraphics graphics = null;
    private static final Set<GuiMessage> DRAWN_THIS_FRAME = Collections.newSetFromMap(new IdentityHashMap<>());

    private ClientDrawRecipeInChat() {
    }

    public static void beginFrame(@Nullable final GuiGraphics graphics) {
        ClientDrawRecipeInChat.graphics = graphics;
        DRAWN_THIS_FRAME.clear();
    }

    public static void endFrame() {
        graphics = null;
        DRAWN_THIS_FRAME.clear();
    }

    // TODO: I don't know if there's a good way to implement opacity, we will just keep it opaque for now
    public static void renderRecipeForLine(final GuiMessage.Line line, final float textTop, final float opacity) {
        if (graphics == null) {
            return;
        }

        // Only draw on spacer lines
        if (line.content() != FormattedCharSequence.EMPTY) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final RecipeChatLookup lookup = (RecipeChatLookup) mc.gui.getChat();

        final Integer spacerIndexFromBottom = lookup.emirs$getSpacerIndexFromBottom(line);
        if (spacerIndexFromBottom == null) {
            return;
        }
        final GuiMessage message = lookup.emirs$getMessageForLine(line);
        if (message == null) {
            return;
        }
        if (!DRAWN_THIS_FRAME.add(message)) {
            return;
        }
        final UUID id = lookup.emirs$getRecipeId(message);
        if (id == null) {
            return;
        }
        final ClientRecipeShareManager.SharedRecipeDrawable shared = ClientRecipeShareManager.get(id);
        if (shared == null) {
            return;
        }

        final RecipePreview preview = shared.preview();

        final int x = mc.font.width(" ");
        final int y = (int) textTop - preview.height() + mc.font.lineHeight / 2 + spacerIndexFromBottom * mc.font.lineHeight;

        final boolean isChatFocused = mc.screen instanceof ChatScreen;
        final int mouseX = isChatFocused ? getGuiMouseX(mc) : Integer.MIN_VALUE;
        final int mouseY = isChatFocused ? getGuiMouseY(mc) : Integer.MIN_VALUE;

        ClientChatRecipeInteraction.add(message, id, preview.getRectWithBorder(x, y));

        final double scale = mc.options.chatScale().get();

        final int visibleHeight = Mth.floor((float) ((InvokerChatComponent) mc.gui.getChat()).emirs$getHeight() / (float) scale);
        final int chatBottom = Mth.floor((float) (graphics.guiHeight() - 40) / (float) scale);
        final int chatTop = chatBottom - visibleHeight;

        final int clipLeft = -4;
        final int clipRight = ((InvokerChatComponent) mc.gui.getChat()).emirs$getWidth() + 4;

        graphics.enableScissor(clipLeft, chatTop, clipRight, chatBottom);
        preview.render(graphics, x, y, mouseX, mouseY);
        graphics.disableScissor();
    }

    private static int getGuiMouseX(final Minecraft mc) {
        final var window = mc.getWindow();
        return (int) Math.floor(
            mc.mouseHandler.xpos() * window.getGuiScaledWidth() / (double) window.getScreenWidth()
        );
    }

    private static int getGuiMouseY(final Minecraft mc) {
        final var window = mc.getWindow();
        return (int) Math.floor(
            mc.mouseHandler.ypos() * window.getGuiScaledHeight() / (double) window.getScreenHeight()
        );
    }
}
