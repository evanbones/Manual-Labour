package com.evandev.manual_labour.compat.emi;

import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@EmiEntrypoint
public class ManualLabourEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ManualLabourCategories.WORKSTONE);
        registry.addWorkstation(ManualLabourCategories.WORKSTONE, ManualLabourCategories.WORKSTONE_STACK);

        for (RecipeHolder<WorkstoneRecipe> recipeHolder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.WORKSTONE.get())) {
            WorkstoneRecipe recipe = recipeHolder.value();
            var emiOutputs = recipe.getResults().stream()
                    .map(chanceResult -> EmiStack.of(chanceResult.stack()).setChance(chanceResult.chance()))
                    .toList();

            registry.addRecipe(new WorkstoneEmiRecipe(
                    recipeHolder.id(),
                    EmiIngredient.of(recipe.getTool()),
                    EmiIngredient.of(recipe.getIngredients().getFirst()),
                    emiOutputs
            ));
        }
    }
}
