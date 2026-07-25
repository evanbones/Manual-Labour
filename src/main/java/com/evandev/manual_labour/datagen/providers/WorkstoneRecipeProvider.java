package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.ChanceResult;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import com.evandev.manual_labour.registry.ModTags;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class WorkstoneRecipeProvider {

    public static void buildRecipes(RecipeOutput output) {

        createHammerRecipe(output, "cobblestone_to_gravel",
                Ingredient.of(Items.COBBLESTONE),
                List.of(new ChanceResult(new ItemStack(Items.GRAVEL), 1.0F)));

        createHammerRecipe(output, "gravel_to_sand",
                Ingredient.of(Items.GRAVEL),
                List.of(
                        new ChanceResult(new ItemStack(Items.SAND), 1.0F),
                        new ChanceResult(new ItemStack(Items.FLINT), 0.25F)
                ));
    }

    private static void createHammerRecipe(RecipeOutput output, String recipeName, Ingredient input, List<ChanceResult> resultsList) {
        Ingredient tool = Ingredient.of(ModTags.Items.HAMMERS);

        NonNullList<ChanceResult> results = NonNullList.create();
        results.addAll(resultsList);

        WorkstoneRecipe recipe = new WorkstoneRecipe("", input, tool, results);

        output.accept(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "workstone/" + recipeName),
                recipe,
                null
        );
    }
}
