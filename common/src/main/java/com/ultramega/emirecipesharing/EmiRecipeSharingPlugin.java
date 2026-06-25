package com.ultramega.emirecipesharing;

import com.ultramega.emirecipesharing.client.ClientRecipeShareManager;
import com.ultramega.emirecipesharing.recipes.EmiRecipeShareDecorator;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class EmiRecipeSharingPlugin implements EmiPlugin {
    @Override
    public void register(final EmiRegistry registry) {
        registry.addRecipeDecorator(new EmiRecipeShareDecorator());
    }

    public static void openSharedRecipe(final ClientRecipeShareManager.SharedRecipeDrawable shared) {
        EmiApi.displayRecipe(shared.recipe());
    }
}
