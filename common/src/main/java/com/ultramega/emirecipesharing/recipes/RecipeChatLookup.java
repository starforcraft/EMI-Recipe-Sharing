package com.ultramega.emirecipesharing.recipes;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.client.GuiMessage;

public interface RecipeChatLookup {
    @Nullable
    UUID emirs$getRecipeId(GuiMessage message);

    @Nullable
    Integer emirs$getSpacerIndexFromBottom(GuiMessage.Line line);

    @Nullable
    GuiMessage emirs$getMessageForLine(GuiMessage.Line line);
}
