package com.ultramega.emirecipesharing.client;

import com.ultramega.emirecipesharing.client.ClientChatRecipeInteraction.Entry;
import com.ultramega.emirecipesharing.mixin.InvokerChatComponent;
import com.ultramega.emirecipesharing.recipes.RecipeChatLookup;
import com.ultramega.emirecipesharing.recipes.RecipePreview;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class ClientDrawRecipeInChat {
    @Nullable
    private static GuiGraphics graphics = null;
    @Nullable
    private static PendingTooltip pendingTooltip = null;
    private static final Set<GuiMessage> DRAWN_THIS_FRAME = Collections.newSetFromMap(new IdentityHashMap<>());

    private ClientDrawRecipeInChat() {
    }

    public static void beginFrame(@Nullable final GuiGraphics graphics) {
        ClientDrawRecipeInChat.graphics = graphics;
        pendingTooltip = null;
        DRAWN_THIS_FRAME.clear();
    }

    public static void endFrame() {
        if (graphics != null && pendingTooltip != null) {
            pendingTooltip.render(graphics);
        }

        graphics = null;
        pendingTooltip = null;
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

        final double scale = mc.options.chatScale().get();

        final int localMouseX = isChatFocused ? (int) Math.floor(mouseX / scale - 4.0D) : Integer.MIN_VALUE;
        final int localMouseY = isChatFocused ? (int) Math.floor(mouseY / scale) : Integer.MIN_VALUE;

        ClientChatRecipeInteraction.add(message, id, preview.getRectWithBorder(x, y));

        final int visibleHeight = Mth.floor((float) ((InvokerChatComponent) mc.gui.getChat()).emirs$getHeight() / (float) scale);
        final int chatBottom = Mth.floor((float) (graphics.guiHeight() - 40) / (float) scale);
        final int chatTop = chatBottom - visibleHeight;

        final int clipLeft = -4;
        final int clipRight = ((InvokerChatComponent) mc.gui.getChat()).emirs$getWidth() + 4;

        graphics.enableScissor(clipLeft, chatTop, clipRight, chatBottom);
        preview.render(graphics, x, y, localMouseX, localMouseY);
        graphics.disableScissor();

        if (isChatFocused) {
            final Entry entry = ClientChatRecipeInteraction.findEntryAt(localMouseX, localMouseY);
            if (entry != null) {
                final ClientRecipeShareManager.SharedRecipeDrawable hovered = ClientRecipeShareManager.get(entry.recipeId());
                if (hovered != null) {
                    final Rect2i rect = entry.rect();

                    final EmiStackInteraction interaction = hovered.preview().getStackAt(rect.getX(), rect.getY(), localMouseX, localMouseY);
                    if (!interaction.isEmpty()) {
                        pendingTooltip = new PendingTooltip(interaction, mouseX, mouseY);
                    }
                }
            }
        }
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

    private record PendingTooltip(EmiStackInteraction interaction, int screenMouseX, int screenMouseY) {
        private void render(final GuiGraphics graphics) {
            final var screen = Minecraft.getInstance().screen;
            if (screen == null) {
                return;
            }

            EmiRenderHelper.drawTooltip(screen, EmiDrawContext.wrap(graphics), this.interaction.getStack().getTooltip(), this.screenMouseX, this.screenMouseY);
        }
    }
}
