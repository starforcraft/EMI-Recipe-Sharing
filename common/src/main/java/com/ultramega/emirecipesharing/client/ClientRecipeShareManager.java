package com.ultramega.emirecipesharing.client;

import com.ultramega.emirecipesharing.network.ShareRecipePacket;
import com.ultramega.emirecipesharing.platform.Services;
import com.ultramega.emirecipesharing.recipes.RecipeChatComponentFactory;
import com.ultramega.emirecipesharing.recipes.RecipePreview;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class ClientRecipeShareManager {
    private static final int MAX_SHARED_RECIPES = 50;
    private static final Map<UUID, SharedRecipeDrawable> DRAWABLES =
        new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<UUID, SharedRecipeDrawable> eldest) {
                return this.size() > MAX_SHARED_RECIPES;
            }
        };

    private ClientRecipeShareManager() {
    }

    public static void receive(final ShareRecipePacket payload, final Player player) {
        Minecraft.getInstance().execute(() -> {
            if (!Services.PLATFORM.getConfig().get().showSharedRecipesInChat()) {
                return;
            }

            final EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(payload.recipeId());
            if (recipe == null || !recipe.getCategory().getId().equals(payload.categoryId())) {
                return;
            }

            final UUID id = UUID.randomUUID();
            DRAWABLES.put(id, new SharedRecipeDrawable(recipe, new RecipePreview(recipe)));
            player.sendSystemMessage(RecipeChatComponentFactory.makeSharedRecipeMessage(Component.literal(payload.sharerName()), id));
        });
    }

    @Nullable
    public static SharedRecipeDrawable get(final UUID id) {
        return DRAWABLES.get(id);
    }

    public record SharedRecipeDrawable(EmiRecipe recipe, RecipePreview preview) {
    }
}
