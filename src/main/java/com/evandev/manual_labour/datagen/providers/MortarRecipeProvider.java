package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.ChanceResult;
import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

public class MortarRecipeProvider {

    public static void buildRecipes(RecipeOutput output) {
    }

    private static void createGrindingRecipe(RecipeOutput output, String recipeName, Ingredient input, int processingTime, List<ChanceResult> resultsList) {
        NonNullList<ChanceResult> results = NonNullList.create();
        results.addAll(resultsList);

        MortarGrindingRecipe recipe = new MortarGrindingRecipe("", input, processingTime, results);

        output.accept(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mortar/grinding/" + recipeName),
                recipe,
                null
        );
    }

    private static void createMixingRecipe(RecipeOutput output, String recipeName, List<Ingredient> inputs,
                                            Optional<SizedFluidIngredient> fluidInput, int processingTime, List<ChanceResult> resultsList) {
        NonNullList<Ingredient> inputList = NonNullList.create();
        inputList.addAll(inputs);

        NonNullList<ChanceResult> results = NonNullList.create();
        results.addAll(resultsList);

        MortarMixingRecipe recipe = new MortarMixingRecipe("", inputList, fluidInput, processingTime, results);

        output.accept(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mortar/mixing/" + recipeName),
                recipe,
                null
        );
    }
}
