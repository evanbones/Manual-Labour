package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.recipe.ChanceResult;
import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MortarRecipeProvider {

    private static final int WATER_PER_MIX = 250;
    private static final int SOFT_GRIND = 60;
    private static final int MEDIUM_GRIND = 80;
    private static final int HARD_GRIND = 100;

    public static void buildRecipes(RecipeOutput rawOutput) {
        RecipeOutput output = rawOutput.withConditions(new NotCondition(new ModLoadedCondition(CreateCompat.CREATE)));

        buildGrindingRecipes(output);
        buildMixingRecipes(output);
    }

    private static void buildGrindingRecipes(RecipeOutput output) {
        grind(output, "bone_meal", Items.BONE, MEDIUM_GRIND, Items.BONE_MEAL, 4);
        grind(output, "blaze_powder", Items.BLAZE_ROD, MEDIUM_GRIND, Items.BLAZE_POWDER, 3);
        grind(output, "glowstone_dust", Blocks.GLOWSTONE, MEDIUM_GRIND, Items.GLOWSTONE_DUST, 4);

        grind(output, "gravel", Blocks.COBBLESTONE, HARD_GRIND, Items.GRAVEL, 1);
        grind(output, "sand", Blocks.GRAVEL, HARD_GRIND, Items.SAND, 1);

        grind(output, "black_dye", Items.INK_SAC, SOFT_GRIND, Items.BLACK_DYE, 2);
        grind(output, "brown_dye", Items.COCOA_BEANS, SOFT_GRIND, Items.BROWN_DYE, 2);
        grind(output, "blue_dye_from_lapis", Items.LAPIS_LAZULI, SOFT_GRIND, Items.BLUE_DYE, 2);
        grind(output, "red_dye_from_beetroot", Items.BEETROOT, SOFT_GRIND, Items.RED_DYE, 2);
        grind(output, "green_dye", Blocks.CACTUS, SOFT_GRIND, Items.GREEN_DYE, 2);

        flowerDye(output, Blocks.DANDELION, Items.YELLOW_DYE);
        flowerDye(output, Blocks.POPPY, Items.RED_DYE);
        flowerDye(output, Blocks.BLUE_ORCHID, Items.LIGHT_BLUE_DYE);
        flowerDye(output, Blocks.ALLIUM, Items.MAGENTA_DYE);
        flowerDye(output, Blocks.AZURE_BLUET, Items.LIGHT_GRAY_DYE);
        flowerDye(output, Blocks.RED_TULIP, Items.RED_DYE);
        flowerDye(output, Blocks.ORANGE_TULIP, Items.ORANGE_DYE);
        flowerDye(output, Blocks.WHITE_TULIP, Items.LIGHT_GRAY_DYE);
        flowerDye(output, Blocks.PINK_TULIP, Items.PINK_DYE);
        flowerDye(output, Blocks.OXEYE_DAISY, Items.LIGHT_GRAY_DYE);
        flowerDye(output, Blocks.CORNFLOWER, Items.BLUE_DYE);
        flowerDye(output, Blocks.LILY_OF_THE_VALLEY, Items.WHITE_DYE);
        flowerDye(output, Blocks.WITHER_ROSE, Items.BLACK_DYE);
        flowerDye(output, Blocks.TORCHFLOWER, Items.ORANGE_DYE);
    }

    private static void buildMixingRecipes(RecipeOutput output) {
        mix(output, "mud", List.of(Ingredient.of(Blocks.DIRT)), water(), MEDIUM_GRIND, Items.MUD, 1);
        mix(output, "clay", Collections.nCopies(4, Ingredient.of(Items.CLAY_BALL)), water(), MEDIUM_GRIND, Items.CLAY, 1);

        for (DyeColor colour : DyeColor.values()) {
            concretePowder(output, colour);
        }
    }

    private static void concretePowder(RecipeOutput output, DyeColor colour) {
        List<Ingredient> inputs = new ArrayList<>(9);
        inputs.addAll(Collections.nCopies(4, Ingredient.of(Items.SAND)));
        inputs.addAll(Collections.nCopies(4, Ingredient.of(Items.GRAVEL)));
        inputs.add(Ingredient.of(DyeItem.byColor(colour)));

        mix(output, colour.getName() + "_concrete_powder", inputs, Optional.empty(), HARD_GRIND,
                concretePowderFor(colour), 8);
    }

    private static ItemLike concretePowderFor(DyeColor colour) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(colour.getName() + "_concrete_powder"));
    }

    private static Optional<SizedFluidIngredient> water() {
        return Optional.of(SizedFluidIngredient.of(Fluids.WATER, WATER_PER_MIX));
    }

    private static void flowerDye(RecipeOutput output, ItemLike flower, Item dye) {
        grind(output, BuiltInRegistries.ITEM.getKey(dye).getPath() + "_from_" + BuiltInRegistries.ITEM.getKey(flower.asItem()).getPath(),
                flower, SOFT_GRIND, dye, 2);
    }

    private static void grind(RecipeOutput output, String recipeName, ItemLike input, int processingTime,
                              ItemLike result, int count) {
        createGrindingRecipe(output, recipeName, Ingredient.of(input), processingTime,
                List.of(new ChanceResult(new ItemStack(result, count), 1.0F)), List.of());
    }

    private static void mix(RecipeOutput output, String recipeName, List<Ingredient> inputs,
                            Optional<SizedFluidIngredient> fluidInput, int processingTime,
                            ItemLike result, int count) {
        createMixingRecipe(output, recipeName, inputs, fluidInput, processingTime,
                List.of(new ChanceResult(new ItemStack(result, count), 1.0F)), List.of());
    }

    private static void createGrindingRecipe(RecipeOutput output, String recipeName, Ingredient input, int processingTime,
                                             List<ChanceResult> resultsList, List<FluidStack> fluidResultsList) {
        NonNullList<ChanceResult> results = NonNullList.create();
        results.addAll(resultsList);

        NonNullList<FluidStack> fluidResults = NonNullList.create();
        fluidResults.addAll(fluidResultsList);

        MortarGrindingRecipe recipe = new MortarGrindingRecipe("", input, processingTime, results, fluidResults);

        output.accept(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mortar/grinding/" + recipeName),
                recipe,
                null
        );
    }

    private static void createMixingRecipe(RecipeOutput output, String recipeName, List<Ingredient> inputs,
                                           Optional<SizedFluidIngredient> fluidInput, int processingTime,
                                           List<ChanceResult> resultsList, List<FluidStack> fluidResultsList) {
        NonNullList<Ingredient> inputList = NonNullList.create();
        inputList.addAll(inputs);

        NonNullList<ChanceResult> results = NonNullList.create();
        results.addAll(resultsList);

        NonNullList<FluidStack> fluidResults = NonNullList.create();
        fluidResults.addAll(fluidResultsList);

        MortarMixingRecipe recipe = new MortarMixingRecipe("", inputList, fluidInput, processingTime, results, fluidResults);

        output.accept(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mortar/mixing/" + recipeName),
                recipe,
                null
        );
    }
}
