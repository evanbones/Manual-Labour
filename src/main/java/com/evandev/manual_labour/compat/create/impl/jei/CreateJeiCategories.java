package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.create.impl.millstone.CreateContent;
import com.evandev.manual_labour.compat.jei.EmptyBackground;
import com.evandev.manual_labour.compat.jei.ItemIcon;
import com.evandev.manual_labour.compat.jei.ManualLabourJeiPlugin;
import com.evandev.manual_labour.compat.jei.ManualRecipeCategory;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.recipe.ManualProcessingExclusions;
import com.evandev.manual_labour.recipe.WorkstoneRecipeLike;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class CreateJeiCategories {

    private CreateJeiCategories() {
    }

    public static void addCategories(Consumer<ManualRecipeCategory<?>> sink) {
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

    public static void addSequencedAssemblies(RecipeManager manager, List<RecipeHolder<Recipe<?>>> out) {
        for (RecipeHolder<SequencedAssemblyRecipe> holder :
                manager.getAllRecipesFor(AllRecipeTypes.SEQUENCED_ASSEMBLY.<RecipeWrapper, SequencedAssemblyRecipe>getType())) {
            if (CreateAssemblyView.isManual(holder.value())) {
                out.add(new RecipeHolder<>(holder.id(), holder.value()));
            }
        }
    }

    private static List<RecipeHolder<AbstractCrushingRecipe>> gatherMillingRecipes() {
        RecipeManager manager = ManualLabourJeiPlugin.recipeManager();
        if (manager == null) return List.of();

        List<RecipeHolder<AbstractCrushingRecipe>> recipes = new ArrayList<>();
        manager.getAllRecipesFor(AllRecipeTypes.MILLING.<RecipeInput, AbstractCrushingRecipe>getType())
                .forEach(r -> {
                    if (!ManualProcessingExclusions.isExcluded(r.id())) recipes.add(r);
                });
        return recipes;
    }

    private static List<RecipeHolder<PressingRecipe>> gatherPressingRecipes() {
        RecipeManager manager = ManualLabourJeiPlugin.recipeManager();
        if (manager == null) return List.of();

        Set<Item> overridden = new HashSet<>();
        for (RecipeHolder<WorkstoneRecipeLike> workstoneRecipe : manager.getAllRecipesFor(ModRecipeTypes.WORKSTONE.get())) {
            for (ItemStack stack : workstoneRecipe.value().getInputIngredient().getItems()) {
                overridden.add(stack.getItem());
            }
        }

        List<RecipeHolder<PressingRecipe>> recipes = new ArrayList<>();
        for (RecipeHolder<PressingRecipe> holder : manager.getAllRecipesFor(
                AllRecipeTypes.PRESSING.<SingleRecipeInput, PressingRecipe>getType())) {
            if (ManualProcessingExclusions.isExcluded(holder.id())) continue;

            boolean covered = false;
            for (ItemStack stack : holder.value().getIngredients().getFirst().getItems()) {
                if (overridden.contains(stack.getItem())) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                recipes.add(holder);
            }
        }
        return recipes;
    }
}
