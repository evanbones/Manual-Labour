package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.create.impl.millstone.CreateContent;
import com.evandev.manual_labour.compat.jei.EmptyBackground;
import com.evandev.manual_labour.compat.jei.ItemIcon;
import com.evandev.manual_labour.compat.jei.ManualLabourJeiPlugin;
import com.evandev.manual_labour.compat.jei.ManualRecipeCategory;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.recipe.WorkstoneRecipeLike;
import com.evandev.manual_labour.registry.ModBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CreateJeiCategories {

    private CreateJeiCategories() {
    }

    public static void addCategories(Consumer<ManualRecipeCategory<?>> sink) {
        if (ModConfig.get().enableManualAssemblyJei) {
            sink.accept(new ManualAssemblyCategory(ManualLabourJeiPlugin.info(
                    ManualLabourJeiPlugin.id("manual_assembly"),
                    new EmptyBackground(180, 115),
                    new ItemIcon(() -> new ItemStack(ModBlocks.WORKSTONE.get())),
                    CreateJeiCategories::gatherWorkstoneAssemblies,
                    List.of(() -> new ItemStack(ModBlocks.WORKSTONE.get()))
            )));
        }

        if (ModConfig.get().enableMillstoneJei) {
            sink.accept(new MillstoneCategory(ManualLabourJeiPlugin.info(
                    ManualLabourJeiPlugin.id("millstone"),
                    new EmptyBackground(177, 100),
                    new ItemIcon(() -> new ItemStack(CreateContent.MILLSTONE.get())),
                    CreateJeiCategories::gatherMillingRecipes,
                    List.of(() -> new ItemStack(CreateContent.MILLSTONE.get()))
            )));
        }

        if (ModConfig.get().enableManualPressingJei && ModConfig.get().useCreatePressingRecipes) {
            sink.accept(new ManualPressingCategory(ManualLabourJeiPlugin.info(
                    ManualLabourJeiPlugin.id("manual_pressing"),
                    new EmptyBackground(177, 70),
                    new ItemIcon(() -> new ItemStack(ModBlocks.WORKSTONE.get())),
                    CreateJeiCategories::gatherPressingRecipes,
                    List.of(() -> new ItemStack(ModBlocks.WORKSTONE.get()))
            )));
        }
    }

    private static List<RecipeHolder<SequencedAssemblyRecipe>> gatherWorkstoneAssemblies() {
        RecipeManager manager = ManualLabourJeiPlugin.recipeManager();
        if (manager == null) return List.of();

        List<RecipeHolder<SequencedAssemblyRecipe>> recipes = new ArrayList<>();
        for (RecipeHolder<SequencedAssemblyRecipe> holder :
                manager.getAllRecipesFor(AllRecipeTypes.SEQUENCED_ASSEMBLY.<RecipeWrapper, SequencedAssemblyRecipe>getType())) {
            if (usesWorkstone(holder.value())) recipes.add(holder);
        }
        return recipes;
    }

    private static boolean usesWorkstone(SequencedAssemblyRecipe recipe) {
        boolean allowDeploying = ModConfig.get().useCreateDeployingRecipes;
        boolean allowPressing = ModConfig.get().useCreatePressingRecipes;
        for (SequencedRecipe<?> step : recipe.getSequence()) {
            Object stepRecipe = step.getRecipe();
            if (stepRecipe instanceof WorkstoneRecipeLike) return true;
            if (allowDeploying && stepRecipe instanceof DeployerApplicationRecipe) return true;
            if (allowPressing && stepRecipe instanceof PressingRecipe) return true;
        }
        return false;
    }

    private static List<RecipeHolder<AbstractCrushingRecipe>> gatherMillingRecipes() {
        RecipeManager manager = ManualLabourJeiPlugin.recipeManager();
        if (manager == null) return List.of();
        return new ArrayList<>(manager.getAllRecipesFor(
                AllRecipeTypes.MILLING.<RecipeInput, AbstractCrushingRecipe>getType()));
    }

    private static List<RecipeHolder<PressingRecipe>> gatherPressingRecipes() {
        RecipeManager manager = ManualLabourJeiPlugin.recipeManager();
        if (manager == null) return List.of();
        return new ArrayList<>(manager.getAllRecipesFor(
                AllRecipeTypes.PRESSING.<SingleRecipeInput, PressingRecipe>getType()));
    }
}
