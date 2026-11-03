package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.List;

public interface WorkstoneRecipeLike extends Recipe<RecipeWrapper>, WorkstoneProcess {

    int MAX_OUTPUTS = 6;

    Ingredient getInputIngredient();

    Ingredient getToolIngredient();

    List<ProcessingOutput> getOutputs();
}
